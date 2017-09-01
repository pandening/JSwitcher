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
 * Created by hujian06 on 2017/8/24.
 */

import com.hujian.switcher.reactive.aux.Functions;
import com.hujian.switcher.reactive.aux.ObjectHelper;
import com.hujian.switcher.reactive.aux.ObservableEmpty;
import com.hujian.switcher.reactive.functions.Action;
import com.hujian.switcher.reactive.functions.Consumer;
import com.hujian.switcher.reactive.functions.Function;
import com.hujian.switcher.reactive.functions.Predicate;
import com.hujian.switcher.reactive.functions.ScalarCallable;
import com.hujian.switcher.ScheduleHooks;
import com.hujian.switcher.schedulers.core.Scheduler;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * The Observable class is the non-backpressured, optionally multi-valued base reactive class that
 * offers factory methods, intermediate operators and the ability to consume synchronous
 * and/or asynchronous reactive dataflows.
 * @param <T>
 *            the type of the items emitted by the Observable
 */
public abstract class Observable<T> implements ObservableSource<T> {

    private static final int BUFFER_SIZE = 128; // the buffer size

    /**
     * try to give the next consumer, no  onError, no OnComplete.
     * Subscribes to an ObservableSource and provides a callback to handle the items it emits.
     * @param onNextConsumer onNext consumer
     * @return the dispose
     */
    public final Disposable subscribe(Consumer<? super T> onNextConsumer) {
        return subscribe(onNextConsumer, Functions.ON_ERROR_MISSING, Functions.EMPTY_ACTION, Functions.emptyConsumer());
    }

    /**
     * Subscribes to an ObservableSource and provides callbacks to handle the items it emits
     *
     * @param onNext
     *             the {@code Consumer<T>} you have designed to accept emissions from the ObservableSource
     * @param onError
     *             the {@code Consumer<Throwable>} you have designed to accept any error notification from the
     *             ObservableSource
     * @return a {@link Disposable} reference with which the caller can stop receiving items before
     *         the ObservableSource has finished sending them
     * @throws NullPointerException
     *             if {@code onNext} is null, or
     *             if {@code onError} is null
     */
    public final Disposable subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError) {
        return subscribe(onNext, onError, Functions.EMPTY_ACTION, Functions.emptyConsumer());
    }

    /**
     * Subscribes to an ObservableSource and provides callbacks to handle the items it emits
     *
     * @param onNext
     *             the {@code Consumer<T>} you have designed to accept emissions from the ObservableSource
     * @param onError
     *             the {@code Consumer<Throwable>} you have designed to accept any error notification from the
     *             ObservableSource
     * @param onComplete
     *             the {@code Action} you have designed to accept a completion notification from the
     *             ObservableSource
     * @return a {@link Disposable} reference with which the caller can stop receiving items before
     *         the ObservableSource has finished sending them
     * @throws NullPointerException
     *             if {@code onNext} is null, or
     *             if {@code onError} is null, or
     *             if {@code onComplete} is null
     */
    public final Disposable subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError,
                                      Action onComplete) {
        return subscribe(onNext, onError, onComplete, Functions.emptyConsumer());
    }

    /**
     * Subscribes to an ObservableSource and provides callbacks to handle the items it emits
     *
     * @param onNext
     *             the {@code Consumer<T>} you have designed to accept emissions from the ObservableSource
     * @param onError
     *             the {@code Consumer<Throwable>} you have designed to accept any error notification from the
     *             ObservableSource
     * @param onComplete
     *             the {@code Action} you have designed to accept a completion notification from the
     *             ObservableSource
     * @param onSubscribe
     *             the {@code Consumer} that receives the upstream's Disposable
     * @return a {@link Disposable} reference with which the caller can stop receiving items before
     *         the ObservableSource has finished sending them
     * @throws NullPointerException
     *             if {@code onNext} is null, or
     *             if {@code onError} is null, or
     *             if {@code onComplete} is null
     */
    public final Disposable subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError,
                                      Action onComplete, Consumer<? super Disposable> onSubscribe) {
        ObjectHelper.requireNonNull(onNext, "onNext is null");
        ObjectHelper.requireNonNull(onError, "onError is null");
        ObjectHelper.requireNonNull(onComplete, "onComplete is null");
        ObjectHelper.requireNonNull(onSubscribe, "onSubscribe is null");

        //convert to a magic observer object.
        MagicObserver<T> ls = new MagicObserver<T>(onNext, onError, onComplete, onSubscribe);

        //subscribe.
        subscribe(ls);

        return ls;
    }


    @Override
    public void subscribe(Observer<? super T> observer) {
        Exception exception = null;
        try {
            subscribeActual(observer);
        } catch (Exception e) {
            exception = e;
        } finally {
            if (exception != null) {
                ScheduleHooks.onError(exception);
            }
        }
    }

    /**
     * Operator implementations (both source and intermediate) should implement this method that
     * performs the necessary business logic.
     * There is no need to call any of the plugin hooks on the current Observable instance or
     * the Subscriber.
     * @param observer the incoming Observer, never null
     */
    protected abstract void subscribeActual(Observer<? super T> observer);

    /**
     * create observable
     * @param source the source
     * @param <T> type
     * @return the observable
     */
    public static <T> Observable<T> create(ObservableOnSubscribe<T> source) {
         return new ObservableCreate<T>(source);
    }

    /**
     * Returns an Observable that emits a single item and then completes.
     *
     * To convert any object into an ObservableSource that emits that object, pass that object into the {@code just}
     * method.
     *
     * @param item
     *            the item to emit
     * @param <T>
     *            the type of that item
     * @return an Observable that emits {@code value} as a single item and then completes
     * */
    public static <T> Observable<T> just(T item) {
        ObjectHelper.requireNonNull(item, "The item is null");
        return new ForJustObservable<T>(item);
    }

    /**
     * Converts an Array into an ObservableSource that emits the items in the Array.
     *
     * @param items
     *            the array of elements
     * @param <T>
     *            the type of items in the Array and the type of items to be emitted by the resulting ObservableSource
     * @return an Observable that emits each item in the source Array
     */
    @SuppressWarnings(value = "unchecked")
    public static <T> Observable<T> fromArray(T... items) {
        ObjectHelper.requireNonNull(items, "items is null");
        if (items.length == 0) {
            return (Observable<T>) ObservableEmpty.INSTANCE;
        } else if (items.length == 1) {
            return just(items[0]);
        }

        return new ForFromArrayObservable<T>(items);
    }

    /**
     * Converts an {@link Iterable} sequence into an ObservableSource that emits the items in the sequence.
     *
     * @param source
     *            the source {@link Iterable} sequence
     * @param <T>
     *            the type of items in the {@link Iterable} sequence and the type of items to be emitted by the
     *            resulting ObservableSource
     * @return an Observable that emits each item in the source {@link Iterable} sequence
     */
    public static <T> Observable<T> fromIterable(Iterable<? extends T> source) {
        ObjectHelper.requireNonNull(source, "source is null");
        return new ForFromIterableObservable<T>(source);
    }

    /**
     * Converts a {@link Future} into an ObservableSource, with a timeout on the Future.
     *
     * @param future
     *            the source {@link Future}
     * @param timeout
     *            the maximum time to wait before calling {@code get}
     * @param unit
     *            the {@link TimeUnit} of the {@code timeout} argument
     * @param <T>
     *            the type of object that the {@link Future} returns, and also the type of item to be emitted by
     *            the resulting ObservableSource
     * @return an Observable that emits the item from the source {@link Future}
     */
    public static <T> Observable<T> fromFuture(Future<? extends T> future, long timeout, TimeUnit unit) {
        return new ForFromFutureObservable<T>(future, timeout, unit);
    }

    /**
     * Returns an Observable that applies a specified function to each item emitted by the source ObservableSource and
     * emits the results of these function applications.
     *
     * @param <R> the output type
     * @param mapper
     *            a function to apply to each item emitted by the ObservableSource
     * @return an Observable that emits the items from the source ObservableSource, transformed by the specified
     *         function
     */
    public final <R> Observable<R> map(Function<? super T, ? extends R> mapper) {
        ObjectHelper.requireNonNull(mapper, "mapper is null");

        return new ObservableMap<T, R>(this, mapper);
    }

    /**
     * Returns an Observable that emits items based on applying a function that you supply to each item emitted
     * by the source ObservableSource, where that function returns an ObservableSource, and then merging those resulting
     * ObservableSources and emitting the results of this merger.
     *
     * @param <R> the value type of the inner ObservableSources and the output type
     * @param mapper
     *            a function that, when applied to an item emitted by the source ObservableSource, returns an
     *            ObservableSource
     * @return an Observable that emits the result of applying the transformation function to each item emitted
     *         by the source ObservableSource and merging the results of the ObservableSources obtained from this
     *         transformation
     */
    public final <R> Observable<R> flatMap(Function<? super T, ? extends ObservableSource<? extends R>> mapper) {
        return flatMap(mapper, false);
    }

    /**
     * Returns an Observable that emits items based on applying a function that you supply to each item emitted
     * by the source ObservableSource, where that function returns an ObservableSource, and then merging those resulting
     * ObservableSources and emitting the results of this merger.
     *
     * @param <R> the value type of the inner ObservableSources and the output type
     * @param mapper
     *            a function that, when applied to an item emitted by the source ObservableSource, returns an
     *            ObservableSource
     * @param delayErrors
     *            if true, exceptions from the current Observable and all inner ObservableSources are delayed until all of them terminate
     *            if false, the first one signalling an exception will terminate the whole sequence immediately
     * @return an Observable that emits the result of applying the transformation function to each item emitted
     *         by the source ObservableSource and merging the results of the ObservableSources obtained from this
     *         transformation
     */
    public final <R> Observable<R> flatMap(Function<? super T, ? extends ObservableSource<? extends R>> mapper, boolean delayErrors) {
        return flatMap(mapper, delayErrors, Integer.MAX_VALUE);
    }

    /**
     * Returns an Observable that emits items based on applying a function that you supply to each item emitted
     * by the source ObservableSource, where that function returns an ObservableSource, and then merging those resulting
     * ObservableSources and emitting the results of this merger, while limiting the maximum number of concurrent
     * subscriptions to these ObservableSources.
     *
     * @param <R> the value type of the inner ObservableSources and the output type
     * @param mapper
     *            a function that, when applied to an item emitted by the source ObservableSource, returns an
     *            ObservableSource
     * @param maxConcurrency
     *         the maximum number of ObservableSources that may be subscribed to concurrently
     * @param delayErrors
     *            if true, exceptions from the current Observable and all inner ObservableSources are delayed until all of them terminate
     *            if false, the first one signalling an exception will terminate the whole sequence immediately
     * @return an Observable that emits the result of applying the transformation function to each item emitted
     *         by the source ObservableSource and merging the results of the ObservableSources obtained from this
     *         transformation
     */
    public final <R> Observable<R> flatMap(Function<? super T, ? extends ObservableSource<? extends R>> mapper, boolean delayErrors, int maxConcurrency) {
        return flatMap(mapper, delayErrors, maxConcurrency, 1024);
    }

    /**
     * Returns an Observable that emits items based on applying a function that you supply to each item emitted
     * by the source ObservableSource, where that function returns an ObservableSource, and then merging those resulting
     * ObservableSources and emitting the results of this merger, while limiting the maximum number of concurrent
     * subscriptions to these ObservableSources.
     *
     * @param <R> the value type of the inner ObservableSources and the output type
     * @param mapper
     *            a function that, when applied to an item emitted by the source ObservableSource, returns an
     *            ObservableSource
     * @param maxConcurrency
     *         the maximum number of ObservableSources that may be subscribed to concurrently
     * @param delayErrors
     *            if true, exceptions from the current Observable and all inner ObservableSources are delayed until all of them terminate
     *            if false, the first one signalling an exception will terminate the whole sequence immediately
     * @param bufferSize
     *            the number of elements to prefetch from each inner ObservableSource
     * @return an Observable that emits the result of applying the transformation function to each item emitted
     *         by the source ObservableSource and merging the results of the ObservableSources obtained from this
     *         transformation
     */
    @SuppressWarnings(value = "unchecked")
    public final <R> Observable<R> flatMap(Function<? super T, ? extends ObservableSource<? extends R>> mapper,
                                           boolean delayErrors, int maxConcurrency, int bufferSize) {
        ObjectHelper.requireNonNull(mapper, "mapper is null");
        ObjectHelper.verifyPositive(maxConcurrency, "maxConcurrency");
        ObjectHelper.verifyPositive(bufferSize, "bufferSize");
        if (this instanceof ScalarCallable) {
            @SuppressWarnings("unchecked")
            T v = ((ScalarCallable<T>)this).call();
            if (v == null) {
                return (Observable<R>) ObservableEmpty.INSTANCE;
            }
            return  new ScalarXMapObservable<>(v,mapper);
        }
        return new ObservableFlatMap<T, R>(this, mapper, delayErrors, maxConcurrency, bufferSize);
    }

    /**
     * Filters items emitted by an ObservableSource by only emitting those that satisfy a specified predicate.
     * @param predicate
     *            a function that evaluates each item emitted by the source ObservableSource, returning {@code true}
     *            if it passes the filter
     * @return an Observable that emits only those items emitted by the source ObservableSource that the filter
     *         evaluates as {@code true}
     */
    public final Observable<T> filter(Predicate<? super T> predicate) {
        ObjectHelper.requireNonNull(predicate, "predicate is null");

        return new ObservableFilter<T>(this, predicate);
    }

    /**
     * Asynchronously subscribes Observers to this ObservableSource on the specified {@link Scheduler}.
     *
     * @param scheduler
     *            the {@link Scheduler} to perform subscription actions on
     * @return the source ObservableSource modified so that its subscriptions happen on the
     *         specified {@link Scheduler}
     */
    public final Observable<T> subscribeOn(Scheduler scheduler) {
        ObjectHelper.requireNonNull(scheduler, "scheduler is null");
        return new ObservableSubscribeOn<T>(this, scheduler);
    }

    /**
     * Modifies an ObservableSource to perform its emissions and notifications on a specified {@link Scheduler},
     *
     * @param scheduler
     *            the {@link Scheduler} to notify {@link Observer}s on
     * @return the source ObservableSource modified so that its {@link Observer}s are notified on the specified
     *         {@link Scheduler}
     */
    public final Observable<T> observeOn(Scheduler scheduler) {
        return observeOn(scheduler, false, BUFFER_SIZE);
    }

    /**
     * Modifies an ObservableSource to perform its emissions and notifications on a specified {@link Scheduler},
     * asynchronously with an unbounded buffer with BUFFER_SIZE "island size" and optionally delays onError notifications.
     *
     * @param scheduler
     *            the {@link Scheduler} to notify {@link Observer}s on
     * @param delayError
     *            indicates if the onError notification may not cut ahead of onNext notification on the other side of the
     *            scheduling boundary. If true a sequence ending in onError will be replayed in the same order as was received
     *            from upstream
     * @return the source ObservableSource modified so that its {@link Observer}s are notified on the specified
     *         {@link Scheduler}
     */
    public final Observable<T> observeOn(Scheduler scheduler, boolean delayError) {
        return observeOn(scheduler, delayError, BUFFER_SIZE);
    }

    /**
     * Modifies an ObservableSource to perform its emissions and notifications on a specified {@link Scheduler},
     * asynchronously with an unbounded buffer of configurable "island size" and optionally delays onError notifications.
     * @param scheduler
     *            the {@link Scheduler} to notify {@link Observer}s on
     * @param delayError
     *            indicates if the onError notification may not cut ahead of onNext notification on the other side of the
     *            scheduling boundary. If true a sequence ending in onError will be replayed in the same order as was received
     *            from upstream
     * @param bufferSize the size of the buffer.
     * @return the source ObservableSource modified so that its {@link Observer}s are notified on the specified
     *         {@link Scheduler}
     */
    public final Observable<T> observeOn(Scheduler scheduler, boolean delayError, int bufferSize) {
        ObjectHelper.requireNonNull(scheduler, "scheduler is null");
        ObjectHelper.verifyPositive(bufferSize, "bufferSize");
        return new ObservableObserveOn<T>(this, scheduler, delayError, bufferSize);
    }

}