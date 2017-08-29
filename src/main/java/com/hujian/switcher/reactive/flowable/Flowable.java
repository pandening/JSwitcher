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

import com.hujian.switcher.reactive.Disposable;
import com.hujian.switcher.reactive.aux.Functions;
import com.hujian.switcher.reactive.aux.ObjectHelper;
import com.hujian.switcher.reactive.functions.Action;
import com.hujian.switcher.reactive.functions.Consumer;
import com.hujian.switcher.reactive.functions.Function;
import com.hujian.switcher.schedulers.core.Scheduler;

/**
 * Created by hujian06 on 2017/8/28.
 */
public abstract class Flowable<T> implements Publisher<T> {

    private static final int BUFFER_SIZE = 128; // the buffer size

    /**
     * Subscribes to a Publisher and provides a callback to handle the items it emits.
     *
     * @param onNext
     *             the {@code Consumer<T>} you have designed to accept emissions from the Publisher
     * @return a Disposable reference with which the caller can stop receiving items before
     *         the Publisher has finished sending them
     * @throws NullPointerException
     *             if {@code onNext} is null
     */
    public final Disposable subscribe(Consumer<? super T> onNext) {
        return subscribe(onNext, Functions.ON_ERROR_MISSING, Functions.EMPTY_ACTION, RequestMax.INSTANCE);
    }

    /**
     * Subscribes to a Publisher and provides callbacks to handle the items it emits and any error
     * notification it issues.
     *
     * @param onNext
     *             the {@code Consumer<T>} you have designed to accept emissions from the Publisher
     * @param onError
     *             the {@code Consumer<Throwable>} you have designed to accept any error notification from the
     *             Publisher
     * @return a {@link Disposable} reference with which the caller can stop receiving items before
     *         the Publisher has finished sending them
     * @throws NullPointerException
     *             if {@code onNext} is null, or
     *             if {@code onError} is null
     */
    public final Disposable subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError) {
        return subscribe(onNext, onError, Functions.EMPTY_ACTION, RequestMax.INSTANCE);
    }

    /**
     * Subscribes to a Publisher and provides callbacks to handle the items it emits and any error or
     * completion notification it issues.
     *
     * @param onNext
     *             the {@code Consumer<T>} you have designed to accept emissions from the Publisher
     * @param onError
     *             the {@code Consumer<Throwable>} you have designed to accept any error notification from the
     *             Publisher
     * @param onComplete
     *             the {@code Action} you have designed to accept a completion notification from the
     *             Publisher
     * @return a {@link Disposable} reference with which the caller can stop receiving items before
     *         the Publisher has finished sending them
     * @throws NullPointerException
     *             if {@code onNext} is null, or
     *             if {@code onError} is null, or
     *             if {@code onComplete} is null
     */
    public final Disposable subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Action onComplete) {
        return subscribe(onNext, onError, onComplete, RequestMax.INSTANCE);
    }

    /**
     * Subscribes to a Publisher and provides callbacks to handle the items it emits and any error or
     * completion notification it issues.
     *
     * @param onNext
     *             the {@code Consumer<T>} you have designed to accept emissions from the Publisher
     * @param onError
     *             the {@code Consumer<Throwable>} you have designed to accept any error notification from the
     *             Publisher
     * @param onComplete
     *             the {@code Action} you have designed to accept a completion notification from the
     *             Publisher
     * @param onSubscribe
     *             the {@code Consumer} that receives the upstream's Subscription
     * @return a {@link Disposable} reference with which the caller can stop receiving items before
     *         the Publisher has finished sending them
     * @throws NullPointerException
     *             if {@code onNext} is null, or
     *             if {@code onError} is null, or
     *             if {@code onComplete} is null
     */
    public final Disposable subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError,
                                      Action onComplete, Consumer<? super Subscription> onSubscribe) {
        ObjectHelper.requireNonNull(onNext, "onNext is null");
        ObjectHelper.requireNonNull(onError, "onError is null");
        ObjectHelper.requireNonNull(onComplete, "onComplete is null");
        ObjectHelper.requireNonNull(onSubscribe, "onSubscribe is null");

        LambdaSubscriber<T> ls = new LambdaSubscriber<T>(onNext, onError, onComplete, onSubscribe);

        subscribe(ls);

        return ls;
    }

    public final void subscribe(FlowableSubscriber<? super T> s) {
        ObjectHelper.requireNonNull(s, "s is null");
        try {
            subscribeActual(s);
        } catch (NullPointerException e) { // NOPMD
            throw e;
        } catch (Throwable e) {
            NullPointerException npe = new NullPointerException("Actually not, but can't throw other exceptions due to RS");
            npe.initCause(e);
            throw npe;
        }
    }

    @SuppressWarnings(value = "unchecked")
    @Override
    public final void subscribe(Subscriber<? super T> s) {
        if (s instanceof FlowableSubscriber) {
            subscribe((FlowableSubscriber<? super T>)s);
        } else {
            ObjectHelper.requireNonNull(s, "s is null");
            subscribe(new StrictSubscriber<T>(s));
        }
    }

    /**
     * Operator implementations (both source and intermediate) should implement this method that
     * performs the necessary business logic.
     * There is no need to call any of the plugin hooks on the current Flowable instance or
     * the Subscriber.
     * @param s the incoming Subscriber, never null
     */
    protected abstract void subscribeActual(Subscriber<? super T> s);

    /**
     * Asynchronously subscribes Subscribers to this Publisher on the specified {@link Scheduler}.
     *
     * @param scheduler
     *            the {@link Scheduler} to perform subscription actions on
     * @return the source Publisher modified so that its subscriptions happen on the
     *         specified {@link Scheduler}
     */
    public final Flowable<T> subscribeOn(Scheduler scheduler) {
        ObjectHelper.requireNonNull(scheduler, "scheduler is null");
        return subscribeOn(scheduler, !(this instanceof FlowableCreate));
    }

    /**
     * Asynchronously subscribes Subscribers to this Publisher on the specified {@link Scheduler}
     * optionally reroutes requests from other threads to the same {@link Scheduler} thread.
     *
     * If there is a {@link #create(FlowableOnSubscribe, BackpressureStrategy)} type source up in the
     * chain, it is recommended to have {@code requestOn} false to avoid same-pool deadlock
     * because requests may pile up behind an eager/blocking emitter.
     *
     * @param scheduler
     *            the {@link Scheduler} to perform subscription actions on
     * @param requestOn if true, requests are rerouted to the given Scheduler as well (strong pipelining)
     *                  if false, requests coming from any thread are simply forwarded to
     *                  the upstream on the same thread (weak pipelining)
     * @return the source Publisher modified so that its subscriptions happen on the
     *         specified {@link Scheduler}
     */
    public final Flowable<T> subscribeOn(Scheduler scheduler, boolean requestOn) {
        ObjectHelper.requireNonNull(scheduler, "scheduler is null");
        return new FlowableSubscribeOn<T>(this, scheduler, requestOn);
    }

    /**
     * Modifies a Publisher to perform its emissions and notifications on a specified {@link Scheduler},
     * asynchronously with a bounded buffer of BUFFER_SIZE slots.
     *
     * @param scheduler
     *            the {@link Scheduler} to notify {@link Subscriber}s on
     * @return the source Publisher modified so that its {@link Subscriber}s are notified on the specified
     *         {@link Scheduler}
     */
    public final Flowable<T> observeOn(Scheduler scheduler) {
        return observeOn(scheduler, false, BUFFER_SIZE);
    }

    /**
     * Modifies a Publisher to perform its emissions and notifications on a specified {@link Scheduler},
     * asynchronously with a bounded buffer and optionally delays onError notifications.
     *
     * @param scheduler
     *            the {@link Scheduler} to notify {@link Subscriber}s on
     * @param delayError
     *            indicates if the onError notification may not cut ahead of onNext notification on the other side of the
     *            scheduling boundary. If true a sequence ending in onError will be replayed in the same order as was received
     *            from upstream
     * @return the source Publisher modified so that its {@link Subscriber}s are notified on the specified
     *         {@link Scheduler}
     */
    public final Flowable<T> observeOn(Scheduler scheduler, boolean delayError) {
        return observeOn(scheduler, delayError, BUFFER_SIZE);
    }

    /**
     * Modifies a Publisher to perform its emissions and notifications on a specified {@link Scheduler},
     * asynchronously with a bounded buffer of configurable size and optionally delays onError notifications.
     * @param scheduler
     *            the {@link Scheduler} to notify {@link Subscriber}s on
     * @param delayError
     *            indicates if the onError notification may not cut ahead of onNext notification on the other side of the
     *            scheduling boundary. If true a sequence ending in onError will be replayed in the same order as was received
     *            from upstream
     * @param bufferSize the size of the buffer.
     * @return the source Publisher modified so that its {@link Subscriber}s are notified on the specified
     *         {@link Scheduler}
     */
    public final Flowable<T> observeOn(Scheduler scheduler, boolean delayError, int bufferSize) {
        ObjectHelper.requireNonNull(scheduler, "scheduler is null");
        ObjectHelper.verifyPositive(bufferSize, "bufferSize");
        return new FlowableObserveOn<T>(this, scheduler, delayError, bufferSize);
    }

    /**
     * @param <T> the element type
     * @param source the emitter that is called when a Subscriber subscribes to the returned {@code Flowable}
     * @param mode the backpressure mode to apply if the downstream Subscriber doesn't request (fast) enough
     * @return the new Flowable instance
     * @see FlowableOnSubscribe
     * @see BackpressureStrategy
     */
    public static <T> Flowable<T> create(FlowableOnSubscribe<T> source, BackpressureStrategy mode) {
        ObjectHelper.requireNonNull(source, "source is null");
        ObjectHelper.requireNonNull(mode, "mode is null");

        return new FlowableCreate<T>(source, mode);
    }

    /**
     * Returns a Flowable that applies a specified function to each item emitted by the source Publisher and
     * emits the results of these function applications.
     *
     * @param <R> the output type
     * @param mapper
     *            a function to apply to each item emitted by the Publisher
     * @return a Flowable that emits the items from the source Publisher, transformed by the specified
     *         function
     */
    public final <R> Flowable<R> map(Function<? super T, ? extends R> mapper) {
        ObjectHelper.requireNonNull(mapper, "mapper is null");

        return new FlowableMap<T, R>(this, mapper);
    }

    /**
     * RequestMax enum.
     */
    public enum RequestMax implements Consumer<Subscription> {
        INSTANCE;
        @Override
        public void accept(Subscription t) throws Exception {
            t.request(Long.MAX_VALUE);
        }
    }

}
