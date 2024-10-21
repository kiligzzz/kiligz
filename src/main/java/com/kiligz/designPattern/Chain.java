package com.kiligz.designPattern;

/**
 * 责任链模式抽象类
 * 可继承实现链式转换器、过滤器等
 *
 * @author ivan.zhu
 */
@SuppressWarnings("all")
public abstract class Chain<T> implements Cloneable {

    /**
     * 下个链
     */
    private Chain<T> next;

    /**
     * 构造责任链
     * 重复对象浅拷贝，避免循环链
     */
    public static <T> Chain<T> build(Chain<T>... chains) {
        if (chains.length == 0)
            return new NoopChain<>();

        Chain<T> head = chains[0];
        Chain<T> cur = head;
        for (int i = 1; i < chains.length; i++) {
            Chain<T> clone = (Chain<T>) chains[i].clone();
            clone.next = null;
            cur = cur.next = clone;
        }
        return head;
    }

    /**
     * 往当前链最后拼接Chain
     * 重复对象浅拷贝，避免循环链
     */
    public void join(Chain<T>... chains) {
        Chain<T> cur = this;
        while (cur.next != null) {
            cur = cur.next;
        }
        for (Chain<T> chain : chains) {
            Chain<T> clone = (Chain<T>) chain.clone();
            clone.next = null;
            cur = cur.next = clone;
        }
    }

    /**
     * 按顺序执行该链及之后所有链逻辑
     */
    public T startup(T data) {
        if (data == null)
            return null;

        data = exec(data);
        return next == null ? data : next.startup(data);
    }

    /**
     * 执行当前链逻辑
     */
    public abstract T exec(T data);

    /**
     * 下一个链
     */
    public Chain<T> next() {
        return next;
    }

    /**
     * 浅拷贝
     */
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 空转换器
     */
    private static class NoopChain<T> extends Chain<T> {
        @Override
        public T exec(T data) {
            return data;
        }
    }
}
