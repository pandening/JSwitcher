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


import com.hujian.schedulers.AbstractScheduleRunner;
import com.hujian.schedulers.SwitcherFitter;
import com.hujian.schedulers.SwitcherResultFuture;
import com.hujian.schedulers.core.Schedulers;

import java.util.concurrent.Executors;

/**
 * Created by hujian06 on 2017/8/31.
 * just test the schedule-switcher
 */
@SuppressWarnings(value = "unchecked")
public class Test {
    public static void main(String ... args)
            throws Exception {

        SwitcherResultFuture<String> future = new SwitcherResultFuture<>();

        SwitcherFitter.switcherFitter()
                .switchToIoSchedule()
                .switchToComputeSchedule()
                .switchToNewSchedule()
                .switchToIoSchedule()
                .switchToSingleSchedule()
                .switchTo(Schedulers.from(Executors.newFixedThreadPool(1)))
                .fit(new AbstractScheduleRunner() {
                    @Override
                    protected Object realRun() {
                        return "0->" +Thread.currentThread().getName();
                    }
                    @Override
                    protected Object fallback(Exception e) {
                        return "1->"+Thread.currentThread().getName();
                    }
                },future,false);

        System.out.println("get data from future:" + future.fetchResult());
    }
}
