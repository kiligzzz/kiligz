package com.kiligz.io;

import com.kiligz.concurrent.ReentrantSpinLock;
import lombok.Getter;

import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;

/**
 * 抽象内存映射流
 *
 * @author ivan.zhu
 * @since 2024/7/4
 */
public abstract class AbstractMappedStream implements AutoCloseable {
    /**
     * 用4个字节存储bytes数组长度
     */
    protected static final int INT_LENGTH = 4;

    /**
     * 操作的文件路径
     */
    @Getter
    protected String filePath;

    /**
     * 单位byte
     */
    protected long ioMappedSize;

    /**
     * 随机访问流
     */
    protected RandomAccessFile raf;

    /**
     * 读取位置
     */
    protected long position;

    /**
     * 内存映射buffer
     */
    protected MappedByteBuffer mbb;

    /**
     * 可重入自旋锁，因为直接操作内存，所以耗时相对都很短，所以用乐观锁机制
     */
    protected final ReentrantSpinLock lock = new ReentrantSpinLock();

    /**
     * 默认映射可用内存的10%
     */
    protected static long defaultMappedSize() {
        long free = Runtime.getRuntime().freeMemory();
        return (long) (free * 0.1);
    }
}
