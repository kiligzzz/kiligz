package com.kiligz.uuid;

/**
 * 通用唯一标识符
 *
 * @author Ivan
 * @since 2023/3/10
 */
public class UUID {
    /**
     * uuid的默认长度
     */
    public static final int DEFAULT_LENGTH = 12;

    /**
     * 默认长度唯一id
     */
    public static String uuid() {
        return uuid(DEFAULT_LENGTH);
    }

    /**
     * 指定长度唯一id
     */
    public static String uuid(int length) {
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 100000);
        String uuid = Long.toString(timestamp * random, 16) + Integer.toString(random, 16);
        if (uuid.length() > length) {
            uuid = uuid.substring(0, length);
        } else {
            uuid = String.format("%0" + length + "d", Long.parseLong(uuid, 16));
        }
        return uuid;
    }
}
