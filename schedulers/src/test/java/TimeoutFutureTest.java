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

import com.google.common.collect.Lists;
import com.hujian.schedulers.AbstractScheduleRunner;
import com.hujian.schedulers.RequireScheduleFailureException;
import com.hujian.schedulers.SwitcherFitter;
import com.hujian.schedulers.SwitcherResultFuture;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by hujian06 on 2017/9/1.
 * test
 */
@SuppressWarnings(value = "unchecked")
public class TimeoutFutureTest {

    static SwitcherResultFuture<String> future1 = new SwitcherResultFuture<>(); //normal
    static SwitcherResultFuture<String> future2 = new SwitcherResultFuture<>(); //normal
    static SwitcherResultFuture<String> future3 = new SwitcherResultFuture<>(); //timeout
    static SwitcherResultFuture<String> future4 = new SwitcherResultFuture<>(); //timeout

    static NormalRunner normalRunner = new NormalRunner();
    static TimeoutRunner timeoutRunner = new TimeoutRunner();

    static List<SwitcherResultFuture<?>> completableFutures;
    static List<SwitcherResultFuture<?>> timeoutFutures;


    static class NormalRunner extends AbstractScheduleRunner<String> {

        @Override
        protected String realRun() {
            return "normal-run:" + Thread.currentThread().getName();
        }

        @Override
        protected String fallback(Exception e) {
            return "fail-run:" + Thread.currentThread().getName();
        }
    }


    static class TimeoutRunner extends AbstractScheduleRunner<String> {

        @Override
        protected String realRun() {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "timeout-run:" + Thread.currentThread().getName();
        }

        @Override
        protected String fallback(Exception e) {
            return "timeout-run:" + Thread.currentThread().getName();
        }
    }

    public static void main(String ... args)
            throws InterruptedException, ExecutionException, RequireScheduleFailureException {

        completableFutures = Lists.newArrayList(future1, future2, future3, future4);

        timeoutFutures = Lists.newArrayList();

        SwitcherFitter.switcherFitter()
                .switchToIoSchedule()
                .switchToSingleSchedule()
                .fit(normalRunner, future1, true)
                .switchToComputeSchedule()
                .fit(normalRunner, future2, true)
                .fit(timeoutRunner, future3, true)
                .switchToSingleSchedule()
                .switchToSingleSchedule()
                .fit(timeoutRunner, future4, true)
                .awaitFuturesCompletedOrTimeout(100, completableFutures, timeoutFutures, 10)
                .switchToComputeSchedule()
                .fit(() -> {System.out.println("i am a tester->" + Thread.currentThread().getName());});
        if (timeoutFutures != null && !timeoutFutures.isEmpty()) {
            for (SwitcherResultFuture<?> future : timeoutFutures) {
                System.out.println("timeoutFuture:" + future.getFuture());
            }
        }

        for (SwitcherResultFuture<?> future : completableFutures) {
            System.out.println("result:" + future.fetchResult());
        }


    }

}
