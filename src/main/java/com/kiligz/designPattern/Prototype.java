package com.kiligz.designPattern;

import java.io.*;

/**
 * 原型模式接口，提供默认的浅拷贝、深拷贝（序列化方式）方法；
 *
 * @author Ivan
 */
@SuppressWarnings("unchecked")
public interface Prototype<T> extends Cloneable, Serializable {
    default T shallowCopy() throws RuntimeException {
        try {
            return (T) clone();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    default T deepCopy() throws RuntimeException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)
        ) {
            oos.writeObject(this);
            oos.flush();
            try (ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
                 ObjectInputStream ois = new ObjectInputStream(bis)
            ) {
                return (T) ois.readObject();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    Object clone() throws CloneNotSupportedException;
}

