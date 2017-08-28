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

import com.hujian.switcher.reactivex.aux.ObjectHelper;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by hujian06 on 2017/8/28.
 */
public final class ForFromFutureObservable<T> extends Observable<T> {
    private final Future<? extends T> future;
    private final long timeout;
    private final TimeUnit unit;

    public ForFromFutureObservable(Future<? extends T> future, long timeout, TimeUnit unit) {
        this.future = future;
        this.timeout = timeout;
        this.unit = unit;
    }

    @Override
    public void subscribeActual(Observer<? super T> s) {
        DeferredScalarDisposable<T> d = new DeferredScalarDisposable<T>(s);
        s.onSubscribe(d);
        if (!d.isDisposed()) {
            T v;
            try {
                v = ObjectHelper.requireNonNull(unit != null ? future.get(timeout, unit) : future.get(), "Future returned null");
            } catch (Throwable ex) {
                if (!d.isDisposed()) {
                    s.onError(ex);
                }
                return;
            }
            d.complete(v);
        }
    }
}
