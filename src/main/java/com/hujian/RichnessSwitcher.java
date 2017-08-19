package com.hujian;

/**
 * Created by hujian06 on 2017/8/19.
 */
public final class RichnessSwitcher implements Switcher {

    @Override
    public void clear() throws InterruptedException {

    }

    @Override
    public Switcher shutdown() {
        return null;
    }

    @Override
    public Switcher shutdownNow() {
        return null;
    }

    @Override
    public Switcher apply(Runnable job, Boolean isCreateMode) throws SwitchRunntimeException, InterruptedException {
        return null;
    }

    @Override
    public Switcher switchToIoExecutor(Boolean isCreateMode) throws InterruptedException {
        return null;
    }

    @Override
    public Switcher switchToMultiIoExecutor(Boolean isCreateMode) throws InterruptedException {
        return null;
    }

    @Override
    public Switcher switchToNewIoExecutor() throws InterruptedException {
        return null;
    }

    @Override
    public Switcher switchToNewMultiIoExecutor() throws InterruptedException {
        return null;
    }

    @Override
    public Switcher switchToComputeExecutor(Boolean isCreateMode) throws InterruptedException {
        return null;
    }

    @Override
    public Switcher switchToMultiComputeExecutor(Boolean isCreateMode) throws InterruptedException {
        return null;
    }

    @Override
    public Switcher switchToNewComputeExecutor() throws InterruptedException {
        return null;
    }

    @Override
    public Switcher switchToNewMultiComputeExecutor() throws InterruptedException {
        return null;
    }

    @Override
    public Switcher switchToSingleExecutor(Boolean isCreateMode) throws InterruptedException {
        return null;
    }

    @Override
    public Switcher switchToNewSingleExecutor() throws InterruptedException {
        return null;
    }

    @Override
    public Switcher switchToNewExecutor() throws InterruptedException {
        return null;
    }

    @Override
    public Switcher switchBackToIoExecutor(Boolean isCreateMode) throws InterruptedException {
        return null;
    }

    @Override
    public Switcher switchBackToMultiIoExecutor(Boolean isCreateMode) throws InterruptedException {
        return null;
    }

    @Override
    public Switcher switchBackToComputeExecutor(Boolean isCreateMode) throws InterruptedException {
        return null;
    }

    @Override
    public Switcher switchBackToMultiComputeExecutor(Boolean isCreateMode) throws InterruptedException {
        return null;
    }

    @Override
    public Switcher switchAfterIOWork(Runnable job, Boolean isMultiMode, Boolean isCreateMode) throws SwitchRunntimeException, InterruptedException {
        return null;
    }

    @Override
    public Switcher switchAfterComputeWork(Runnable job, Boolean isMultiMode, Boolean isCreateMode) throws SwitchRunntimeException, InterruptedException {
        return null;
    }

    @Override
    public Switcher switchAfterWork(Runnable job, Boolean isMultiMode, Boolean isCreateMode) throws SwitchRunntimeException, InterruptedException {
        return null;
    }

    @Override
    public Switcher switchBeforeIoWork(Runnable job, Boolean isMultiMode, Boolean isCreateMode) throws InterruptedException {
        return null;
    }

    @Override
    public Switcher switchBeforeComputeWork(Runnable job, Boolean isMultiMode, Boolean isCreateMode) throws InterruptedException {
        return null;
    }

    @Override
    public Switcher switchBeforeWork(Runnable job, Boolean isMultiMode, Boolean isCreateMode) throws InterruptedException {
        return null;
    }
}
