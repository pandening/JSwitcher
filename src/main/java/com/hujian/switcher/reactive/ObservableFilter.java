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

import com.hujian.switcher.reactive.functions.Predicate;

/**
 * Created by hujian06 on 2017/8/28.
 */
public final class ObservableFilter<T> extends AbstractObservableWithUpstream<T, T> {
    private final Predicate<? super T> predicate;
    public ObservableFilter(ObservableSource<T> source, Predicate<? super T> predicate) {
        super(source);
        this.predicate = predicate;
    }

    @Override
    public void subscribeActual(Observer<? super T> s) {
        source.subscribe(new FilterObserver<T>(s, predicate));
    }

    static final class FilterObserver<T> extends BasicFuseableObserver<T, T> {
        final Predicate<? super T> filter;

        FilterObserver(Observer<? super T> actual, Predicate<? super T> filter) {
            super(actual);
            this.filter = filter;
        }

        /**
         * actual do the filter work.
         * @param t the item emitted by the Observable
         */
        @Override
        public void onNext(T t) {
            if (sourceMode == NONE) {
                boolean b;
                try {
                    b = filter.test(t);
                } catch (Throwable e) {
                    fail(e);
                    return;
                }
                if (b) { // judge and action
                    actual.onNext(t);
                }
            } else {
                actual.onNext(null);
            }
        }

        @Override
        public int requestFusion(int mode) {
            return transitiveBoundaryFusion(mode);
        }

        @Override
        public T poll() throws Exception {
            for (;;) {
                T v = qs.poll();
                if (v == null || filter.test(v)) {
                    return v;
                }
            }
        }
    }
}
