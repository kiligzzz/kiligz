package com.kiligz.net;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * net工具类
 * 
 * @author Ivan
 * @since 2023/4/14
 */
public class NetUtil {
    /**
     * 默认的ip
     */
    private static final String DEFAULT_IP = "127.0.0.1";

    /**
     * 获取本机ip
     */
    public static String ip() {
        try {
            InetAddress res = null;
            int lowest = Integer.MAX_VALUE;

            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
            while (nis.hasMoreElements()) {
                NetworkInterface ni = nis.nextElement();
                if (ni.isUp()) {
                    if (ni.getIndex() >= lowest && res != null)
                        continue;
                    lowest = ni.getIndex();

                    Enumeration<InetAddress> ias = ni.getInetAddresses();
                    while (ias.hasMoreElements()) {
                        InetAddress ia = ias.nextElement();
                        if (ia instanceof Inet4Address && !ia.isLoopbackAddress()) {
                            res = ia;
                        }
                    }
                }
            }

            if (res == null)
                res = InetAddress.getLocalHost();

            return res.getHostAddress();
        } catch (Exception ignore) {
        }
        return DEFAULT_IP;
    }
}
