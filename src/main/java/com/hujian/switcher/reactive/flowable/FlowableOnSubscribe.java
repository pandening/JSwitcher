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
/**
 * A functional interface that has a {@code subscribe()} method that receives
 * an instance of a FlowableEmitter instance that allows pushing
 * events in a backpressure-safe and cancellation-safe manner.
 *
 * @param <T> the value type pushed
 */
public interface FlowableOnSubscribe<T> {

    /**
     * Called for each Subscriber that subscribes.
     * @param e the safe emitter instance, never null
     * @throws Exception on error
     */
    void subscribe(FlowableEmitter<T> e) throws Exception;
}
