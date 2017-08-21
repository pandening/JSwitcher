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

package com.hujian.switcher.utils;

import com.hujian.switcher.ResultfulSwitcher;
import com.hujian.switcher.ResultfulSwitcherIfac;
import com.hujian.switcher.RichnessSwitcher;
import com.hujian.switcher.RichnessSwitcherIface;
import com.hujian.switcher.SampleSwitcher;
import com.hujian.switcher.core.SwitchExecutorService;
import com.hujian.switcher.core.Switcher;

/**
 * Created by hujian06 on 2017/8/19.
 */
public final class SwitcherFactory {

    private static Switcher switcher;
    private static RichnessSwitcherIface richnessSwitcherIface;
    private static ResultfulSwitcherIfac resultfulSwitcherIfac;

    public static ResultfulSwitcherIfac getCurResultfulSwitcherIfac() {
        if (resultfulSwitcherIfac != null) {
            return (ResultfulSwitcherIfac) resultfulSwitcherIfac.getCurrentSwitcher();
        } else if (richnessSwitcherIface != null) {
            return (ResultfulSwitcherIfac) richnessSwitcherIface.getCurrentSwitcher();
        } else if (switcher != null) {
            return (ResultfulSwitcherIfac) switcher.getCurrentSwitcher();
        } else {
            return null;
        }
    }

    public static Switcher createShareSwitcher() {
        if(switcher == null) {
            synchronized (SwitcherFactory.class) {
                if (switcher == null) {
                    switcher = new SampleSwitcher();
                }
            }
        }
        return switcher;
    }

    public static RichnessSwitcherIface createShareRichnessSwitcher() {
        if(richnessSwitcherIface == null) {
            synchronized (SwitcherFactory.class) {
                if (richnessSwitcherIface == null) {
                    richnessSwitcherIface = new RichnessSwitcher();
                }
            }
        }
        return richnessSwitcherIface;
    }

    public static ResultfulSwitcherIfac createResultfulSwitcher() {
        if(resultfulSwitcherIfac == null) {
            synchronized (SwitcherFactory.class) {
                if (resultfulSwitcherIfac == null) {
                    resultfulSwitcherIfac = new ResultfulSwitcher();
                }
            }
        }
        return resultfulSwitcherIfac;
    }

    public static void shutdownSwitcher() throws InterruptedException {
        if (null != switcher) {
            switcher.clear();
        }
    }

    public static void shutdownRichnessSwitcher() throws InterruptedException {
        if (null != richnessSwitcherIface) {
            richnessSwitcherIface.clear();
        }
    }

    public static void shutdownResultfulSwitcher() throws InterruptedException {
        if (null != resultfulSwitcherIfac) {
            resultfulSwitcherIfac.clear();
        }
    }

    public static void shutdown() throws InterruptedException {
        shutdownSwitcher();;
        shutdownRichnessSwitcher();
        shutdownResultfulSwitcher();

        //do not forget the default-executorService
        SwitchExecutorService.defaultRunExecutorService.shutdownNow();
    }

}
