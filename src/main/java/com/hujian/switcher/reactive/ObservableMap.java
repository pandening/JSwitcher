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

import com.hujian.switcher.reactive.aux.ObjectHelper;
import com.hujian.switcher.reactive.functions.Function;

/**
 * Created by hujian06 on 2017/8/28.
 * for each income item,apply the mapper,then emit to observer
 */
public final class ObservableMap<T, U> extends AbstractObservableWithUpstream<T, U> {
    private final Function<? super T, ? extends U> function; // the function (mapper)

    /**
     * try to get the function 'mapper'
     * @param source the source observable
     * @param function the mapper
     */
    public ObservableMap(ObservableSource<T> source, Function<? super T, ? extends U> function) {
        super(source);
        this.function = function;
    }

    /**
     * run subscribe,remember that -> the subscribe is "customer" by "create" method
     *
     * @param t the observer
     */
    @Override
    public void subscribeActual(Observer<? super U> t) {
        //try to run by 'auto' , the subscribe is called by observer normally
        //but for this position,the mapper need to do something between observer
        //and observable. the "mapper" is the actually worker
        source.subscribe(new MapObserver<T, U>(t, function));
    }


    static final class MapObserver<T, U> extends BasicFuseableObserver<T, U> {
        final Function<? super T, ? extends U> mapper;

        MapObserver(Observer<? super U> actual, Function<? super T, ? extends U> mapper) {
            super(actual);
            this.mapper = mapper;
        }

        @Override
        public void onNext(T t) {
            if (done) {
                return;
            }
            if (sourceMode != NONE) {
                actual.onNext(null);
                return;
            }
            U v;
            try {
                v = ObjectHelper.requireNonNull(mapper.apply(t),
                        "The mapper function returned a null value.");
            } catch (Throwable ex) {
                fail(ex);
                return;
            }
            actual.onNext(v);
        }

        @Override
        public int requestFusion(int mode) {
            return transitiveBoundaryFusion(mode);
        }

        @Override
        public U poll() throws Exception {
            T t = qs.poll();
            return t != null ? ObjectHelper.<U>requireNonNull(mapper.apply(t),
                    "The mapper function returned a null value.") : null;
        }
    }
}
