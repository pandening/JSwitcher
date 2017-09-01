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
import com.hujian.switcher.flowable.SwitcherFlowException;
import com.hujian.switcher.flowable.SwitcherObservableOnSubscribe;
import com.hujian.switcher.flowable.SwitcherObservableService;
import com.hujian.switcher.schedulers.ScheduleHooks;
import com.hujian.switcher.utils.SwitcherFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created by hujian06 on 2017/8/23.
 */
@SuppressWarnings(value = "unchecked")
public class SampleFlowableDemo {
    public static void main(String ... args) throws InterruptedException {
        try {
            SwitcherFactory
                    .createSwitcherObservable()
                    .switchToComputeExecutor(true)
                    .transToRichnessSwitcher()
                    .transToResultfulSwitcher()
                    .transToSampleSwitcherObservable()
                    .createSwitcherObservable(new SwitcherObservableOnSubscribe() {
                        @Override
                        public void subscribe(SwitcherObservableService observer) throws InterruptedException {
                            observer.send("hujian");
                            observer.send("hujian");
                            observer.send("hujian");
                            observer.send("hujian");
                        }
                    })
                    .delaySubscribe(new SwitcherObservableService() {
                        @Override
                        protected void ctrl(SampleSwitcherObservable.SwitcherObserverInformation information) {
                            information.getDisposable().request(2);
                        }

                        @Override
                        protected void onStart() {

                        }

                        @Override
                        protected void onEmit(Object data) throws InterruptedException {
                            System.out.println("recv data from observable:" + data);
                        }

                        @Override
                        protected void onError(SwitcherFlowException e) {

                        }

                        @Override
                        protected void onComplete() {

                        }
                    }, TimeUnit.MICROSECONDS, 100);
                    /*.subscribe(new SwitcherObservableService() {
                        @Override
                        protected void ctrl(SampleSwitcherObservable.SwitcherObserverInformation information) {
                            information.getDisposable().request(2);
                        }

                        @Override
                        protected void onStart() {

                        }

                        @Override
                        protected void onEmit(Object data) throws InterruptedException {
                            System.out.println("recv data:" + data);
                        }

                        @Override
                        protected void onError(SwitcherFlowException e) {

                        }

                        @Override
                        protected void onComplete() {

                        }
                    });*/
        } catch (Exception e) {
            ScheduleHooks.onError(e);
        } finally {
            TimeUnit.SECONDS.sleep(2);
            SwitcherFactory.shutdown();
        }
    }

}
