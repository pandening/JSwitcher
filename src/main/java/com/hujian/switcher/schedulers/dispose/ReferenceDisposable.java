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

package com.hujian.switcher.schedulers.dispose;

/**
 * Created by hujian06 on 2017/8/29.
 */

import com.hujian.switcher.reactive.Disposable;
import com.hujian.switcher.reactive.aux.ObjectHelper;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Base class for Disposable containers that manage some other type that
 * has to be run when the container is disposed.
 *
 * @param <T> the type contained
 */
abstract class ReferenceDisposable<T> extends AtomicReference<T> implements Disposable {

    ReferenceDisposable(T value) {
        super(ObjectHelper.requireNonNull(value, "value is null"));
    }

    protected abstract void onDisposed(T value);

    @Override
    public final void dispose() {
        T value = get();
        if (value != null) {
            value = getAndSet(null);
            if (value != null) {
                onDisposed(value);
            }
        }
    }

    @Override
    public final boolean isDisposed() {
        return get() == null;
    }
}
