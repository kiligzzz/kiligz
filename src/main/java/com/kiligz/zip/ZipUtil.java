package com.kiligz.zip;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * zip工具类
 *
 * @author Ivan
 * @since 2024/11/6
 */
public class ZipUtil {

    /**
     * 压缩
     */
    public static void zip(String sourcePath, String zipPath) {
        Path sp = Path.of(sourcePath);
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath))) {
            Files.walkFileTree(sp, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String entryName = sp.relativize(file).toString();
                    zos.putNextEntry(new ZipEntry(entryName));
                    Files.copy(file, zos);
                    zos.closeEntry();
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (!dir.equals(sp)) { // Avoid adding the root directory itself
                        String entryName = sp.relativize(dir).toString();
                        zos.putNextEntry(new ZipEntry(entryName + "/"));
                        zos.closeEntry();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * 压缩多个文件
     */
    public static void zipFiles(List<String> filePathList, String zipPath) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath))) {
            for (String filePath : filePathList) {
                try (InputStream is = new FileInputStream(filePath)) {
                    ZipEntry zipEntry = new ZipEntry(new File(filePath).getName());
                    zos.putNextEntry(zipEntry);
                    copy(is, zos);
                    zos.closeEntry();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * 解压
     */
    public static void unzip(String zipPath, String destPath) {
        Path zp = Path.of(zipPath);
        Path dp = Path.of(destPath);
        try (Stream<Path> zpStream = Files.list(zp)){
            Files.createDirectories(dp);
            zpStream.forEach((zipEntryPath) -> {
                try {
                    ZipEntry zipEntry = new ZipEntry(zipEntryPath.getFileName().toString().replaceFirst(zp.getFileName().toString() + "$", ""));
                    Path outputPath = dp.resolve(zipEntry.toString());
                    if (zipEntry.isDirectory()) {
                        Files.createDirectories(outputPath);
                    } else {
                        Files.createDirectories(outputPath.getParent());
                        try (InputStream in = Files.newInputStream(zipEntryPath);
                             OutputStream out = Files.newOutputStream(outputPath)) {
                            copy(in, out);
                        }
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void copy(InputStream is, OutputStream os) {
        try {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) != -1) {
                os.write(buffer, 0, length);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}