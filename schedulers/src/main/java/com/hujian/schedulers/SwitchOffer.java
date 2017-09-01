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

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * Created by hujian06 on 2017/8/30.
 *
 * the function of 'switcher'
 */
public interface SwitchOffer<T> {

    /**
     * waiting the future to complete or timeout, you should offer a timeout value for waiting
     * and the future that you want to wait.
     * this method can detect which future timeout
     *
     *      THIS METHOD WILL RUN AT MAIN THREAD, BUT THE STILL WAIT WAIT JOB WILL
     *      RUN AT AN NEW SCHEDULE {@link com.hujian.schedulers.core.Schedulers#NEW_THREAD#}
     *      YOU CAN ALSO CHANGE THE SCHEDULE BY {@link ScheduleHooks} !!LATER!!
     *
     * @param timeoutMillis
     *                              the timeout millis
     * @param completableFutures
     *                              the future
     * @param timeoutFutures
     *                             the timeout future here.
     * @param stillWaitTimeMills
     *                            if you want to still wait the future, let the value > 0 , or
     *                            the method will auto-cancel the future, and the future will return
     *                            "null" , so you should check the return value before you do anything
     *                            according to the return value!!
     * @return the context
     * @throws ExecutionException
     *         runningTime Exception
     * @throws InterruptedException
     *         interrupted Exception
     */
    SwitcherFitter awaitFuturesCompletedOrTimeout(int timeoutMillis, List<SwitcherResultFuture<?>> completableFutures,
                                                  List<SwitcherResultFuture<?>> timeoutFutures, int stillWaitTimeMills)
            throws ExecutionException, InterruptedException, RequireScheduleFailureException;


    /**
     * the end of operator.
     * the method should be called once in a 'jvm'
     * the method will wait the future some time {@code timeMillsToWait}, then
     * shutdown all of the schedule, so, you can not do any work after call
     * this method.
     *                              !!!NOTICE!!!
     *     THIS METHOD WILL RETURN IMMEDIATELY IF THE {@link SwitcherFitter#totalFutures} IS
     *     NULL OR EMPTY,SO THIS METHOD IS NOT A METHOD WITH 'SLEEP' FUNCTION.THIS METHOD
     *     JUST FOR WAITING THE FUTURE(S) BEFORE END OF THE "SWITCH CHAIN"
     *
     * @param timeMillsToWait the time to wait.
     */
    void waitAndShutdown(int timeMillsToWait) throws InterruptedException, ExecutionException, RequireScheduleFailureException;

    /**
     * switch to a schedule from param {@code scheduler}. you can get a custom schedule by
     * {@link com.hujian.schedulers.core.Schedulers#from(Executor)}
     * just offer the special executor to get the schedule.and let the
     * follow runnable run by the schedule.
     * @param scheduler the schedule.
     * @return the switcher context.
     */
    SwitcherFitter switchTo(Scheduler scheduler);

    /**
     * switch to the IoHolder Schedule auto.the Switcher will switch to
     * {@link com.hujian.schedulers.core.Schedulers#IO#}
     * schedule, then the runnable will run by the schedule.
     * @return the switcher context
     */
    SwitcherFitter switchToIoSchedule();

    /**
     * switch to the IoHolder Schedule auto.the Switcher will switch to
     * {@link com.hujian.schedulers.core.Schedulers#COMPUTATION#}
     * schedule, then the runnable will run by the schedule.
     * @return the switcher context
     */
    SwitcherFitter switchToComputeSchedule();

    /**
     * switch to the IoHolder Schedule auto.the Switcher will switch to
     * {@link com.hujian.schedulers.core.Schedulers#NEW_THREAD#}
     * schedule, then the runnable will run by the schedule.
     * @return the switcher context
     */
    SwitcherFitter switchToNewSchedule();

    /**
     * switch to the IoHolder Schedule auto.the Switcher will switch to
     * {@link com.hujian.schedulers.core.Schedulers#SINGLE#}
     * schedule, then the runnable will run by the schedule.
     * @return the switcher context
     */
    SwitcherFitter switchToSingleSchedule();

    /**
     * after switching to schedule that you choose to. you can fit your work
     * on this schedule.you can also switch to another schedule now. but if you
     * want to do your job on the current schedule now,you can choose this method
     * the {@code runnable} will run by the current schedule
     * @param runnable the runnable, must nonNull
     * @return the context
     */
    SwitcherFitter fit(Runnable runnable) throws RequireScheduleFailureException, ExecutionException, InterruptedException;


    /**
     * after switching to schedule that you choose to. you can fit your work
     * on this schedule.you can also switch to another schedule now. but if you
     * want to do your job on the current schedule now,you can choose this method
     * the {@code runnable} will run by the current schedule, and the result future
     * will be stored in an object of {@link SwitcherResultFuture<T>}
     * so that you can fetch the real result from the future. the object of {@link SwitcherResultFuture<T>}
     * offer some methods to get the real data from the future:
     * {@link SwitcherResultFuture#fetch()} #}
     * {@link SwitcherResultFuture#fetch(long, TimeUnit)} (long, TimeUnit)#}
     * @param runnable the runnable, must nonNull
     * @param future future, the object for fetching result from.
     * @return the context
     */
    SwitcherFitter fit(Runnable runnable, SwitcherResultFuture<T> future) throws RequireScheduleFailureException,
            ExecutionException, InterruptedException;

    /**
     * just like {@link #fit(Runnable, SwitcherResultFuture)#}, bit this method will more powerful in the
     * same time.you can set a delay time to schedule your work.
     * @param runnable the runnable, must nonNull
     * @param delay the delay time
     * @param unit the time unit {@link TimeUnit#SECONDS ... etc.}
     * @return the context
     */
    SwitcherFitter fit(Runnable runnable, long delay, TimeUnit unit) throws RequireScheduleFailureException,
            ExecutionException, InterruptedException;

    /**
     * just like {@link #fit(Runnable, SwitcherResultFuture)#}, bit this method will more powerful in the
     * same time.you can set a delay time to schedule your work.
     * @param runnable the runnable, must nonNull
     * @param delay the delay time
     * @param unit the time unit {@link TimeUnit#SECONDS ... etc.}
     * @param future the future. the object for fetching result from.
     * @return the context
     */
    SwitcherFitter fit(Runnable runnable, long delay, TimeUnit unit, SwitcherResultFuture<T> future)
            throws RequireScheduleFailureException, ExecutionException, InterruptedException;

    /**
     * like {@link #fit(Runnable, SwitcherResultFuture)}
     * and
     * {@link #fit(Runnable, long, TimeUnit, SwitcherResultFuture)}
     * but the special is the method is Cyclical. you can set the initial delay time, period to schedule
     * your job {@code runnable}
     * @param runnable the runnable, must nonNull
     * @param initialDelay the initial delay time to start up
     * @param period the period to schedule
     * @param unit the time unit
     * @return the context
     */
    SwitcherFitter fit(Runnable runnable, long initialDelay, long period, TimeUnit unit)
            throws RequireScheduleFailureException, ExecutionException, InterruptedException;

    /**
     * just like {@link #fit(Runnable)#}
     * the only difference is the job replace by an object of {@link ScheduleRunner<T>}
     * @param runner the runnable, nonNull
     * @param future the future
     * @param isAsyncMode  choose the run mode, async|sync
     * @return the context
     */
    SwitcherFitter fit(ScheduleRunner<T> runner, SwitcherResultFuture<T> future, Boolean isAsyncMode)
            throws RequireScheduleFailureException, ExecutionException, InterruptedException;

    /**
     * just like {@link #fit(Runnable, long, TimeUnit, SwitcherResultFuture)#}
     * the only difference is the job replace by an object of {@link ScheduleRunner<T>}
     * @param runner the runnable. nonNull
     * @param delay the delay time
     * @param unit the time unit
     * @param future the future
     * @param isAsyncMode  choose the run mode, async|sync  false means sync mode.
     * @return the context
     */
    SwitcherFitter fit(ScheduleRunner<T> runner, long delay, TimeUnit unit, SwitcherResultFuture<T> future,
                       Boolean isAsyncMode) throws RequireScheduleFailureException;

}
