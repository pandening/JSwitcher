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

import com.hujian.switcher.flowable.SampleSwitcherObservable;
import com.hujian.switcher.flowable.SwitcherBlockingObservableOnSubscribe;
import com.hujian.switcher.flowable.SwitcherBlockingObserverService;
import com.hujian.switcher.flowable.SwitcherBuffer;
import com.hujian.switcher.flowable.SwitcherClassTokenErrException;
import com.hujian.switcher.flowable.SwitcherConsumer;
import com.hujian.switcher.flowable.SwitcherFlowException;
import com.hujian.switcher.flowable.SwitcherObservableOnSubscribe;
import com.hujian.switcher.flowable.SwitcherObservableService;
import com.hujian.switcher.flowable.SwitcherSampleObservableOnSubscribe;
import com.hujian.switcher.utils.SwitcherFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created by hujian06 on 2017/8/23.
 */
@SuppressWarnings(value = "unchecked")
public class FlowRichDemo {

    public static void main(String ... args) throws InterruptedException {
        try {
            SwitcherFactory
                    .createSwitcherObservable()
                    .switchToMultiComputeExecutor(true)
                    .transToRichnessSwitcher()
                    .transToResultfulSwitcher()
                    .transToSampleSwitcherObservable()
                    .createSwitcherObservable(new SwitcherSampleObservableOnSubscribe() {
                        @Override
                        public void subscribe(SwitcherConsumer consumer) {
                            consumer.accept("i am hujian");
                            consumer.accept("i am hujian v1");
                            consumer.accept("i am hujian v2");
                        }
                    })
                    .switchToIoExecutor(true)
                    .transToRichnessSwitcher()
                    .transToResultfulSwitcher()
                    .transToSampleSwitcherObservable()
                    .subscribe(new SwitcherConsumer() {
                        @Override
                        public void accept(Object data) {
                            System.out.println("recv data:" + data);
                        }
                    })
                    .switchToNewExecutor()
                    .transToRichnessSwitcher()
                    .transToResultfulSwitcher()
                    .transToSampleSwitcherObservable()
                    .apply(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("current thread:" + Thread.currentThread().getName());
                        }
                    }, true)
                    .switchToNewIoExecutor()
                    .transToRichnessSwitcher()
                    .transToResultfulSwitcher()
                    .transToSampleSwitcherObservable()
                    .createSwitcherObservable(new SwitcherObservableOnSubscribe() {
                        @Override
                        public void subscribe(SwitcherObservableService observer) throws InterruptedException {
                            for (int i = 0; i < 10; i ++) {
                                observer.send("hhh " + i);
                            }
                         }
                    })
                    .switchToIoExecutor(true)
                    .transToRichnessSwitcher()
                    .transToResultfulSwitcher()
                    .transToSampleSwitcherObservable()
                    .subscribe(new SwitcherObservableService() {
                        @Override
                        protected void ctrl(SampleSwitcherObservable.SwitcherObserverInformation information) {
                            information.getDisposable().request(2);
                        }

                        @Override
                        protected void onStart() {

                        }

                        @Override
                        protected void onEmit(Object data) throws InterruptedException {
                            System.out.println("get data:" + data);
                        }

                        @Override
                        protected void onError(SwitcherFlowException e) {

                        }

                        @Override
                        protected void onComplete() {

                        }
                    })
                    .switchToMultiIoExecutor(true)
                    .transToRichnessSwitcher()
                    .transToResultfulSwitcher()
                    .transToSampleSwitcherObservable()
                    .createSwitcherBlockingObservable(new SwitcherBlockingObservableOnSubscribe() {
                        @Override
                        public void subscribe(SwitcherBlockingObserverService observer) throws InterruptedException, InstantiationException, SwitcherClassTokenErrException, IllegalAccessException {
                            for (int i = 0;i < 10; i ++) {
                                observer.send("hahaha " + i);
                            }
                        }
                    })
                    .switchToMultiIoExecutor(false)
                    .transToRichnessSwitcher()
                    .transToResultfulSwitcher()
                    .transToSampleSwitcherObservable()
                    .subscribe(new SwitcherBlockingObserverService() {
                        @Override
                        protected void ctrl(SampleSwitcherObservable.SwitcherObserverInformation information) {
                            information.getDisposable().request(4);
                        }

                        @Override
                        protected void onStart() {

                        }

                        @Override
                        protected void onEmit(SwitcherBuffer buffer) throws InterruptedException {
                            System.out.println("ppp ->" + buffer.get());
                        }

                        @Override
                        protected void onError(SwitcherFlowException e) {

                        }

                        @Override
                        protected void onComplete() {

                        }
                    });
        } catch (Exception e) {
            ScheduleHooks.onError(e);
        } finally {
            TimeUnit.SECONDS.sleep(3);
            SwitcherFactory.shutdown();
        }
    }

}
