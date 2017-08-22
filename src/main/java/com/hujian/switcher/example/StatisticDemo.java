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

package com.hujian.switcher.example;

import com.hujian.switcher.AbstractSwitcherRunner;
import com.hujian.switcher.SwitcherResultfulEntry;
import com.hujian.switcher.statistic.SampleSwitcherStatistic;
import com.hujian.switcher.statistic.Statistic;
import com.hujian.switcher.statistic.SwitcherStatisticEntry;
import com.hujian.switcher.utils.SwitcherFactory;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by hujian06 on 2017/8/22.
 */
@SuppressWarnings(value = "unchecked")
public class StatisticDemo {

    public static void main(String ... args) throws InterruptedException, ExecutionException {

        try {
            SwitcherFactory
                    .createResultfulSwitcher()
                    .switchToNewIoExecutor()
                    .transToRichnessSwitcher()
                    .transToResultfulSwitcher()
                    .syncApply(stupidRunner, resultfulEntry)
                    .switchToMultiComputeExecutor(true)
                    .transToRichnessSwitcher()
                    .transToResultfulSwitcher()
                    .asyncApply(stupidRunner, resultfulEntry)
                    .switchToNewExecutor()
                    .transToRichnessSwitcher()
                    .transToResultfulSwitcher()
                    .asyncApply(stupidRunnerV2, resultfulEntry);

            TimeUnit.SECONDS.sleep(2);

            //System.out.println("result:" + resultfulEntry.getResultfulData());

            SwitcherFactory.shutdown();

            Map<String, SwitcherStatisticEntry> map = statistic.getSwitchStatisticEntryBySessionId(SESSION_ID);

            for (Map.Entry<String, SwitcherStatisticEntry> entryEntry : map.entrySet()) {
                System.out.println(entryEntry.toString());
            }

        } catch (Exception e) {
            SwitcherFactory.shutdown();
        }

    }

    private static Statistic statistic = SampleSwitcherStatistic.getInstance();
    private static StupidRunner stupidRunner = new StupidRunner();
    private static StupidRunner stupidRunnerV2 = new StupidRunner(true);
    private static final String SESSION_ID = "switcher-runner-statistic";
    private static SwitcherResultfulEntry<String> resultfulEntry
            = SwitcherResultfulEntry.emptyEntry();

    private static class StupidRunner extends AbstractSwitcherRunner<String> {
        private static Random random = new Random(47);
        private Boolean errorFlag = false;

        public StupidRunner() {

        }

        public StupidRunner(Boolean errorFlag) {
            this.errorFlag = errorFlag;
        }
        @Override
        protected String run() {
            int sleep = random.nextInt(1000) % 1000;
            try {
                TimeUnit.MILLISECONDS.sleep(sleep);
                if (errorFlag == true) {
                    int errorCode = 1 / 0;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "stupid sleep:" + sleep + " at thread:" + Thread.currentThread().getName();
        }

        @Override
        protected String fallback() {
            return "fallback is calling";
        }
    }

}
