/**
 * Copyright (c) 2017 hujian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hujian.switcher.reactive;

import com.hujian.switcher.reactive.aux.DisposableHelper;
import com.hujian.switcher.reactive.aux.SimpleQueue;
import com.hujian.switcher.reactive.aux.SpscLinkedArrayQueue;
import com.hujian.switcher.reactive.functions.Cancellable;
import com.hujian.switcher.schedulers.ScheduleHooks;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by hujian06 on 2017/8/25.
 */
public final class ObservableCreate<T> extends Observable<T> {

    final ObservableOnSubscribe<T> source; // this is the source observable

    /**
     * the constructor,try to get the source observable
     * @param source
     */
    public ObservableCreate(ObservableOnSubscribe<T> source) {
        this.source = source;
    }

    /**
     * the actual worker,do the subscribe job here
     * @param observer the incoming Observer, never null
     */
    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        //the parent is the observer,the observable will operate it at the 'create'
        //function,then the observable will let the observer's onNext/onError/onComplete
        //work,then the observer will subscribe the source observable,so,the observer will
        //receive the emit information from 'subscribe observable'
        //so,actually,the var "parent" is the connection-er between "Observer" and "Observable"
        CreateEmitter<T> parent = new CreateEmitter<T>(observer);
        //touch the 'onSubscribe' for observer,the observer that subscribe on this source observable
        //will receive the information firstly.
        observer.onSubscribe(parent);
        try {
            //assign to the source observable with the observer
            //then, the 'parent' will be operated by source observable on 'create' stage
            source.subscribe(parent);
        } catch (Throwable ex) {
            ScheduleHooks.onError(ex);
            parent.onError(ex);
        }
    }

    /**
     * try to convert observer to emitter for observable.the observable will
     * call these callback functions on the 'create' stage.
     *
     * the observer will be called after subscribe on the source observable
     *
     * @param <T>
     *           the type
     */
    static final class CreateEmitter<T> extends AtomicReference<Disposable>
            implements ObservableEmitter<T>, Disposable {

        final Observer<? super T> observer;

        CreateEmitter(Observer<? super T> observer) {
            this.observer = observer;
        }

        @Override
        public void onNext(T t) {
            if (t == null) {
                Exception npe = new NullPointerException("onNext called with null.");
                onError(npe);
                return;
            }
            if (!isDisposed()) {
                observer.onNext(t);
            }
        }

        @Override
        public void onError(Throwable t) {
            if (!tryOnError(t)) {
                observer.onError(t);
            }
        }

        @Override
        public boolean tryOnError(Throwable t) {
            if (t == null) {
                t = new NullPointerException("onError called with null.");
            }
            if (!isDisposed()) {
                try {
                    observer.onError(t);
                } finally {
                    dispose();
                }
                return true;
            }
            return false;
        }

        @Override
        public void onComplete() {
            if (!isDisposed()) {
                try {
                    observer.onComplete();
                } finally {
                    dispose();
                }
            }
        }

        @Override
        public void setDisposable(Disposable d) {
            DisposableHelper.set(this, d);
        }

        @Override
        public void setCancellable(Cancellable c) {
            setDisposable(new CancellableDisposable(c));
        }

        @Override
        public ObservableEmitter<T> serialize() {
            return new SerializedEmitter<T>(this);
        }

        @Override
        public void dispose() {
            DisposableHelper.dispose(this);
        }

        @Override
        public boolean isDisposed() {
            return DisposableHelper.isDisposed(get());
        }
    }

    /**
     * Serializes calls to onNext, onError and onComplete.
     *
     * @param <T> the value type
     */
    static final class SerializedEmitter<T>
            extends AtomicInteger
            implements ObservableEmitter<T> {

        private static final long serialVersionUID = 4883307006032401862L;

        final ObservableEmitter<T> emitter;

        final List<Throwable> errorList;

        final SpscLinkedArrayQueue<T> queue;

        volatile boolean done;

        SerializedEmitter(ObservableEmitter<T> emitter) {
            this.emitter = emitter;
            this.errorList = new Vector<>();
            this.queue = new SpscLinkedArrayQueue<T>(16);
        }

        @Override
        public void onNext(T t) {
            if (emitter.isDisposed() || done) {
                return;
            }
            if (t == null) {
                onError(new NullPointerException("onNext called with null."));
                return;
            }
            if (get() == 0 && compareAndSet(0, 1)) {
                emitter.onNext(t);
                if (decrementAndGet() == 0) {
                    return;
                }
            } else {
                SimpleQueue<T> q = queue;
                synchronized (q) {
                    q.offer(t);
                }
                if (getAndIncrement() != 0) {
                    return;
                }
            }
            drainLoop();
        }

        @Override
        public void onError(Throwable t) {
            if (!tryOnError(t)) {
                emitter.onError(t);
            }
        }

        @Override
        public boolean tryOnError(Throwable t) {
            if (emitter.isDisposed() || done) {
                return false;
            }
            if (t == null) {
                t = new NullPointerException("onError called with null.");
            }
            if (errorList.add(t)) {
                done = true;
                drain();
                return true;
            }
            return false;
        }

        @Override
        public void onComplete() {
            if (emitter.isDisposed() || done) {
                return;
            }
            done = true;
            drain();
        }

        void drain() {
            if (getAndIncrement() == 0) {
                drainLoop();
            }
        }

        void drainLoop() {
            ObservableEmitter<T> e = emitter;
            SpscLinkedArrayQueue<T> q = queue;
            List<Throwable> error = this.errorList;
            int missed = 1;
            for (;;) {
                for (;;) {
                    if (e.isDisposed()) {
                        q.clear();
                        return;
                    }
                    if (!error.isEmpty()) {
                        q.clear();
                        e.onError(error.get(0));
                        error.clear();
                        return;
                    }

                    boolean d = done;
                    T v = q.poll();

                    boolean empty = v == null;

                    if (d && empty) {
                        e.onComplete();
                        return;
                    }

                    if (empty) {
                        break;
                    }

                    e.onNext(v);
                }

                missed = addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            }
        }

        @Override
        public void setDisposable(Disposable s) {
            emitter.setDisposable(s);
        }

        @Override
        public void setCancellable(Cancellable c) {
            emitter.setCancellable(c);
        }

        @Override
        public boolean isDisposed() {
            return emitter.isDisposed();
        }

        @Override
        public ObservableEmitter<T> serialize() {
            return this;
        }
    }

}
