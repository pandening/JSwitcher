package com.hujian.switcher;

/**
 * Created by hujian06 on 2017/8/18.
 */
public enum ExecutorType {
    IO_EXECUTOR_SERVICE(1, "io-executorService"),
    MULTI_IO_EXECUTOR_SERVICE(2, "multi-io-executorService"),
    COMPUTE_EXECUTOR_SERVICE(3, "compute-executorService"),
    MULTI_COMPUTE_EXECUTOR_SERVICE(4, "multi-compute-executorService"),
    SINGLE_EXECUTOR_SERVICE(5, "single-executorService"),
    NEW_EXECUTOR_SERVICE(6, "new-executorService"),
    EMPTY_EXECUTOR_SERVICE(7, "empty-executorService"),
    DEFAULT_RUN_EXECUTOR_SERVICE(8, "default-executorService");

    private int index;
    private String name;

    ExecutorType(int index, String name) {
        this.index = index;
        this.name = name;
    }

    public static String getName(int index) {
        for (ExecutorType c : ExecutorType.values()) {
            if (c.getIndex() == index) {
                return c.name;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
