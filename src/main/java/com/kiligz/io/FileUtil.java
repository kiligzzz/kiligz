package com.kiligz.io;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * 文件工具类
 * <pre>
 * 1.支持级联获取、删除指定目录的所有文件、文件夹
 * 2.支持获取文件名相关
 * 3.支持获取文件md5值
 * </pre>
 *
 * @author Ivan
 * @since 2023/5/20
 */
public class FileUtil {
    /**
     * 列出指定路径下的所有文件
     */
    public static List<File> listFiles(String... dirs) {
        List<File> fileList = new ArrayList<>();
        for (String dir : dirs) {
            list(new File(dir), fileList, false);
        }
        return fileList;
    }

    /**
     * 列出指定路径下的所有文件，包括目录
     */
    public static List<File> list(String... dirs) {
        List<File> fileList = new ArrayList<>();
        for (String dir : dirs) {
            list(new File(dir), fileList, true);
        }
        return fileList;
    }

    /**
     * 删除指定路径下的所有文件
     */
    public static boolean delete(String... dirs) {
        for (String dirStr : dirs) {
            File dir = new File(dirStr);
            File[] files = dir.listFiles();
            if (dir.isDirectory() && files != null) {
                for (File file : files) {
                    if (!delete(file.getAbsolutePath())) {
                        return false;
                    }
                }
            }
            if (!dir.delete()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取文件所在目录
     */
    public static String getPrefixPath(String file) {
        return file.substring(0, file.lastIndexOf(File.separator) + 1);
    }

    /**
     * 获取文件名
     */
    public static String getName(String file) {
        String name = getNameWithSuffix(file);
        return name.substring(name.indexOf("."));
    }

    /**
     * 获取文件名带后缀
     */
    public static String getNameWithSuffix(String file) {
        String[] arr = file.split(File.separator);
        return arr[arr.length - 1];
    }

    /**
     * 计算文件md5值
     */
    public static String md5(String path) {
        byte[] md5Hash;
        String md5Str;
        try (FileInputStream fis = new FileInputStream(path)) {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = fis.read(buffer)) > 0) {
                md.update(buffer, 0, read);
            }
            md5Hash = md.digest();
            md5Str = Base64.getEncoder().encodeToString(md5Hash);
        } catch (Exception e) {
            return "";
        }
        return md5Str;
    }

    /**
     * 列出指定目录下的文件，支持选择是否列出文件夹
     */
    private static void list(File dir, List<File> fileList, boolean isListDir) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        if (isListDir) {
            fileList.add(dir);
        }
        for (File file : files) {
            if (file.isFile()) {
                fileList.add(file);
            } else {
                list(file, fileList, isListDir);
            }
        }
    }
}
