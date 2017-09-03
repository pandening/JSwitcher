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

package com.hujian.switcher.reactive.flowable;

/**
 * Created by hujian06 on 2017/8/29.
 */

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base class extending AtomicInteger (wip or request accounting) and QueueSubscription (fusion).
 *
 * @param <T> the value type
 */
public abstract class BasicIntQueueSubscription<T> extends AtomicInteger implements QueueSubscription<T> {

    @Override
    public final boolean offer(T e) {
        throw new UnsupportedOperationException("Should not be called!");
    }

    @Override
    public final boolean offer(T v1, T v2) {
        throw new UnsupportedOperationException("Should not be called!");
    }
}