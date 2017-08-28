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

package com.hujian.switcher.reactive.flowable;

/**
 * Created by hujian06 on 2017/8/28.
 */

import com.hujian.switcher.reactive.Disposable;
import com.hujian.switcher.reactive.aux.DisposableHelper;

import java.util.concurrent.atomic.AtomicReference;

/**
 * A Disposable container that allows updating/replacing a Disposable
 * atomically and with respect of disposing the container itself.
 *
 */
public final class SequentialDisposable extends AtomicReference<Disposable> implements Disposable {

    /**
     * Constructs an empty SequentialDisposable.
     */
    public SequentialDisposable() {
        // nothing to do
    }

    /**
     * Construct a SequentialDisposable with the initial Disposable provided.
     * @param initial the initial disposable, null allowed
     */
    public SequentialDisposable(Disposable initial) {
        lazySet(initial);
    }

    /**
     * Atomically: set the next disposable on this container and dispose the previous
     * one (if any) or dispose next if the container has been disposed.
     * @param next the Disposable to set, may be null
     * @return true if the operation succeeded, false if the container has been disposed
     */
    public boolean update(Disposable next) {
        return DisposableHelper.set(this, next);
    }

    /**
     * Atomically: set the next disposable on this container but don't dispose the previous
     * one (if any) or dispose next if the container has been disposed.
     * @param next the Disposable to set, may be null
     * @return true if the operation succeeded, false if the container has been disposed
     */
    public boolean replace(Disposable next) {
        return DisposableHelper.replace(this, next);
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
