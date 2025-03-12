package com.kiligz.uuid;

import java.security.SecureRandom;

/**
 * 通用唯一标识符
 *
 * @author Ivan
 * @since 2023/3/10
 */
public class UUID {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * uuid的默认长度
     */
    public static final int DEFAULT_LENGTH = 18;

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
        int random = SECURE_RANDOM.nextInt(0xFFFFF) + 0x10000;
        String uuid = Long.toHexString(timestamp * random) + Integer.toHexString(random);
        if (uuid.length() > length) {
            uuid = uuid.substring(0, length);
        } else {
            uuid = String.format("%0" + length + "d", Long.parseLong(uuid, 16));
        }
        return uuid;
    }
}
