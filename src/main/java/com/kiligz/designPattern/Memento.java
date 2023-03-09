package com.kiligz.designPattern;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

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
    Map<String, Memento<?>> mementoMap = new HashMap<>();

    /**
     * 保存备忘录
     */
    default void saveMemento(String key) throws Exception {
        String mementoKey = System.identityHashCode(this) + "::" + key;
        Memento<T> memento = (Memento<T>) this.deepCopy();
        mementoMap.put(mementoKey, memento);
    }

    /**
     * 恢复备忘录
     */
    default T restoreMemento(String key) {
        String mementoKey = System.identityHashCode(this) + "::" + key;
        return (T) mementoMap.get(mementoKey);
    }

    /**
     * 深拷贝
     */
    default T deepCopy() throws Exception {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutput oo = new ObjectOutputStream(bos)
        ) {
            oo.writeObject(this);
            oo.flush();
            try (ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
                 ObjectInput oi = new ObjectInputStream(bis)
            ) {
                return (T) oi.readObject();
            }
        }
    }

    /**
     * 备忘录信息
     */
    default String infoMementoMap() {
        return mementoMap.toString();
    }
}

