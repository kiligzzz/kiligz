package com.kiligz.io;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件工具类
 * <pre>
 * 1.支持级联获取、删除指定目录的所有文件、文件夹
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
