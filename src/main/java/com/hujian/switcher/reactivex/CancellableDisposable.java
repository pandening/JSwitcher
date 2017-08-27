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
 * Created by hujian06 on 2017/8/25.
 */

import com.hujian.switcher.reactivex.functions.Cancellable;

import java.util.concurrent.atomic.AtomicReference;

/**
 * A disposable container that wraps a Cancellable instance.
 * Watch out for the AtomicReference API leak!
 */
public final class CancellableDisposable extends AtomicReference<Cancellable>
        implements Disposable {

    public CancellableDisposable(Cancellable cancellable) {
        super(cancellable);
    }

    @Override
    public boolean isDisposed() {
        return get() == null;
    }

    @Override
    public void dispose() {
        if (get() != null) {
            Cancellable c = getAndSet(null);
            if (c != null) {
                try {
                    c.cancel();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}

