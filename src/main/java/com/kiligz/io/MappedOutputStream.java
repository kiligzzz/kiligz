package com.kiligz.io;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

/**
 * 线程安全的内存映射输出流
 *
 * @author Ivan
 * @since 2024/7/3
 */
@Slf4j
public class MappedOutputStream<T> extends AbstractMappedStream {
    public MappedOutputStream(String filePath) {
        this(filePath, defaultMappedSize());
    }

    public MappedOutputStream(String filePath, long ioMappedSize) {
        try {
            this.filePath = filePath;
            this.ioMappedSize = ioMappedSize;
            this.raf = new RandomAccessFile(filePath, "rw");
            this.mbb = raf.getChannel().map(FileChannel.MapMode.READ_WRITE, position, ioMappedSize);
        } catch (IOException e) {
            throw new RuntimeException("写入文件失败", e);
        }
    }

    /**
     * 写入一个对象
     */
    public void write(T t) {
        try {
            byte[] bytes = getBytes(t);
            // 加锁写入对象
            lock.lock();
            try {
                int size = bytes.length + INT_LENGTH;
                if (mbb.remaining() < size) {
                    resetMbb();
                }
                mbb.putInt(bytes.length);
                mbb.put(bytes);
                position += size;
            } finally {
                lock.unlock();
            }
        } catch (IOException e) {
            throw new RuntimeException("写入文件失败", e);
        }
    }

    /**
     * 关闭输出流
     */
    @Override
    public void close() {
        try {
            mbb.force();
            raf.setLength(position);
            raf.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 重新内存映射
     */
    private void resetMbb() throws IOException {
        mbb.force();
        mbb = raf.getChannel().map(FileChannel.MapMode.READ_WRITE, position, ioMappedSize);
    }

    /**
     * 对象转bytes数组
     */
    private static byte[] getBytes(Object obj) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
            oos.flush();
            return bos.toByteArray();
        }
    }
}