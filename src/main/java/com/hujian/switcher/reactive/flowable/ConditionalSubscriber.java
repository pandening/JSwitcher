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
 * A Subscriber with an additional onNextIf(T) method that
 * tells the caller the specified value has been accepted or
 * not.
 *
 * This allows certain queue-drain or source-drain operators
 * to avoid requesting 1 on behalf of a dropped value.
 *
 * @param <T> the value type
 */
public interface ConditionalSubscriber<T> extends FlowableSubscriber<T> {
    /**
     * Conditionally takes the value.
     * @param t the value to deliver
     * @return true if the value has been accepted, false if the value has been rejected
     */
    boolean tryOnNext(T t);
}
