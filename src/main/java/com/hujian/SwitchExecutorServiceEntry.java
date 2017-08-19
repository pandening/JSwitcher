package com.hujian;

import java.util.concurrent.ExecutorService;

/**
 * Created by hujian06 on 2017/8/18.
 */
public class SwitchExecutorServiceEntry {

    private String executorType;
    private ExecutorService executorService;

    /**
     * empty (default) constructor
     */
    public SwitchExecutorServiceEntry() {
        this.executorType = ExecutorType.EMPTY_EXECUTOR_SERVICE.getName();
        executorService = null;
    }

    /**
     * get an empty switch executor Service entry
     * @return
     */
    public static SwitchExecutorServiceEntry emptyEntry() {
        return new SwitchExecutorServiceEntry();
    }

    public SwitchExecutorServiceEntry(String executorType, ExecutorService executorService) {
        this.executorType = executorType;
        this.executorService = executorService;
    }

    public String getExecutorType() {
        return executorType;
    }

    public void setExecutorType(String executorType) {
        this.executorType = executorType;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }
}
