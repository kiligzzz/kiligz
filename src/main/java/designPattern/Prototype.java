package designPattern;

import java.io.*;

/**
 * 原型模式接口，提供默认的浅拷贝、深拷贝（序列化方式）方法；
 * 浅拷贝需调用实体类实现clone()方法，且需public修饰
 *
 * @author Ivan
 */
@SuppressWarnings("unchecked")
public interface Prototype<T> extends Cloneable, Serializable {
    default T shallowCopy() throws Exception {
        return (T) this.getClass().getDeclaredMethod("clone").invoke(this);
    }

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
}

