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

package com.hujian.switcher.flowable;

import com.google.common.base.Preconditions;
import org.apache.log4j.Logger;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by hujian06 on 2017/8/23.
 */
public class BlockingSwitcherBuffer<T> implements SwitcherBuffer<T> {
    private static final Logger LOGGER = Logger.getLogger(BlockingSwitcherBuffer.class);

    private static final int BLOCK_QUEUE_DEFAULT_SIZE = 1024;

    private int blockQueueSize = 0;

    private BlockingQueue<T> blockingQueue = null;

    public BlockingSwitcherBuffer() {
        this(BLOCK_QUEUE_DEFAULT_SIZE);
        this.blockQueueSize = BLOCK_QUEUE_DEFAULT_SIZE;
    }

    public BlockingSwitcherBuffer(int blockingQueueSize) {
        blockingQueue = new ArrayBlockingQueue<T>(blockingQueueSize);
        this.blockQueueSize = blockingQueueSize;

    }

    @Override
    public void put(T data, Class clsToken) throws InterruptedException,
            IllegalAccessException, InstantiationException, SwitcherClassTokenErrException {
        Preconditions.checkArgument(clsToken != null , "class token is null");
        if (clsToken.newInstance() instanceof SwitcherProducerClassToken) {
            blockingQueue.put(data);
        } else {
            throw new SwitcherClassTokenErrException("you can not access this method");
        }
    }

    @Override
    public T get() throws InterruptedException {
        return blockingQueue.poll();
    }

    public int getBlockQueueSize() {
        return blockQueueSize;
    }

}
