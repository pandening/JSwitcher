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
 * Created by hujian06 on 2017/8/28.
 */

import com.hujian.switcher.reactivex.aux.EmptyDisposable;
import com.hujian.switcher.reactivex.aux.ObjectHelper;
import com.hujian.switcher.reactivex.functions.Function;

import java.util.concurrent.Callable;

/**
 * Maps a scalar value to an ObservableSource and subscribes to it.
 *
 * @param <T> the scalar value type
 * @param <R> the mapped ObservableSource's element type.
 */
final class ScalarXMapObservable<T, R> extends Observable<R> {

    final T value;

    private final Function<? super T, ? extends ObservableSource<? extends R>> mapper;

    ScalarXMapObservable(T value,
                         Function<? super T, ? extends ObservableSource<? extends R>> mapper) {
        this.value = value;
        this.mapper = mapper;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void subscribeActual(Observer<? super R> s) {
        ObservableSource<? extends R> other;
        try {
            other = ObjectHelper.requireNonNull(mapper.apply(value), "The mapper returned a null ObservableSource");
        } catch (Throwable e) {
            EmptyDisposable.error(e, s);
            return;
        }
        if (other instanceof Callable) {
            R u;

            try {
                u = ((Callable<R>)other).call();
            } catch (Throwable ex) {
                EmptyDisposable.error(ex, s);
                return;
            }

            if (u == null) {
                EmptyDisposable.complete(s);
                return;
            }
            ScalarDisposable<R> sd = new ScalarDisposable<R>(s, u);
            s.onSubscribe(sd);
            sd.run();
        } else {
            other.subscribe(s);
        }
    }
}
