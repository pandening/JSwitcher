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

package com.hujian.schedulers;

import com.hujian.schedulers.core.Scheduler;
import com.hujian.switcher.reactive.aux.ObjectHelper;
import com.hujian.switcher.reactive.flowable.aux.CompositeException;
import com.hujian.switcher.reactive.flowable.aux.ExceptionHelper;
import com.hujian.switcher.reactive.functions.Consumer;
import com.hujian.switcher.reactive.functions.Function;

import java.util.concurrent.Callable;

/**
 * Created by hujian06 on 2017/8/29.
 */
public final class ScheduleHooks {

    private static volatile Consumer<? super Throwable> errorHandler;

    private static volatile Function<? super Runnable, ? extends Runnable> onScheduleHandler;

    private static volatile Function<? super Callable<Scheduler>, ? extends Scheduler> onInitComputationHandler;

    private static volatile Function<? super Callable<Scheduler>, ? extends Scheduler> onInitSingleHandler;

    private static volatile Function<? super Callable<Scheduler>, ? extends Scheduler> onInitIoHandler;

    private static volatile Function<? super Callable<Scheduler>, ? extends Scheduler> onInitNewThreadHandler;

    private static volatile Function<? super Scheduler, ? extends Scheduler> onComputationHandler;

    private static volatile Function<? super Scheduler, ? extends Scheduler> onSingleHandler;

    private static volatile Function<? super Scheduler, ? extends Scheduler> onIoHandler;

    private static volatile Function<? super Scheduler, ? extends Scheduler> onNewThreadHandler;

    /** Prevents changing the plugins. */
    private static volatile boolean lockdown;

    /**
     * If true, attempting to run a blockingX operation on a (by default)
     * computation or single scheduler will throw an IllegalStateException.
     */
    private static volatile boolean failNonBlockingScheduler;

    /**
     * Prevents changing the plugins from then on.
     * <p>This allows container-like environments to prevent clients
     * messing with plugins.
     */
    public static void lockdown() {
        lockdown = true;
    }

    /**
     * Returns true if the plugins were locked down.
     * @return true if the plugins were locked down
     */
    public static boolean isLockdown() {
        return lockdown;
    }

    /**
     * Enables or disables the blockingX operators to fail
     * with an IllegalStateException on a non-blocking
     * scheduler such as computation or single.
     * @param enable enable or disable the feature
     */
    public static void setFailOnNonBlockingScheduler(boolean enable) {
        if (lockdown) {
            throw new IllegalStateException("ScheduleHooks can't be changed anymore");
        }
        failNonBlockingScheduler = enable;
    }

    /**
     * Returns true if the blockingX operators fail
     * with an IllegalStateException on a non-blocking scheduler
     * such as computation or single.
     * @return true if the blockingX operators fail on a non-blocking scheduler
     */
    public static boolean isFailOnNonBlockingScheduler() {
        return failNonBlockingScheduler;
    }

    /**
     * Returns the current hook function.
     * @return the hook function, may be null
     */
    public static Function<? super Scheduler, ? extends Scheduler> getComputationSchedulerHandler() {
        return onComputationHandler;
    }

    /**
     * Returns the a hook consumer.
     * @return the hook consumer, may be null
     */
    public static Consumer<? super Throwable> getErrorHandler() {
        return errorHandler;
    }

    /**
     * Returns the current hook function.
     * @return the hook function, may be null
     */
    public static Function<? super Callable<Scheduler>, ? extends Scheduler> getInitComputationSchedulerHandler() {
        return onInitComputationHandler;
    }

    /**
     * Returns the current hook function.
     * @return the hook function, may be null
     */
    public static Function<? super Callable<Scheduler>, ? extends Scheduler> getInitIoSchedulerHandler() {
        return onInitIoHandler;
    }

    /**
     * Returns the current hook function.
     * @return the hook function, may be null
     */
    public static Function<? super Callable<Scheduler>, ? extends Scheduler> getInitNewThreadSchedulerHandler() {
        return onInitNewThreadHandler;
    }

    /**
     * Returns the current hook function.
     * @return the hook function, may be null
     */
    public static Function<? super Callable<Scheduler>, ? extends Scheduler> getInitSingleSchedulerHandler() {
        return onInitSingleHandler;
    }

    /**
     * Returns the current hook function.
     * @return the hook function, may be null
     */
    public static Function<? super Scheduler, ? extends Scheduler> getIoSchedulerHandler() {
        return onIoHandler;
    }

    /**
     * Returns the current hook function.
     * @return the hook function, may be null
     */
    public static Function<? super Scheduler, ? extends Scheduler> getNewThreadSchedulerHandler() {
        return onNewThreadHandler;
    }

    /**
     * Returns the current hook function.
     * @return the hook function, may be null
     */
    public static Function<? super Runnable, ? extends Runnable> getScheduleHandler() {
        return onScheduleHandler;
    }

    /**
     * Returns the current hook function.
     * @return the hook function, may be null
     */
    public static Function<? super Scheduler, ? extends Scheduler> getSingleSchedulerHandler() {
        return onSingleHandler;
    }

    /**
     * Calls the associated hook function.
     * @param defaultScheduler a {@link Callable} which returns the hook's input value
     * @return the value returned by the hook, not null
     * @throws NullPointerException if the callable parameter or its result are null
     */
    public static Scheduler initComputationScheduler(Callable<Scheduler> defaultScheduler) {
        ObjectHelper.requireNonNull(defaultScheduler, "Scheduler Callable can't be null");
        Function<? super Callable<Scheduler>, ? extends Scheduler> f = onInitComputationHandler;
        if (f == null) {
            return callRequireNonNull(defaultScheduler);
        }
        return applyRequireNonNull(f, defaultScheduler);
    }

    /**
     * Calls the associated hook function.
     * @param defaultScheduler a {@link Callable} which returns the hook's input value
     * @return the value returned by the hook, not null
     * @throws NullPointerException if the callable parameter or its result are null
     */
    public static Scheduler initIoScheduler(Callable<Scheduler> defaultScheduler) {
        ObjectHelper.requireNonNull(defaultScheduler, "Scheduler Callable can't be null");
        Function<? super Callable<Scheduler>, ? extends Scheduler> f = onInitIoHandler;
        if (f == null) {
            return callRequireNonNull(defaultScheduler);
        }
        return applyRequireNonNull(f, defaultScheduler);
    }

    /**
     * Calls the associated hook function.
     * @param defaultScheduler a {@link Callable} which returns the hook's input value
     * @return the value returned by the hook, not null
     * @throws NullPointerException if the callable parameter or its result are null
     */
    public static Scheduler initNewThreadScheduler(Callable<Scheduler> defaultScheduler) {
        ObjectHelper.requireNonNull(defaultScheduler, "Scheduler Callable can't be null");
        Function<? super Callable<Scheduler>, ? extends Scheduler> f = onInitNewThreadHandler;
        if (f == null) {
            return callRequireNonNull(defaultScheduler);
        }
        return applyRequireNonNull(f, defaultScheduler);
    }

    /**
     * Calls the associated hook function.
     * @param defaultScheduler a {@link Callable} which returns the hook's input value
     * @return the value returned by the hook, not null
     * @throws NullPointerException if the callable parameter or its result are null
     */
    public static Scheduler initSingleScheduler(Callable<Scheduler> defaultScheduler) {
        ObjectHelper.requireNonNull(defaultScheduler, "Scheduler Callable can't be null");
        Function<? super Callable<Scheduler>, ? extends Scheduler> f = onInitSingleHandler;
        if (f == null) {
            return callRequireNonNull(defaultScheduler);
        }
        return applyRequireNonNull(f, defaultScheduler);
    }

    /**
     * Calls the associated hook function.
     * @param defaultScheduler the hook's input value
     * @return the value returned by the hook
     */
    public static Scheduler onComputationScheduler(Scheduler defaultScheduler) {
        Function<? super Scheduler, ? extends Scheduler> f = onComputationHandler;
        if (f == null) {
            return defaultScheduler;
        }
        return apply(f, defaultScheduler);
    }

    /**
     * Called when an undeliverable error occurs.
     * @param error the error to report
     */
    public static void onError(Throwable error) {
        Consumer<? super Throwable> f = errorHandler;

        if (error == null) {
            error = new NullPointerException("onError called with null.");
        } else {
            if (!isBug(error)) {
                error = new UndeliverableException(error);
            }
        }

        if (f != null) {
            try {
                f.accept(error);
                return;
            } catch (Throwable e) {
                e.printStackTrace();
                uncaught(e);
            }
        }

        error.printStackTrace();
        uncaught(error);
    }

    /**
     * Checks if the given error is one of the already named
     * bug cases that should pass through {@link #onError(Throwable)}
     * as is.
     * @param error the error to check
     * @return true if the error should pass through, false if
     * it may be wrapped into an UndeliverableException
     */
    static boolean isBug(Throwable error) {
        // user forgot to add the onError handler in subscribe
        if (error instanceof OnErrorNotImplementedException) {
            return true;
        }
        // the sender didn't honor the request amount
        // it's either due to an operator bug or concurrent onNext
        if (error instanceof MissingBackpressureException) {
            return true;
        }
        // general protocol violations
        // it's either due to an operator bug or concurrent onNext
        if (error instanceof IllegalStateException) {
            return true;
        }
        // nulls are generally not allowed
        // likely an operator bug or missing null-check
        if (error instanceof NullPointerException) {
            return true;
        }
        // bad arguments, likely invalid user input
        if (error instanceof IllegalArgumentException) {
            return true;
        }
        // Crash while handling an exception
        if (error instanceof CompositeException) {
            return true;
        }
        // everything else is probably due to lifecycle limits
        return false;
    }

    static void uncaught(Throwable error) {
        Thread currentThread = Thread.currentThread();
        Thread.UncaughtExceptionHandler handler = currentThread.getUncaughtExceptionHandler();
        handler.uncaughtException(currentThread, error);
    }

    /**
     * Calls the associated hook function.
     * @param defaultScheduler the hook's input value
     * @return the value returned by the hook
     */
    public static Scheduler onIoScheduler(Scheduler defaultScheduler) {
        Function<? super Scheduler, ? extends Scheduler> f = onIoHandler;
        if (f == null) {
            return defaultScheduler;
        }
        return apply(f, defaultScheduler);
    }

    /**
     * Calls the associated hook function.
     * @param defaultScheduler the hook's input value
     * @return the value returned by the hook
     */
    public static Scheduler onNewThreadScheduler(Scheduler defaultScheduler) {
        Function<? super Scheduler, ? extends Scheduler> f = onNewThreadHandler;
        if (f == null) {
            return defaultScheduler;
        }
        return apply(f, defaultScheduler);
    }

    /**
     * Called when a task is scheduled.
     * @param run the runnable instance
     * @return the replacement runnable
     */
    public static Runnable onSchedule(Runnable run) {
        Function<? super Runnable, ? extends Runnable> f = onScheduleHandler;
        if (f == null) {
            return run;
        }
        return apply(f, run);
    }

    /**
     * Calls the associated hook function.
     * @param defaultScheduler the hook's input value
     * @return the value returned by the hook
     */
    public static Scheduler onSingleScheduler(Scheduler defaultScheduler) {
        Function<? super Scheduler, ? extends Scheduler> f = onSingleHandler;
        if (f == null) {
            return defaultScheduler;
        }
        return apply(f, defaultScheduler);
    }

    /**
     * Removes all handlers and resets to default behavior.
     */
    public static void reset() {
        setErrorHandler(null);
        setScheduleHandler(null);

        setComputationSchedulerHandler(null);
        setInitComputationSchedulerHandler(null);

        setIoSchedulerHandler(null);
        setInitIoSchedulerHandler(null);

        setSingleSchedulerHandler(null);
        setInitSingleSchedulerHandler(null);

        setNewThreadSchedulerHandler(null);
        setInitNewThreadSchedulerHandler(null);

        setFailOnNonBlockingScheduler(false);
    }

    /**
     * Sets the specific hook function.
     * @param handler the hook function to set, null allowed
     */
    public static void setComputationSchedulerHandler(Function<? super Scheduler, ? extends Scheduler> handler) {
        if (lockdown) {
            throw new IllegalStateException("ScheduleHooks can't be changed anymore");
        }
        onComputationHandler = handler;
    }

    /**
     * Sets the specific hook function.
     * @param handler the hook function to set, null allowed
     */
    public static void setErrorHandler(Consumer<? super Throwable> handler) {
        if (lockdown) {
            throw new IllegalStateException("ScheduleHooks can't be changed anymore");
        }
        errorHandler = handler;
    }

    /**
     * Sets the specific hook function.
     * @param handler the hook function to set, null allowed, but the function may not return null
     */
    public static void setInitComputationSchedulerHandler(Function<? super Callable<Scheduler>, ? extends Scheduler> handler) {
        if (lockdown) {
            throw new IllegalStateException("ScheduleHooks can't be changed anymore");
        }
        onInitComputationHandler = handler;
    }

    /**
     * Sets the specific hook function.
     * @param handler the hook function to set, null allowed, but the function may not return null
     */
    public static void setInitIoSchedulerHandler(Function<? super Callable<Scheduler>, ? extends Scheduler> handler) {
        if (lockdown) {
            throw new IllegalStateException("ScheduleHooks can't be changed anymore");
        }
        onInitIoHandler = handler;
    }

    /**
     * Sets the specific hook function.
     * @param handler the hook function to set, null allowed, but the function may not return null
     */
    public static void setInitNewThreadSchedulerHandler(Function<? super Callable<Scheduler>, ? extends Scheduler> handler) {
        if (lockdown) {
            throw new IllegalStateException("ScheduleHooks can't be changed anymore");
        }
        onInitNewThreadHandler = handler;
    }

    /**
     * Sets the specific hook function.
     * @param handler the hook function to set, null allowed, but the function may not return null
     */
    public static void setInitSingleSchedulerHandler(Function<? super Callable<Scheduler>, ? extends Scheduler> handler) {
        if (lockdown) {
            throw new IllegalStateException("ScheduleHooks can't be changed anymore");
        }
        onInitSingleHandler = handler;
    }

    /**
     * Sets the specific hook function.
     * @param handler the hook function to set, null allowed
     */
    public static void setIoSchedulerHandler(Function<? super Scheduler, ? extends Scheduler> handler) {
        if (lockdown) {
            throw new IllegalStateException("ScheduleHooks can't be changed anymore");
        }
        onIoHandler = handler;
    }

    /**
     * Sets the specific hook function.
     * @param handler the hook function to set, null allowed
     */
    public static void setNewThreadSchedulerHandler(Function<? super Scheduler, ? extends Scheduler> handler) {
        if (lockdown) {
            throw new IllegalStateException("ScheduleHooks can't be changed anymore");
        }
        onNewThreadHandler = handler;
    }

    /**
     * Sets the specific hook function.
     * @param handler the hook function to set, null allowed
     */
    public static void setScheduleHandler(Function<? super Runnable, ? extends Runnable> handler) {
        if (lockdown) {
            throw new IllegalStateException("ScheduleHooks can't be changed anymore");
        }
        onScheduleHandler = handler;
    }

    /**
     * Sets the specific hook function.
     * @param handler the hook function to set, null allowed
     */
    public static void setSingleSchedulerHandler(Function<? super Scheduler, ? extends Scheduler> handler) {
        if (lockdown) {
            throw new IllegalStateException("ScheduleHooks can't be changed anymore");
        }
        onSingleHandler = handler;
    }

    /**
     * Revokes the lockdown, only for testing purposes.
     */
    static void unlock() {
        lockdown = false;
    }

    /**
     * Wraps the call to the Scheduler creation callable in try-catch and propagates thrown
     * checked exceptions as RuntimeException and enforces that result is not null.
     * @param s the {@link Callable} which returns a {@link Scheduler}, not null (not verified). Cannot return null
     * @return the result of the callable call, not null
     * @throws NullPointerException if the callable parameter returns null
     */
    static Scheduler callRequireNonNull(Callable<Scheduler> s) {
        try {
            return ObjectHelper.requireNonNull(s.call(), "Scheduler Callable result can't be null");
        } catch (Throwable ex) {
            throw ExceptionHelper.wrapOrThrow(ex);
        }
    }

    /**
     * Wraps the call to the function in try-catch and propagates thrown
     * checked exceptions as RuntimeException.
     * @param <T> the input type
     * @param <R> the output type
     * @param f the function to call, not null (not verified)
     * @param t the parameter value to the function
     * @return the result of the function call
     */
    static <T, R> R apply(Function<T, R> f, T t) {
        try {
            return f.apply(t);
        } catch (Throwable ex) {
            throw ExceptionHelper.wrapOrThrow(ex);
        }
    }

    /**
     * Wraps the call to the Scheduler creation function in try-catch and propagates thrown
     * checked exceptions as RuntimeException and enforces that result is not null.
     * @param f the function to call, not null (not verified). Cannot return null
     * @param s the parameter value to the function
     * @return the result of the function call, not null
     * @throws NullPointerException if the function parameter returns null
     */
    static Scheduler applyRequireNonNull(Function<? super Callable<Scheduler>, ? extends Scheduler> f, Callable<Scheduler> s) {
        return ObjectHelper.requireNonNull(apply(f, s), "Scheduler Callable result can't be null");
    }

    /** Helper class, no instances. */
    private ScheduleHooks() {
        throw new IllegalStateException("No instances!");
    }

    public static final class UndeliverableException extends IllegalStateException {

        /**
         * Construct an instance by wrapping the given, non-null
         * cause Throwable.
         * @param cause the cause, not null
         */
        public UndeliverableException(Throwable cause) {
            super(cause);
        }
    }

    public static final class OnErrorNotImplementedException extends RuntimeException {

        /**
         * Customizes the {@code Throwable} with a custom message and wraps it before it
         * is signalled to the {@code onError()} handler as {@code OnErrorNotImplementedException}.
         *
         * @param message
         *          the message to assign to the {@code Throwable} to signal
         * @param e
         *          the {@code Throwable} to signal; if null, a NullPointerException is constructed
         */
        public OnErrorNotImplementedException(String message, Throwable e) {
            super(message, e != null ? e : new NullPointerException());
        }

        /**
         * Wraps the {@code Throwable} before it
         * is signalled to the {@code onError()}
         * handler as {@code OnErrorNotImplementedException}.
         *
         * @param e
         *          the {@code Throwable} to signal; if null, a NullPointerException is constructed
         */
        public OnErrorNotImplementedException(Throwable e) {
            super(e != null ? e.getMessage() : null, e != null ? e : new NullPointerException());
        }
    }

    public static final class MissingBackpressureException extends RuntimeException {

        /**
         * Constructs a MissingBackpressureException without message or cause.
         */
        public MissingBackpressureException() {
            // no message
        }

        /**
         * Constructs a MissingBackpressureException with the given message but no cause.
         * @param message the error message
         */
        public MissingBackpressureException(String message) {
            super(message);
        }

    }

}
