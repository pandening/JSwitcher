package com.hujian.switcher;

import com.google.common.base.Preconditions;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.concurrent.ExecutorService;

/**
 * Created by hujian06 on 2017/8/19.
 */
public class RichnessSwitcher extends SampleSwitcher implements RichnessSwitcherIface {
    private static Logger LOGGER = Logger.getLogger(RichnessSwitcher.class);

    @Override
    public RichnessSwitcherIface assignName(String executorName) {
        Preconditions.checkArgument(executorName != null && !executorName.isEmpty(),
                "empty executor Named at 'assign' action time.");
        if (ExecutorType.EMPTY_EXECUTOR_SERVICE.getName().equals(getCurrentExecutorService().getExecutorType())
                || getCurrentExecutorService().isAssignedName()) {
            LOGGER.error("oops,the current executor had been assigned, or it is null.");
            return this;
        }

        getCurrentExecutorService().setExecutorName(executorName);
        return this;
    }

    /**
     * just for test
     * @return
     */
    @Override
    public SwitcherWithExtraData getSwitcherWithExtraData() throws InterruptedException {
        SwitcherWithExtraData switcherWithExtraData =  new SwitcherWithExtraData(getCurrentExecutorService().getExecutorName(),
                this);
        return switcherWithExtraData;
    }

    @Override
    public RichnessSwitcherIface switchTo(String executorName, Boolean magicOperator,
                                                 Boolean isCreateMode, String createExecutorType) throws InterruptedException {
        Preconditions.checkArgument(executorName != null && !executorName.isEmpty(),
                "empty executor Named at 'assign' action time.");

        if (getCurrentExecutorService() != null && getCurrentExecutorService().getExecutorName().equals(executorName)) {
            return this;
        }

        ExecutorService executorService = null;
        SwitchExecutorServiceEntry executorServiceEntry;
        Iterator<SwitchExecutorServiceEntry> iterator = switchExecutorServicesQueue.iterator();

        while (iterator.hasNext()) {
            executorServiceEntry = iterator.next();
            if (executorName.equals(executorServiceEntry.getExecutorName())) {
                executorService = executorServiceEntry.getExecutorService();
                break;
            }
        }

        //find a not named.
        if (null == executorService && magicOperator && isCreateMode) {
            iterator = switchExecutorServicesQueue.iterator();
            while (iterator.hasNext()) {
                executorServiceEntry = iterator.next();
                if (!executorServiceEntry.isAssignedName()
                        && createExecutorType.equals(executorServiceEntry.getExecutorType())) {
                    executorServiceEntry.setExecutorName(executorName);
                    executorService = executorServiceEntry.getExecutorService();
                }
            }

            //still null.just create an new
            if (null == executorService) {
                executorService = createExecutorService(executorName);
            }
        }

        switchExecutorService(createExecutorType, executorService);
        //do not forget assign the executor name
        assignName(executorName);

        return this;
    }

    @Override
    public RichnessSwitcherIface apply(Runnable job, String executorName, Boolean isCreateMode,
                                       String createExecutorType) throws InterruptedException, SwitchRunntimeException {
        //switch to the thread
        switchTo(executorName, false, isCreateMode, createExecutorType);

        //run the job on the current executor
        if (getCurrentExecutorService().getExecutorService() != null) {
            if (getCurrentExecutorService().getExecutorService().isShutdown()) {
                throw new SwitchRunntimeException("Executor [" + getCurrentExecutorService().getExecutorService()
                        + "] had been shutdown");
            } else {
                getCurrentExecutorService().getExecutorService().submit(job);
            }
        } else {
            throw new SwitchRunntimeException("No Executor To Run Job:[" + job + "]");
        }

        return this;
    }

    @Override
    public ResultfulSwitcher transToResultfulSwitcher() {
        return (ResultfulSwitcher) this;
    }

}
