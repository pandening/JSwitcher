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

package com.hujian.switcher.reactive;

/**
 * Created by hujian06 on 2017/8/28.
 */
/**
 * Base class for operators with a source consumable.
 *
 * @param <T> the input source type
 * @param <U> the output type
 */
abstract class AbstractObservableWithUpstream<T, U> extends Observable<U> implements HasUpstreamObservableSource<T> {

    /** The source consumable Observable. */
    protected final ObservableSource<T> source;

    /**
     * Constructs the ObservableSource with the given consumable.
     * @param source the consumable Observable
     */
    AbstractObservableWithUpstream(ObservableSource<T> source) {
        this.source = source;
    }

    @Override
    public final ObservableSource<T> source() {
        return source;
    }

}
