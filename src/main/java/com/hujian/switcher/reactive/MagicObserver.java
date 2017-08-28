/**
 * Copyright 2017 hujian
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
import com.hujian.switcher.reactive.functions.Action;
import com.hujian.switcher.reactive.functions.Consumer;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by hujian06 on 2017/8/27.
 *
 * this class will be converted to ObservableCreate<T>, then the observable will
 * operate the object and the observer will be called at the same time of "subscribe"
 * on the source observable
 */
public final class MagicObserver<T> extends AtomicReference<Disposable> implements Observer<T>, Disposable {
    final Consumer<? super T> onNext; // onNext consumer
    final Consumer<? super Throwable> onError; // onError consumer
    final Action onComplete; // onComplete action
    final Consumer<? super Disposable> onSubscribe; // subscribe

    /**
     * the constructor
     * @param onNext next
     * @param onError error
     * @param onComplete complete
     * @param onSubscribe  subscribe
     */
    public MagicObserver(Consumer<? super T> onNext, Consumer<? super Throwable> onError,
                         Action onComplete, Consumer<? super Disposable> onSubscribe) {
        super();
        this.onNext = onNext;
        this.onError = onError;
        this.onComplete = onComplete;
        this.onSubscribe = onSubscribe;
    }

    @Override
    public void onSubscribe(Disposable s) {
        if (DisposableHelper.setOnce(this, s)) {
            try {
                onSubscribe.accept(this);
            } catch (Throwable ex) {
                s.dispose();
                onError(ex);
            }
        }
    }

    @Override
    public void onNext(T t) {
        if (!isDisposed()) {
            try {
                onNext.accept(t);
            } catch (Throwable e) {
                get().dispose();
                onError(e);
            }
        }
    }

    @Override
    public void onError(Throwable t) {
        if (!isDisposed()) {
            lazySet(DisposableHelper.DISPOSED);
            try {
                onError.accept(t);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onComplete() {
        if (!isDisposed()) {
            lazySet(DisposableHelper.DISPOSED);
            try {
                onComplete.run();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void dispose() {
        DisposableHelper.dispose(this);
    }

    @Override
    public boolean isDisposed() {
        return get() == DisposableHelper.DISPOSED;
    }
}
