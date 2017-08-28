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

package com.hujian.switcher.statistic;

import java.util.Map;

/**
 * Created by hujian06 on 2017/8/22.
 */
public interface Statistic {

    void incSucCount(String session, String tag, String params);

    void incErrCount(String session, String tag, String params, Exception err);

    void setSucTime(String session, String tag, String params, long mills);

    void setErrTime(String session, String tag, String params, long mills);

    long getSucCount(String session, String tag);

    long getErrCount(String session, String tag);

    double getSucMeanTime(String session, String tag);

    long getSucLowTime(String session, String tag);

    long getSucHighTime(String session, String tag);

    double getErrMeanTime(String session, String tag);

    long getErrLowTime(String session, String tag);

    long getErrHighTime(String session, String tag);

    Map<String, SwitcherStatisticEntry> getSwitchStatisticEntryBySessionId(String sessionId);

}
