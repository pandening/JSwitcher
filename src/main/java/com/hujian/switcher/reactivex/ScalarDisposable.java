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

package com.hujian.switcher.reactivex;

/**
 * Created by hujian06 on 2017/8/27.
 */

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a Disposable that signals one onNext followed by an onComplete.
 *
 * @param <T> the value type
 */
public final class ScalarDisposable<T> extends AtomicInteger
        implements QueueDisposable<T>, Runnable {

    private final Observer<? super T> observer;

    private final T value;

    private static final int START = 0;
    private static final int FUSED = 1;
    private static final int ON_NEXT = 2;
    private static final int ON_COMPLETE = 3;

    public ScalarDisposable(Observer<? super T> observer, T value) {
        this.observer = observer;
        this.value = value;
    }

    @Override
    public boolean offer(T value) {
        throw new UnsupportedOperationException("Should not be called!");
    }

    @Override
    public boolean offer(T v1, T v2) {
        throw new UnsupportedOperationException("Should not be called!");
    }

    @Override
    public T poll() throws Exception {
        if (get() == FUSED) {
            lazySet(ON_COMPLETE);
            return value;
        }
        return null;
    }

    @Override
    public boolean isEmpty() {
        return get() != FUSED;
    }

    @Override
    public void clear() {
        lazySet(ON_COMPLETE);
    }

    @Override
    public void dispose() {
        set(ON_COMPLETE);
    }

    @Override
    public boolean isDisposed() {
        return get() == ON_COMPLETE;
    }

    @Override
    public int requestFusion(int mode) {
        if ((mode & SYNC) != 0) {
            lazySet(FUSED);
            return SYNC;
        }
        return NONE;
    }

    @Override
    public void run() {
        if (get() == START && compareAndSet(START, ON_NEXT)) {
            observer.onNext(value);
            if (get() == ON_NEXT) {
                lazySet(ON_COMPLETE);
                observer.onComplete();
            }
        }
    }
}
