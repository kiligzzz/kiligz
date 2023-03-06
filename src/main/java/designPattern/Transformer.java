package designPattern;

/**
 * 转换器抽象类
 *
 * 使用：
 * Transformer transformer = Transformer.chain(
 *         new NoopTransformer(),
 *         new LowercaseTransformer());
 * transformer.join(
 *         new DigitalTransformer(),
 *         new SpecialCharTransformer());
 *
 * @author Ivan
 */
public abstract class Transformer {
    /**
     * 下个转换器
     */
    private Transformer next;

    /**
     * 构造链式Transformer
     */
    public static Transformer chain(Transformer... transformerArr) {
        if (transformerArr.length == 0)
            return new NoopTransformer();

        Transformer head = transformerArr[0];
        Transformer cur = head;
        for (int i = 1; i < transformerArr.length; i++) {
            cur = cur.next = transformerArr[i];
        }
        return head;
    }

    /**
     * 往当前链最后拼接Transformer
     */
    public void join(Transformer... transformerArr) {
        Transformer cur = this;
        while (cur.next != null) {
            cur = cur.next;
        }
        for (Transformer transformer : transformerArr) {
            cur = cur.next = transformer;
        }
    }

    /**
     * 转换
     */
    public String trans(String str) {
        if (str == null)
            return null;

        str = transform(str);
        return next == null ? str : next.trans(str);
    }

    /**
     * 抽象转换方法
     */
    protected abstract String transform(String str);

    /**
     * 空转换器
     */
    public static class NoopTransformer extends Transformer {
        @Override
        protected String transform(String str) {
            return str;
        }
    }
}