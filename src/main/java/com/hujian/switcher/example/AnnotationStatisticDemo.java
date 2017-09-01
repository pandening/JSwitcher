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

package com.hujian.switcher.example;

import com.hujian.switcher.SwitcherResultfulEntry;
import com.hujian.switcher.annotation.Switcher;
import com.hujian.switcher.annotation.SwitcherAnnotationResolver;
import com.hujian.switcher.statistic.SampleSwitcherStatistic;
import com.hujian.switcher.statistic.Statistic;
import com.hujian.switcher.statistic.SwitcherStatisticEntry;
import com.hujian.switcher.utils.SwitcherFactory;

import java.util.Map;
import java.util.Random;

/**
 * Created by hujian06 on 2017/8/22.
 */
public class AnnotationStatisticDemo {
    private static final String IO_EXECUTOR_NAME = "io-executorService";
    private static final String MULTI_IO_EXECUTOR_SERVICE = "multi-io-executorService";
    private static final String COMPUTE_EXECUTOR_SERVICE = "compute-executorService";
    private static final String MULTI_COMPUTE_EXECUTOR_SERVICE = "multi-compute-executorService";
    private static final String SINGLE_EXECUTOR_SERVICE = "single-executorService";
    private static final String NEW_EXECUTOR_SERVICE = "new-executorService";

    @Switcher(switchToExecutorServiceType = MULTI_COMPUTE_EXECUTOR_SERVICE)
    public static StupidRunner stupidRunnerA;

    @Switcher(switchToExecutorServiceName = "stupidExecutorService")
    public static StupidRunner stupidRunnerB;

    @Switcher(CreateType = MULTI_COMPUTE_EXECUTOR_SERVICE)
    public static StupidRunner stupidRunnerC;

    public static void main(String ... args) throws InterruptedException {
        SwitcherAnnotationResolver resolver = new SwitcherAnnotationResolver(scanPackage);
        try {
            SwitcherFactory
                    .createResultfulSwitcher()
                    .switchToComputeExecutor(true)
                    .assignExecutorName("stupidExecutorService")
                    .switchToMultiIoExecutor(true);

            resolver.execute();
        } catch (Exception e) {
            ScheduleHooks.onError(e);
        } finally {
            Map<String, SwitcherStatisticEntry> map = statistic.getSwitchStatisticEntryBySessionId(SESSION_ID);
            for (Map.Entry<String, SwitcherStatisticEntry> entryEntry : map.entrySet()) {
                System.out.println(entryEntry.toString());
            }
            SwitcherFactory.shutdown();
        }
    }

    private static Statistic statistic = SampleSwitcherStatistic.getInstance();
    private static final String SESSION_ID = "Annotation-statistic";
    private static SwitcherResultfulEntry<String> resultfulEntry
            = SwitcherResultfulEntry.emptyEntry();
    private static final String scanPackage = ".com.hujian.switcher.example";

    public static class StupidRunner implements Runnable {
        private static Random random = new Random(47);
        private Boolean errorFlag = false;

        public StupidRunner() {
            errorFlag = false;
        }

        public StupidRunner(Boolean errorFlag) {
            this.errorFlag = errorFlag;
        }
        @Override
        public void run() {
            int sleep = random.nextInt(1000) % 1000;
            try {
                if (errorFlag == true) {
                    int errorCode = 1 / 0;
                }
            } catch (Exception e) {
                ScheduleHooks.onError(e);
            } finally {
                System.out.println("i am stupid Runner at :" + Thread.currentThread().getName()
                        + " sleep:" + sleep);
            }
        }
    }
}
