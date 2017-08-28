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
import com.hujian.switcher.flowable.SwitcherDisposable;
import com.hujian.switcher.flowable.SwitcherFlowException;
import com.hujian.switcher.utils.SwitcherFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created by hujian06 on 2017/8/23.
 */
@SuppressWarnings(value = "unchecked")
public class BlockFlowDemo {
    public static void main(String ... args) throws InterruptedException {
        try {
            SwitcherFactory
                    .createSwitcherObservable()
                    .switchToMultiComputeExecutor(true)
                    .transToRichnessSwitcher()
                    .transToResultfulSwitcher()
                    .transToSampleSwitcherObservable()
                    .createSwitcherBlockingObservable(new SwitcherBlockingObservableOnSubscribe() {
                        @Override
                        public void subscribe(SwitcherBlockingObserverService observer) throws InterruptedException,
                                InstantiationException, SwitcherClassTokenErrException, IllegalAccessException {
                            observer.send("hujian");
                            observer.send("hujian");
                            observer.send("hujian");
                            observer.send("hujian");
                            observer.send("hujian");
                            observer.send("hujian");
                        }
                    })
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

                            String data = (String) buffer.get();

                            System.out.println("recv data:" + data);
                            //TimeUnit.SECONDS.sleep(1);

                        }

                        @Override
                        protected void onError(SwitcherFlowException e) {

                        }

                        @Override
                        protected void onComplete() {

                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            TimeUnit.SECONDS.sleep(1);
            SwitcherFactory.shutdown();
        }
    }
}
