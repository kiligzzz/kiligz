package com.kiligz.io;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 线程安全的内存映射输入流
 *
 * @author Ivan
 * @since 2024/7/3
 */
@Slf4j
@SuppressWarnings("all")
public class MappedInputStream<T> extends AbstractMappedStream {
    private final AtomicReference<T> peekReference = new AtomicReference<>();
    private long fileRemainSize;

    public MappedInputStream(String filePath) {
        this(filePath, defaultMappedSize());
    }

    public MappedInputStream(String filePath, long ioMappedSize) {
        this.filePath = filePath;
        this.ioMappedSize = ioMappedSize;
        try {
            this.raf = new RandomAccessFile(filePath, "r");
            this.fileRemainSize = raf.getChannel().size();
            resetMbb();
        } catch (IOException e) {
            throw new RuntimeException("读取文件失败", e);
        }
    }

    /**
     * 读取一个对象
     */
    public T read() {
        T t = peekReference.getAndSet(null);
        return t != null ? t : readObject();
    }

    /**
     * 偷看当前流读取位置的第一个对象
     */
    public T peek() {
        return peekReference.updateAndGet(peek -> peek != null ? peek : readObject());
    }

    /**
     * 读取一个对象
     */
    private T readObject() {
        lock.lock();
        try {
            if (!mbb.hasRemaining() && fileRemainSize == 0) {
                return null;
            }
            int remaining = mbb.remaining();
            if (remaining <= INT_LENGTH) {
                resetMbb();
                return readObject();
            }
            int byteSize = mbb.getInt();
            int size = byteSize + INT_LENGTH;
            if (size > remaining) {
                resetMbb();
                return readObject();
            }
            byte[] bytes = new byte[byteSize];
            mbb.get(bytes);
            position += size;
            fileRemainSize -= size;
            return getObj(bytes);
        } catch (Exception e) {
            throw new RuntimeException("读取文件失败", e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 读取剩下所有元素
     */
    public List<T> readAll() {
        List<T> list = new ArrayList<>();
        if (!lock.tryLock()) {
            return list;
        }
        try {
            T t = peekReference.getAndSet(null);
            if (t != null) {
                list.add(t);
            }
            mbb = raf.getChannel().map(FileChannel.MapMode.READ_ONLY, position, fileRemainSize);
            while (true) {
                if (!mbb.hasRemaining()) {
                    break;
                }
                int byteSize = mbb.getInt();
                byte[] bytes = new byte[byteSize];
                mbb.get(bytes);
                list.add(getObj(bytes));
            }
            position += fileRemainSize;
            fileRemainSize = 0;
            return list;
        } catch (Exception e) {
            throw new RuntimeException("读取文件失败", e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 重新从头开始读取
     */
    public void reset() {
        lock.lock();
        try {
            position = 0;
            fileRemainSize = raf.getChannel().size();
            peekReference.set(null);
            resetMbb();
        } catch (IOException e) {
            throw new RuntimeException("设置重新从头开始读取失败");
        } finally {
            lock.unlock();
        }
    }

    /**
     * 关闭输入流
     */
    @Override
    public void close() {
        try {
            raf.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 重新内存映射
     */
    private void resetMbb() throws IOException {
        long bufferSize = fileRemainSize > ioMappedSize ? ioMappedSize : fileRemainSize;
        this.mbb = raf.getChannel().map(FileChannel.MapMode.READ_ONLY, position, bufferSize);
    }

    /**
     * bytes数组转对象
     */
    private T getObj(byte[] bytes) throws Exception {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            return (T) ois.readObject();
        }
    }
}