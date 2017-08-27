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

package com.hujian.switcher.reactivex.aux;

/**
 * Created by hujian06 on 2017/8/27.
 */

import com.hujian.switcher.reactivex.Observer;
import com.hujian.switcher.reactivex.QueueDisposable;

/**
 * Represents a stateless empty Disposable that reports being always
 * empty and disposed.
 * <p>It is also async-fuseable but empty all the time.
 * <p>Since EmptyDisposable implements QueueDisposable and is empty,
 * don't use it in tests and then signal onNext with it;
 * use Disposables.empty() instead.
 */
public enum EmptyDisposable implements QueueDisposable<Object> {
    /**
     * Since EmptyDisposable implements QueueDisposable and is empty,
     * don't use it in tests and then signal onNext with it;
     * use Disposables.empty() instead.
     */
    INSTANCE,
    /**
     * An empty disposable that returns false for isDisposed.
     */
    NEVER;

    @Override
    public void dispose() {
        // no-op
    }

    @Override
    public boolean isDisposed() {
        return this == INSTANCE;
    }

    public static void complete(Observer<?> s) {
        s.onSubscribe(INSTANCE);
        s.onComplete();
    }

    public static void error(Throwable e, Observer<?> s) {
        s.onSubscribe(INSTANCE);
        s.onError(e);
    }


    @Override
    public boolean offer(Object value) {
        throw new UnsupportedOperationException("Should not be called!");
    }

    @Override
    public boolean offer(Object v1, Object v2) {
        throw new UnsupportedOperationException("Should not be called!");
    }

    @Override
    public Object poll() throws Exception {
        return null; // always empty
    }

    @Override
    public boolean isEmpty() {
        return true; // always empty
    }

    @Override
    public void clear() {
        // nothing to do
    }

    @Override
    public int requestFusion(int mode) {
        return mode & ASYNC;
    }

}

