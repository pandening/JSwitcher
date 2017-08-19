package com.hujian;

import java.util.concurrent.ExecutorService;

/**
 * Created by hujian06 on 2017/8/18.
 */
public class SwitchExecutorServiceEntry {

    private static final String DEFAULT_EXECUTOR_NAME = "unNamed-executorService";

    private String executorType;
    private String executorName;
    private ExecutorService executorService;

    private volatile boolean isAssignedName = false;

    /**
     * get an empty switch executor Service entry
     * @return
     */
    public static SwitchExecutorServiceEntry emptyEntry() {
        return new SwitchExecutorServiceEntry();
    }

    /**
     * empty (default) constructor
     */
    public SwitchExecutorServiceEntry() {
        this.executorType = ExecutorType.EMPTY_EXECUTOR_SERVICE.getName();
        this.executorName = DEFAULT_EXECUTOR_NAME;
        this.executorService = null;
    }

    public SwitchExecutorServiceEntry(String executorType, ExecutorService executorService) {
        this.executorType = executorType;
        this.executorService = executorService;
        this.executorName = DEFAULT_EXECUTOR_NAME;
    }

    public SwitchExecutorServiceEntry(String executorType, ExecutorService executorService, String executorName) {
        this.executorType = executorType;
        this.executorName = executorName;
        this.executorService = executorService;
        this.isAssignedName = true;
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

    public String getExecutorName() {
        return executorName;
    }

    public void setExecutorName(String executorName) {
        if (!isAssignedName) {
            this.executorName = executorName;
            this.isAssignedName = true;
        }
    }

    public boolean isAssignedName() {
        return isAssignedName;
    }
}
