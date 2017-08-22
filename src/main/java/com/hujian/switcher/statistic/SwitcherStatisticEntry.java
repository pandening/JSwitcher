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

package com.hujian.switcher.statistic;

import com.google.common.util.concurrent.AtomicDouble;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by hujian06 on 2017/8/22.
 */
public class SwitcherStatisticEntry {

    private String callTag = "";
    private List<String> callParamList = null;
    private List<Exception> exceptionList = null;

    private AtomicLong callSucCount = new AtomicLong(0);
    private AtomicLong callErrCount = new AtomicLong(0);

    private AtomicLong curCallSucTime = new AtomicLong(0);
    private AtomicDouble callSucMeanTime = new AtomicDouble(0.0);
    private AtomicLong callSucLowTime = new AtomicLong(0);
    private AtomicLong callSucHighTime = new AtomicLong(0);
    private AtomicLong curCallErrTime = new AtomicLong(0);
    private AtomicDouble callErrMeanTime = new AtomicDouble(0.0);
    private AtomicLong callErrLowTime = new AtomicLong(0);
    private AtomicLong callErrHighTime = new AtomicLong(0);

    public SwitcherStatisticEntry() {
        callParamList = new Vector<>();
        exceptionList = new Vector<>();
    }

    public String getCallTag() {
        return callTag;
    }

    public void setCallTag(String callTag) {
        this.callTag = callTag;
    }

    public List<Exception> getExceptionList() {
        return exceptionList;
    }

    public void setExceptionList(List<Exception> exceptionList) {
        this.exceptionList = exceptionList;
    }

    public List<String> getCallParamList() {
        return callParamList;
    }

    public void setCallParamList(List<String> callParamList) {
        this.callParamList = callParamList;
    }

    public AtomicLong getCallSucCount() {
        return callSucCount;
    }

    public void setCallSucCount(AtomicLong callSucCount) {
        this.callSucCount = callSucCount;
    }

    public AtomicLong getCallErrCount() {
        return callErrCount;
    }

    public void setCallErrCount(AtomicLong callErrCount) {
        this.callErrCount = callErrCount;
    }

    public AtomicDouble getCallSucMeanTime() {
        return callSucMeanTime;
    }

    public void setCallSucMeanTime(AtomicDouble callSucMeanTime) {
        this.callSucMeanTime = callSucMeanTime;
    }

    public AtomicLong getCallSucLowTime() {
        return callSucLowTime;
    }

    public void setCallSucLowTime(AtomicLong callSucLowTime) {
        this.callSucLowTime = callSucLowTime;
    }

    public AtomicLong getCallSucHighTime() {
        return callSucHighTime;
    }

    public void setCallSucHighTime(AtomicLong callSucHighTime) {
        this.callSucHighTime = callSucHighTime;
    }

    public AtomicDouble getCallErrMeanTime() {
        return callErrMeanTime;
    }

    public void setCallErrMeanTime(AtomicDouble callErrMeanTime) {
        this.callErrMeanTime = callErrMeanTime;
    }

    public AtomicLong getCallErrLowTime() {
        return callErrLowTime;
    }

    public void setCallErrLowTime(AtomicLong callErrLowTime) {
        this.callErrLowTime = callErrLowTime;
    }

    public AtomicLong getCallErrHighTime() {
        return callErrHighTime;
    }

    public void setCallErrHighTime(AtomicLong callErrHighTime) {
        this.callErrHighTime = callErrHighTime;
    }

    public AtomicLong getCurCallSucTime() {
        return curCallSucTime;
    }

    public void setCurCallSucTime(AtomicLong curCallSucTime) {
        this.curCallSucTime = curCallSucTime;
    }

    public AtomicLong getCurCallErrTime() {
        return curCallErrTime;
    }

    public void setCurCallErrTime(AtomicLong curCallErrTime) {
        this.curCallErrTime = curCallErrTime;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(callTag);
        sb.append("\nparams:\n");
        for (String param : callParamList) {
            sb.append(param);
            sb.append("\n");
        }
        sb.append("totalSucCall:");sb.append(callSucCount);
        sb.append("\ntotalErrCall:");sb.append(callErrCount);
        sb.append("\ncurrentSucCallTime|currentErrCallTime = ");
        sb.append(curCallSucTime);sb.append("|");sb.append(curCallErrTime);
        sb.append("\nsucLowestCallTime|sucHighestCallTime|sucMeanCallTime = ");
        sb.append(callSucLowTime);sb.append("|");sb.append(callSucHighTime);sb.append("|");sb.append(callSucMeanTime);
        sb.append("\nerrLowestCallTime|errHighestCallTime|errMeanCallTime = ");
        sb.append(callErrLowTime);sb.append("|");sb.append(callErrHighTime);sb.append("|");sb.append(callErrMeanTime);

        return sb.toString();
    }
}
