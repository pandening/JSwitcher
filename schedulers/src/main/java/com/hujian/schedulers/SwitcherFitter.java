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

import com.google.common.base.Preconditions;
import com.hujian.schedulers.core.Scheduler;
import com.hujian.schedulers.core.Schedulers;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by hujian06 on 2017/8/30.
 *
 * Do Executor Switcher work here. the final utils class offers
 * some method to support 'switch' function.
 * the class is utils,final, so you need not to get instance by
 * "new" operator, and you can not extend this class too.
 */
@SuppressWarnings(value = "unchecked")
public final class SwitcherFitter<T> implements SwitchOffer<T> {

    /**
     * this is the only instance of this class.
     */
    private static final SwitcherFitter SWITCHER_FITTER = new SwitcherFitter();

    /**
     * SWITCHER_FITTER == null ?
     * @return the only instance of this class.
     */
    public static SwitcherFitter switcherFitter() {
        return SWITCHER_FITTER;
    }

    /**
     * the current schedule reference,you should set it after switch schedule
     * and you can not get it at all for some reasons.
     * The initial value is 'null'
     */
    private AtomicReference<Scheduler> currentScheduleReference = new AtomicReference<>(null);

    /**
     * you can not get an new instance by 'new' operator
     */
    private SwitcherFitter() {
        //throw new UnsupportedOperationException("no Instance");
    }

    /**
     * calling this method after you switch to an new schedule
     * @param toScheduler
     */
    private void switchSchedule(Scheduler toScheduler) {
        Preconditions.checkArgument(toScheduler != null,
                "schedule must nonNull");
        for(;;) {
            Scheduler currentSchedule = currentScheduleReference.get();
            if (currentSchedule != null) {
                //TODO need to shutdown now ?
                //currentSchedule.shutdown();
            }
            if (currentScheduleReference.compareAndSet(currentSchedule, toScheduler)) {
                return;
            }
        }
    }

    /**
     * do some check here, then the job will be run by the current env.(schedule)
     * @return the schedule {@link SwitcherFitter#currentScheduleReference#get#}
     *         the function will throw an new Exception in this method body.
     * @throws RequireScheduleFailureException
     *         the exception should be catch in your code
     */
    private Scheduler requireScheduleEnv() throws RequireScheduleFailureException{
        if (null == currentScheduleReference.get()) {
            throw new RequireScheduleFailureException("No Schedule!");
        }

        return currentScheduleReference.get();
    }

    @Override
    public SwitcherFitter switchTo(Scheduler scheduler) {
        switchSchedule(scheduler);
        return this;
    }

    @Override
    public SwitcherFitter switchToIoSchedule() {
        switchSchedule(Schedulers.io());
        return this;
    }

    @Override
    public SwitcherFitter switchToComputeSchedule() {
        switchSchedule(Schedulers.computation());
        return this;
    }

    @Override
    public SwitcherFitter switchToNewSchedule() {
        switchSchedule(Schedulers.newThread());
        return this;
    }

    @Override
    public SwitcherFitter switchToSingleSchedule() {
        switchSchedule(Schedulers.single());
        return this;
    }

    @Override
    public SwitcherFitter fit(Runnable runnable) throws RequireScheduleFailureException,
            ExecutionException, InterruptedException {
        Preconditions.checkArgument(runnable != null,
                "runnable must nonNull");
        Scheduler scheduler = requireScheduleEnv();

        scheduler.scheduleDirect(runnable);
        return this;
    }

    @Override
    public SwitcherFitter fit(Runnable runnable, SwitcherResultFuture<T> future)
            throws RequireScheduleFailureException, ExecutionException, InterruptedException {
        Preconditions.checkArgument(runnable != null,
                "runnable must nonNull");
        Scheduler scheduler = requireScheduleEnv();
        future = Optional.ofNullable(future).orElse(new SwitcherResultFuture<T>());

        //schedule with the result.
        scheduler.scheduleDirect(runnable, future);

        return this;
    }

    @Override
    public SwitcherFitter fit(Runnable runnable, long delay, TimeUnit unit)
            throws RequireScheduleFailureException, ExecutionException, InterruptedException {
        Preconditions.checkArgument(runnable != null && unit != null, "Npe");
        Scheduler scheduler = requireScheduleEnv();

        //schedule only
        scheduler.scheduleDirect(runnable, delay, unit);

        return this;
    }

    @Override
    public SwitcherFitter fit(Runnable runnable, long delay, TimeUnit unit, SwitcherResultFuture<T> future)
            throws RequireScheduleFailureException, ExecutionException, InterruptedException {
        Preconditions.checkArgument(runnable != null && unit != null, "Npe");
        Scheduler scheduler = requireScheduleEnv();
        future = Optional.ofNullable(future).orElse(new SwitcherResultFuture<T>());

        //schedule with the future result
        scheduler.scheduleDirect(runnable, delay, unit, future);

        return this;
    }

    @Override
    public SwitcherFitter fit(ScheduleRunner<T> runner, SwitcherResultFuture<T> future, Boolean isAsyncMode)
            throws RequireScheduleFailureException, ExecutionException, InterruptedException {
        Preconditions.checkArgument(runner != null, "Npe");
        Scheduler scheduler = requireScheduleEnv();
        future = Optional.ofNullable(future).orElse(new SwitcherResultFuture<T>());

        scheduler.scheduleDirect(runner, future, isAsyncMode);

        return this;
    }

    @Override
    public SwitcherFitter fit(ScheduleRunner<T> runner, long delay, TimeUnit unit, SwitcherResultFuture<T> future,
                              Boolean isAsyncMode) throws RequireScheduleFailureException {
        throw new UnsupportedOperationException("No Such Operation");
    }

    @Override
    public SwitcherFitter fit(Runnable runnable, long initialDelay, long period, TimeUnit unit)
            throws RequireScheduleFailureException, ExecutionException, InterruptedException {
        throw new UnsupportedOperationException("No Such Operation");
    }

}
