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

import com.hujian.switcher.reactive.flowable.aux.AtomicThrowable;
import com.hujian.switcher.reactive.flowable.aux.SubscriptionHelper;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by hujian06 on 2017/8/28.
 */
public class StrictSubscriber<T> extends AtomicInteger
        implements FlowableSubscriber<T>, Subscription {

    private final Subscriber<? super T> actual;

    private final AtomicThrowable error;

    private final AtomicLong requested;

    private final AtomicReference<Subscription> s;

    private final AtomicBoolean once;

    private volatile boolean done;

    public StrictSubscriber(Subscriber<? super T> actual) {
        this.actual = actual;
        this.error = new AtomicThrowable();
        this.requested = new AtomicLong();
        this.s = new AtomicReference<Subscription>();
        this.once = new AtomicBoolean();
    }

    @Override
    public void request(long n) {
        if (n <= 0) {
            cancel();
            onError(new IllegalArgumentException("violated: positive request amount required but it was " + n));
        } else {
            SubscriptionHelper.deferredRequest(s, requested, n);
        }
    }

    @Override
    public void cancel() {
        if (!done) {
            SubscriptionHelper.cancel(s);
        }
    }

    @Override
    public void onSubscribe(Subscription s) {
        if (once.compareAndSet(false, true)) {

            actual.onSubscribe(this);

            SubscriptionHelper.deferredSetOnce(this.s, requested, s);
        } else {
            s.cancel();
            cancel();
            onError(new IllegalStateException("violated: onSubscribe must be called at most once"));
        }
    }

    @Override
    public void onNext(T t) {
        HalfSerializer.onNext(actual, t, this, error);
    }

    @Override
    public void onError(Throwable t) {
        done = true;
        HalfSerializer.onError(actual, t, this, error);
    }

    @Override
    public void onComplete() {
        done = true;
        HalfSerializer.onComplete(actual, this, error);
    }
}

