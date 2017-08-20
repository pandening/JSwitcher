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
import com.hujian.switcher.RichnessSwitcher;
import com.hujian.switcher.SampleSwitcher;
import com.hujian.switcher.core.SwitchRunntimeException;
import com.hujian.switcher.utils.SwitcherFactory;
import com.hujian.switcher.SwitcherResultfulEntry;

import java.util.concurrent.ExecutionException;

/**
 * Created by hujian06 on 2017/8/18.
 */
public class SwitchDemo {

    private static SampleSwitcher sampleSwitcher = new SampleSwitcher();
    private static RichnessSwitcher richnessSwitcher = new RichnessSwitcher();
    private static StupidWorker stupidWorker = new StupidWorker();

    @SuppressWarnings(value = "unchecked")
    public static void main(String ... args)
            throws InterruptedException, SwitchRunntimeException, ExecutionException {


        sampleSwitcher
                .switchToMultiIoExecutor(true) //switch to a multi-io-executor[first executorService]
                .apply(stupidWorker, true) //do the stupidWorker on the multi-io-executor executorService
                .switchToMultiComputeExecutor(true) //switch to a multi-compute-executor
                .apply(stupidWorker, false) //do the stupidWorker on the multi-compute-executor
                // first of all switch to an compute executor,then do the stupidWorker on the new compute executorService
                .switchBeforeIoWork(stupidWorker, true, false)
                .switchToNewSingleExecutor() // switch to an new single executor
                .apply(stupidWorker, false); // do the stupidWorker on the single executorService


        String executorName = (String) richnessSwitcher
                .switchToIoExecutor(true)
                .transToRichnessSwitcher()
                .assignName("hujian")
                .apply(stupidWorker, false)
                .switchToNewSingleExecutor()
                .transToRichnessSwitcher()
                .getSwitcherWithExtraData()
                .getData();

        System.out.println("current executor Service name:" + executorName);

        SwitcherFactory.createShareRichnessSwitcher()
                .assignName("empty")
                .switchToNewIoExecutor()
                .switchToComputeExecutor(true)
                .transToRichnessSwitcher()
                .assignName("Assigned-Compute-Executor")
                .switchBackToComputeExecutor(true)
                .apply(stupidWorker, false);

        SwitcherResultfulEntry<String> stringSwitcherResultfulEntry
                = SwitcherResultfulEntry.emptyEntry();
        SwitcherResultfulEntry<Integer> switcherResultfulEntry
                = SwitcherResultfulEntry.emptyEntry();

        SwitcherFactory.createResultfulSwitcher()
                .switchToMultiComputeExecutor(true)
                .transToRichnessSwitcher()
                .transToResultfulSwitcher()
                .asyncApply(new AbstractSwitcherRunner() {
                    @Override
                    protected Object run() {
                        return "i am switcher:" + Thread.currentThread().getName();
                    }
                    @Override
                    protected Object fallback() {
                        return "i am fallback";
                    }
                }, stringSwitcherResultfulEntry)
                .switchAfterIOWork(stupidWorker, true, false)
                .transToRichnessSwitcher()
                .transToResultfulSwitcher()
                .asyncApply(new AbstractSwitcherRunner() {
                    @Override
                    protected Object run() {
                        return "i am switcher:" + Thread.currentThread().getName();
                    }

                    @Override
                    protected Object fallback() {
                        return "i am fallback";
                    }
                }, switcherResultfulEntry);

        SwitcherFactory.shutdown();

        System.out.println("sync:" +
                stringSwitcherResultfulEntry.getResultfulData());

        System.out.println("async:" + switcherResultfulEntry.getResultfulData());

    }

    /**
     * test job.
     */
    private static class StupidWorker implements Runnable {
        @Override
        public void run() {
            System.out.println("i am in:" + Thread.currentThread().getName());
        }
    }

}
