package com.kiligz.designPattern;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 备忘录模式接口，搭配深拷贝（原型模式）；
 * 提供保存、恢复备忘录、深拷贝方法；
 * 每个对象根据key操作自己的备忘录，可有相同的key
 *
 * @author Ivan
 */
@SuppressWarnings("unchecked")
public interface Memento<T> extends Serializable {
    /**
     * 备忘录map，对象唯一id::key -> Memento（对象副本）
     */
    Map<String, Memento<?>> MEMENTO_MAP = new ConcurrentHashMap<>();

    /**
     * 保存备忘录
     */
    default void saveMemento(String key) {
        String mementoKey = System.identityHashCode(this) + "-" + key;
        Memento<T> memento = (Memento<T>) this.deepCopy();
        MEMENTO_MAP.put(mementoKey, memento);
    }

    /**
     * 恢复备忘录
     */
    default T restoreMemento(String key) {
        String mementoKey = System.identityHashCode(this) + "-" + key;
        return (T) MEMENTO_MAP.get(mementoKey);
    }

    /**
     * 清理备忘录
     */
    default void clearMemento(String key) {
        String mementoKey = System.identityHashCode(this) + "-" + key;
        MEMENTO_MAP.remove(mementoKey);
    }

    /**
     * 深拷贝
     */
    default T deepCopy() {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oo = new ObjectOutputStream(bos)
        ) {
            oo.writeObject(this);
            oo.flush();
            try (ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
                 ObjectInputStream oi = new ObjectInputStream(bis)
            ) {
                return (T) oi.readObject();
            }
        } catch (Exception e) {
            throw new RuntimeException("deep copy error", e);
        }
    }

    /**
     * 备忘录信息
     */
    default String infoMementoMap() {
        return MEMENTO_MAP.toString();
    }
}

