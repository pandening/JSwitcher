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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.AtomicDouble;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by hujian06 on 2017/8/22.
 */
public class SampleSwitcherStatistic implements Statistic {
    private static final Logger LOGGER = Logger.getLogger(SampleSwitcherStatistic.class);

    private static SampleSwitcherStatistic SAMPLE_SWITCHER_STATISTIC = new SampleSwitcherStatistic();

    private SwitcherStatisticStrategy paramStatisticStrategy = null;
    private SwitcherStatisticStrategy exceptionStatisticStrategy = null;

    private AtomicLong totalSucCallCount;
    private AtomicLong totalErrCallCount;

    private AtomicLong totalSucCallTime;
    private AtomicLong lowestSucCallTime;
    private AtomicLong highestSucCallTime;

    private AtomicLong totalErrCallTime;
    private AtomicLong lowestErrCallTime;
    private AtomicLong highestErrCallTime;

    private AtomicDouble meanSucTime;
    private AtomicDouble meanErrTime;

    private ConcurrentMap<String, ConcurrentHashMap<String, SwitcherStatisticEntry>> switcherStatisticEntryMap = null; // session -> {tag->statistic }

    private SampleSwitcherStatistic() {
        switcherStatisticEntryMap = new ConcurrentHashMap<>();

        totalErrCallCount = new AtomicLong(0);
        totalSucCallCount = new AtomicLong(0);

        totalSucCallTime = new AtomicLong(0);
        lowestSucCallTime = new AtomicLong(Long.MAX_VALUE);
        highestSucCallTime = new AtomicLong(0);

        totalErrCallTime = new AtomicLong(0);
        lowestErrCallTime = new AtomicLong(Long.MAX_VALUE);
        highestErrCallTime = new AtomicLong(0);

        meanSucTime = new AtomicDouble(0.0);
        meanErrTime = new AtomicDouble(0.0);

        paramStatisticStrategy = SwitcherStatisticStrategy.STATISTIC_FULL_TIME_PARAMS;
        exceptionStatisticStrategy = SwitcherStatisticStrategy.STATISTIC_FULL_TIME_EXCEPTION;
    }

    public static SampleSwitcherStatistic getInstance() {
        if (SAMPLE_SWITCHER_STATISTIC == null) {
            synchronized (SampleSwitcherStatistic.class) {
                if (SAMPLE_SWITCHER_STATISTIC == null) {
                    SAMPLE_SWITCHER_STATISTIC = new SampleSwitcherStatistic();
                }
            }
        }

        return SAMPLE_SWITCHER_STATISTIC;
    }

    public ConcurrentMap<String, ConcurrentHashMap<String, SwitcherStatisticEntry>> getStatisticMap() {
        return switcherStatisticEntryMap;
    }

    @Override
    public Map<String, SwitcherStatisticEntry> getSwitchStatisticEntryBySessionId(String sessionId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(sessionId), "sessionId must not be null");
        if (switcherStatisticEntryMap.containsKey(sessionId)) {
            return switcherStatisticEntryMap.get(sessionId);
        } else {
            return Collections.emptyMap();
        }
    }

    private void requireEnv(String session, String tag) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(session) && !Strings.isNullOrEmpty(tag),
                "session == null || tag == null");
    }

    /**
     *
     * @param entry
     * @param params
     * @param e
     */
    private void handleStrategy(SwitcherStatisticEntry entry, String params, Exception e) {
        List<String> paramList = entry.getCallParamList();
        List<Exception> exceptionList = entry.getExceptionList();
        switch (paramStatisticStrategy) {
            case STATISTIC_LAST_TIME_EXCEPTION:
                paramList = new Vector<>();
                paramList.add(params);
                break;
            case STATISTIC_FIRST_TIME_EXCEPTION:
                if (paramList.isEmpty()) {
                    paramList.add(params);
                }
                break;
            case STATISTIC_FULL_TIME_EXCEPTION:
                paramList.add(params);
                break;
            default://ignore
                break;
        }

        switch (exceptionStatisticStrategy) {
            case STATISTIC_LAST_TIME_PARAMS:
                exceptionList = new Vector<>();
                exceptionList.add(e);
                break;
            case STATISTIC_FIRST_TIME_PARAMS:
                if (exceptionList.isEmpty()) {
                    exceptionList.add(e);
                }
                break;
            case STATISTIC_FULL_TIME_PARAMS:
                exceptionList.add(e);
                break;
            default://ignore
                break;
        }
    }

    @Override
    public void incSucCount(String session, String tag, String params) {
        requireEnv(session, tag);
        ConcurrentMap<String, SwitcherStatisticEntry> statisticEntryConcurrentMap
                = switcherStatisticEntryMap.get(session);
        if (statisticEntryConcurrentMap == null) {
            statisticEntryConcurrentMap = new ConcurrentHashMap<>();
        }
        SwitcherStatisticEntry entry = statisticEntryConcurrentMap.get(tag);
        if (entry == null) {
            entry = new SwitcherStatisticEntry();
            entry.setCallTag(tag);
        }

        entry.getCallSucCount().incrementAndGet();

        handleStrategy(entry, params, null);

        statisticEntryConcurrentMap.put(tag, entry);
        switcherStatisticEntryMap.put(session,
                (ConcurrentHashMap<String, SwitcherStatisticEntry>) statisticEntryConcurrentMap);
    }

    @Override
    public void incErrCount(String session, String tag, String params, Exception err) {
        requireEnv(session, tag);
        ConcurrentMap<String, SwitcherStatisticEntry> statisticEntryConcurrentMap
                = switcherStatisticEntryMap.get(session);
        SwitcherStatisticEntry entry = statisticEntryConcurrentMap.get(tag);
        if (entry == null) {
            entry = new SwitcherStatisticEntry();
            entry.setCallTag(tag);
        }

        entry.getCallErrCount().incrementAndGet();

        handleStrategy(entry, params, err);

        statisticEntryConcurrentMap.put(tag, entry);
        switcherStatisticEntryMap.put(session,
                (ConcurrentHashMap<String, SwitcherStatisticEntry>) statisticEntryConcurrentMap);
    }

    @Override
    public void setSucTime(String session, String tag, String params, long mills) {
        requireEnv(session, tag);
        ConcurrentMap<String, SwitcherStatisticEntry> statisticEntryConcurrentMap
                = switcherStatisticEntryMap.get(session);
        if (statisticEntryConcurrentMap == null) {
            statisticEntryConcurrentMap = new ConcurrentHashMap<>();
        }
        SwitcherStatisticEntry entry = statisticEntryConcurrentMap.get(tag);
        if (entry == null) {
            entry = new SwitcherStatisticEntry();
            entry.setCallTag(tag);
        }

        entry.getCurCallSucTime().set(mills);

        if (totalSucCallTime.getAndAdd(mills) >= Long.MAX_VALUE) {
            totalSucCallTime.set(mills);
            totalSucCallCount.set(1);
        } else {
            long time = totalSucCallCount.incrementAndGet();
        }

        if (mills > highestSucCallTime.get()) {
            highestSucCallTime.set(mills);
        }

        if (mills < lowestSucCallTime.get()) {
            lowestSucCallTime.set(mills);
        }

        double meanSuc;
        if (totalSucCallCount.get() != 0) {
            meanSuc = totalSucCallTime.doubleValue() / totalSucCallCount.get();
        } else {
            meanSuc = 0.0;
        }

        meanSucTime.set(meanSuc);

        entry.getCurCallSucTime().set(mills);
        entry.getCallSucLowTime().set(lowestSucCallTime.get());
        entry.getCallSucHighTime().set(highestSucCallTime.get());
        entry.getCallSucMeanTime().set(meanSuc);

        handleStrategy(entry, params, null);
    }

    @Override
    public void setErrTime(String session, String tag, String params, long mills) {
        requireEnv(session, tag);
        ConcurrentMap<String, SwitcherStatisticEntry> statisticEntryConcurrentMap
                = switcherStatisticEntryMap.get(session);
        SwitcherStatisticEntry entry = statisticEntryConcurrentMap.get(tag);
        if (entry == null) {
            entry = new SwitcherStatisticEntry();
            entry.setCallTag(tag);
        }

        entry.getCurCallErrTime().set(mills);

        if (totalErrCallTime.getAndAdd(mills) >= Long.MAX_VALUE) {
            totalErrCallTime.set(mills);
            totalErrCallCount.set(1);
        } else {
            totalErrCallCount.incrementAndGet();
        }

        if (mills > highestErrCallTime.get()) {
            highestErrCallTime.set(mills);
        }

        if (mills < lowestErrCallTime.get()) {
            lowestErrCallTime.set(mills);
        }

        double meanErr;
        if (totalErrCallCount.get() != 0) {
            meanErr = totalErrCallTime.doubleValue() / totalErrCallCount.get();
        } else {
            meanErr = 0.0;
        }

        meanErrTime.set(meanErr);

        entry.getCurCallErrTime().set(mills);
        entry.getCallErrLowTime().set(lowestErrCallTime.get());
        entry.getCallErrHighTime().set(highestErrCallTime.get());
        entry.getCallErrMeanTime().set(meanErr);

        handleStrategy(entry, params, null);
    }

    @Override
    public long getSucCount(String session, String tag) {
        requireEnv(session, tag);
        ConcurrentMap<String, SwitcherStatisticEntry> statisticEntryConcurrentMap
                = switcherStatisticEntryMap.get(session);
        SwitcherStatisticEntry entry = statisticEntryConcurrentMap.get(tag);
        if (entry == null) {
            entry = new SwitcherStatisticEntry();
            entry.setCallTag(tag);
        }

        return entry.getCallSucCount().get();
    }

    @Override
    public long getErrCount(String session, String tag) {
        requireEnv(session, tag);
        ConcurrentMap<String, SwitcherStatisticEntry> statisticEntryConcurrentMap
                = switcherStatisticEntryMap.get(session);
        SwitcherStatisticEntry entry = statisticEntryConcurrentMap.get(tag);
        if (entry == null) {
            entry = new SwitcherStatisticEntry();
            entry.setCallTag(tag);
        }

        return entry.getCallErrCount().get();
    }

    @Override
    public double getSucMeanTime(String session, String tag) {
        return meanSucTime.get();
    }

    @Override
    public long getSucLowTime(String session, String tag) {
        return lowestSucCallTime.get();
    }

    @Override
    public long getSucHighTime(String session, String tag) {
        return highestSucCallTime.get();
    }

    @Override
    public double getErrMeanTime(String session, String tag) {
        return meanErrTime.get();
    }

    @Override
    public long getErrLowTime(String session, String tag) {
        return lowestErrCallTime.get();
    }

    @Override
    public long getErrHighTime(String session, String tag) {
        return highestErrCallTime.get();
    }

    public SwitcherStatisticStrategy getParamStatisticStrategy() {
        return paramStatisticStrategy;
    }

    public void setParamStatisticStrategy(SwitcherStatisticStrategy paramStatisticStrategy) {
        this.paramStatisticStrategy = paramStatisticStrategy;
    }

    public SwitcherStatisticStrategy getExceptionStatisticStrategy() {
        return exceptionStatisticStrategy;
    }

    public void setExceptionStatisticStrategy(SwitcherStatisticStrategy exceptionStatisticStrategy) {
        this.exceptionStatisticStrategy = exceptionStatisticStrategy;
    }

}
