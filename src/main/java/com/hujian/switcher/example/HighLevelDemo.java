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

import com.hujian.switcher.AbstractSwitcherRunner;
import com.hujian.switcher.SwitcherResultfulEntry;
import com.hujian.switcher.schedulers.ScheduleHooks;
import com.hujian.switcher.utils.SwitcherFactory;

import java.util.concurrent.ExecutionException;

/**
 * Created by hujian06 on 2017/8/21.
 */
public class HighLevelDemo {

    public static void main(String ... args)
            throws InterruptedException, ExecutionException {

        SwitcherFactory
                .createResultfulSwitcher()
                .switchToNewExecutor()
                .transToRichnessSwitcher()
                .transToResultfulSwitcher()
                .syncApply(producer, producerData)
                .switchBackToIoExecutor(true)
                .transToRichnessSwitcher()
                .transToResultfulSwitcher()
                .asyncApply(consumer, consumerData);

        SwitcherFactory.shutdown();

        System.out.println("Result:" + consumerData.getResultfulData());


    }

    private static Producer producer = new Producer();
    private static Consumer consumer = new Consumer();

    private static SwitcherResultfulEntry<String> producerData
            = SwitcherResultfulEntry.emptyEntry();

    private static SwitcherResultfulEntry<String> consumerData
            = SwitcherResultfulEntry.emptyEntry();

    private static class Producer extends AbstractSwitcherRunner<String> {

        @Override
        protected String run() {
            return "hujian ->" + Thread.currentThread().getName();
        }

        @Override
        protected String fallback() {
            return "fallback";
        }
    }

    private static class Consumer extends AbstractSwitcherRunner<String> {

        @Override
        protected String run() {
            try {
                return producerData.getResultfulData().toUpperCase() + " ->" + Thread.currentThread().getName();
            } catch (ExecutionException e) {
                ScheduleHooks.onError(e);
            } catch (InterruptedException e) {
                ScheduleHooks.onError(e);
            }
            return "error";
        }

        @Override
        protected String fallback() {
            return "fallback";
        }
    }

}
