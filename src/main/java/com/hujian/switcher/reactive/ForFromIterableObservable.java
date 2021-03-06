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

import com.hujian.switcher.reactive.aux.BasicQueueDisposable;
import com.hujian.switcher.reactive.aux.EmptyDisposable;
import com.hujian.switcher.reactive.aux.ObjectHelper;

import java.util.Iterator;

/**
 * Created by hujian06 on 2017/8/27.
 */
public final class ForFromIterableObservable<T> extends Observable<T> {
    private final Iterable<? extends T> source;
    public ForFromIterableObservable(Iterable<? extends T> source) {
        this.source = source;
    }

    @Override
    public void subscribeActual(Observer<? super T> s) {
        Iterator<? extends T> it;
        try {
            it = source.iterator();
        } catch (Throwable e) {
            EmptyDisposable.error(e, s);
            return;
        }
        boolean hasNext;
        try {
            hasNext = it.hasNext();
        } catch (Throwable e) {
            EmptyDisposable.error(e, s);
            return;
        }
        if (!hasNext) {
            EmptyDisposable.complete(s);
            return;
        }

        FromIterableDisposable<T> d = new FromIterableDisposable<T>(s, it);
        s.onSubscribe(d);

        if (!d.fusionMode) {
            d.run();
        }
    }

    static final class FromIterableDisposable<T> extends BasicQueueDisposable<T> {

        final Observer<? super T> actual;

        final Iterator<? extends T> it;

        volatile boolean disposed;

        boolean fusionMode;

        boolean done;

        boolean checkNext;

        FromIterableDisposable(Observer<? super T> actual, Iterator<? extends T> it) {
            this.actual = actual;
            this.it = it;
        }

        /**
         * actual worker
         */
        void run() {
            boolean hasNext;
            do {
                if (isDisposed()) {
                    return;
                }
                T v;
                try {
                    v = ObjectHelper.requireNonNull(it.next(), "The iterator returned a null value");
                } catch (Throwable e) {
                    actual.onError(e);
                    return;
                }

                //on next
                actual.onNext(v);

                if (isDisposed()) {
                    return;
                }
                try {
                    hasNext = it.hasNext();
                } catch (Throwable e) {
                    actual.onError(e);
                    return;
                }
            } while (hasNext);

            if (!isDisposed()) {
                actual.onComplete();
            }
        }

        @Override
        public int requestFusion(int mode) {
            if ((mode & SYNC) != 0) {
                fusionMode = true;
                return SYNC;
            }
            return NONE;
        }

        @Override
        public T poll() {
            if (done) {
                return null;
            }
            if (checkNext) {
                if (!it.hasNext()) {
                    done = true;
                    return null;
                }
            } else {
                checkNext = true;
            }

            return ObjectHelper.requireNonNull(it.next(), "The iterator returned a null value");
        }

        @Override
        public boolean isEmpty() {
            return done;
        }

        @Override
        public void clear() {
            done = true;
        }

        @Override
        public void dispose() {
            disposed = true;
        }

        @Override
        public boolean isDisposed() {
            return disposed;
        }
    }
}
