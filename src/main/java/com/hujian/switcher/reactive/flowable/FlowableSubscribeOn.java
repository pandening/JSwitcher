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

import com.hujian.switcher.reactive.flowable.aux.BackpressureHelper;
import com.hujian.switcher.reactive.flowable.aux.SubscriptionHelper;
import com.hujian.switcher.ScheduleHooks;
import com.hujian.switcher.schedulers.core.Scheduler;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by hujian06 on 2017/8/29.
 */
/**
 * Subscribes to the source Flowable on the specified Scheduler and makes
 * sure downstream requests are scheduled there as well.
 *
 * @param <T> the value type emitted
 */
public final class FlowableSubscribeOn<T> extends AbstractFlowableWithUpstream<T , T> {

    private final Scheduler scheduler;

    private final boolean nonScheduledRequests;

    public FlowableSubscribeOn(Flowable<T> source, Scheduler scheduler, boolean nonScheduledRequests) {
        super(source);
        this.scheduler = scheduler;
        this.nonScheduledRequests = nonScheduledRequests;
    }

    @Override
    public void subscribeActual(final Subscriber<? super T> s) {
        Scheduler.Worker w = scheduler.createWorker();
        final SubscribeOnSubscriber<T> sos = new SubscribeOnSubscriber<T>(s, w, source, nonScheduledRequests);
        s.onSubscribe(sos);

        try {
            w.schedule(sos);
        } catch (ExecutionException | InterruptedException e) {
            ScheduleHooks.onError(e);
        }
    }

    static final class SubscribeOnSubscriber<T> extends AtomicReference<Thread>
            implements FlowableSubscriber<T>, Subscription, Runnable {

        final Subscriber<? super T> actual;

        final Scheduler.Worker worker;

        final AtomicReference<Subscription> s;

        final AtomicLong requested;

        final boolean nonScheduledRequests;

        Publisher<T> source;

        SubscribeOnSubscriber(Subscriber<? super T> actual, Scheduler.Worker worker, Publisher<T> source, boolean requestOn) {
            this.actual = actual;
            this.worker = worker;
            this.source = source;
            this.s = new AtomicReference<Subscription>();
            this.requested = new AtomicLong();
            this.nonScheduledRequests = !requestOn;
        }

        @Override
        public void run() {
            lazySet(Thread.currentThread());
            Publisher<T> src = source;
            source = null;
            src.subscribe(this);
        }

        @Override
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.setOnce(this.s, s)) {
                long r = requested.getAndSet(0L);
                if (r != 0L) {
                    requestUpstream(r, s);
                }
            }
        }

        @Override
        public void onNext(T t) {
            actual.onNext(t);
        }

        @Override
        public void onError(Throwable t) {
            actual.onError(t);
            worker.dispose();
        }

        @Override
        public void onComplete() {
            actual.onComplete();
            worker.dispose();
        }

        @Override
        public void request(final long n) {
            if (SubscriptionHelper.validate(n)) {
                Subscription s = this.s.get();
                if (s != null) {
                    requestUpstream(n, s);
                } else {
                    BackpressureHelper.add(requested, n);
                    s = this.s.get();
                    if (s != null) {
                        long r = requested.getAndSet(0L);
                        if (r != 0L) {
                            requestUpstream(r, s);
                        }
                    }
                }
            }
        }

        void requestUpstream(final long n, final Subscription s) {
            if (nonScheduledRequests || Thread.currentThread() == get()) {
                s.request(n);
            } else {
                try {
                    worker.schedule(new Request(s, n));
                } catch (ExecutionException | InterruptedException e) {
                    ScheduleHooks.onError(e);
                }
            }
        }

        @Override
        public void cancel() {
            SubscriptionHelper.cancel(s);
            worker.dispose();
        }

        static final class Request implements Runnable {
            private final Subscription s;
            private final long n;

            Request(Subscription s, long n) {
                this.s = s;
                this.n = n;
            }

            @Override
            public void run() {
                s.request(n);
            }
        }
    }
}
