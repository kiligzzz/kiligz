package com.kiligz.cos;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.*;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.transfer.MultipleFileDownload;
import com.qcloud.cos.transfer.MultipleFileUpload;
import com.qcloud.cos.transfer.TransferManager;
import com.qcloud.cos.transfer.Upload;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * cos工具类
 *
 * @author Ivan
 * @since 2024/7/10
 */
@Slf4j
@SuppressWarnings("all")
public class COS {
    private static final int BATCH_SIZE = 1000;
    private static final long EXPIRE_30_DAYS = 30L * 24 * 60 * 60 * 1000;

    private final COSClient cosClient;
    private final ExecutorService executorService;
    private final TransferManager transferManager;

    public COS(String ak, String sk, String endPoint, String region) {
        COSCredentials cred = new BasicCOSCredentials(ak, sk);
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        clientConfig.setHttpProtocol(HttpProtocol.http);
        clientConfig.setEndPointSuffix(endPoint);
        clientConfig.setMaxErrorRetry(10);
        this.cosClient = new COSClient(cred, clientConfig);
        this.executorService = Executors.newFixedThreadPool(20);
        this.transferManager = new TransferManager(cosClient, executorService);
    }

    /**
     * 上传文件
     */
    public void upload(String bucket, String key, String filePath) {
        try {
            transferManager.upload(bucket, key, new File(filePath)).waitForCompletion();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 下载文件
     */
    public void download(String bucket, String key, String filePath) {
        try {
            transferManager.download(bucket, key, new File(filePath)).waitForCompletion();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 列出指定前缀下所有key
     */
    public List<String> list(String bucket, String keyPrefix) {
        return listObjectSummary(bucket, keyPrefix)
                .stream()
                .map(COSObjectSummary::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 列出指定前缀下所有ObjectSummary
     */
    public List<COSObjectSummary> listObjectSummary(String bucket, String keyPrefix) {
        try {
            ListObjectsRequest request = new ListObjectsRequest()
                    .withBucketName(bucket)
                    .withPrefix(keyPrefix);
            ObjectListing listing = cosClient.listObjects(request);

            List<COSObjectSummary> osList = new ArrayList<>(listing.getObjectSummaries());
            while (listing.isTruncated()) {
                listing = cosClient.listNextBatchOfObjects(listing);
                osList.addAll(listing.getObjectSummaries());

                log.info("listing... {}", osList.size());
            }
            log.info("{}/{} keys: {}", bucket, keyPrefix, osList.size());
            return osList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将对象写入到cos文件
     * 只能用readObject读取
     */
    public void writeObject(String bucket, String key, Object obj) {
        byte[] bytes = getBytes(obj);
        try (InputStream is = new ByteArrayInputStream(bytes)) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(bytes.length);
            PutObjectRequest request = new PutObjectRequest(bucket, key, is, metadata);
            Upload upload = transferManager.upload(request);
            upload.waitForCompletion();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 读取cos文件成对象
     */
    public <T> T readObject(String bucket, String key) {
        COSObject object = cosClient.getObject(bucket, key);
        try (InputStream is = object.getObjectContent();
             ObjectInputStream ois = new ObjectInputStream(is)) {
            return (T) ois.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将字符串写入到cos文件
     */
    public void writeString(String bucket, String key, String content) {
        byte[] bytes = content.getBytes();
        try (InputStream is = new ByteArrayInputStream(bytes)) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(bytes.length);
            PutObjectRequest request = new PutObjectRequest(bucket, key, is, metadata);
            Upload upload = transferManager.upload(request);
            upload.waitForCompletion();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将字符串列表按行写入cos文件
     */
    public void writeLines(String bucket, String key, List<String> lines) {
        try {
            String[] arr = key.split("/");
            String localFile = "./" + arr[arr.length - 1];
            Path localFilePath = Path.of(localFile);
            Files.write(localFilePath, lines);
            upload(bucket, key, localFile);
            Files.delete(localFilePath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 读取文件成字符串
     */
    public String readString(String bucket, String key) {
        StringJoiner joiner = new StringJoiner("\n");
        readLines(bucket, key, line -> joiner.add(line));
        return joiner.toString();
    }

    /**
     * 按行读取文件成list
     */
    public List<String> readLines(String bucket, String key) {
        List<String> lines = new ArrayList<>();
        readLines(bucket, key, lines::add);
        return lines;
    }

    /**
     * 按行读取文件消费
     */
    public void readLines(String bucket, String key, Consumer<String> consumer) {
        COSObject object = cosClient.getObject(bucket, key);
        try (InputStream is = object.getObjectContent();
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            br.lines().forEach(consumer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 批量上传文件
     */
    public void batchUpload(String bucket, Map<String, String> keyPrefixToFileMap) {
        try {
            List<Upload> uploadList = new ArrayList<>();
            keyPrefixToFileMap.forEach((keyPrefix, filePath) -> {
                File file = new File(filePath);
                PutObjectRequest request = new PutObjectRequest(bucket, keyPrefix + file.getName(), file);
                Upload upload = transferManager.upload(request);
                uploadList.add(upload);
            });
            for (Upload upload : uploadList) {
                upload.waitForCompletion();
            }
        } catch (Exception e) {
            throw new RuntimeException("上传文件失败", e);
        }
    }

    /**
     * 批量上传文件
     */
    public void batchUpload(String bucket, String keyPrefix, String localDir) {
        try {
            MultipleFileUpload multipleFileUpload = transferManager.uploadDirectory(
                    bucket, keyPrefix, new File(localDir), true);
            multipleFileUpload.waitForCompletion();
        } catch (Exception e) {
            throw new RuntimeException("上传文件失败", e);
        }
    }

    /**
     * 批量下载文件
     */
    public void batchDownload(String bucket, String keyPrefix, String localDir) {
        try {
            MultipleFileDownload multipleFileDownload = transferManager.downloadDirectory(
                    bucket, keyPrefix, new File(localDir));
            multipleFileDownload.waitForCompletion();
        } catch (Exception e) {
            throw new RuntimeException("下载文件失败", e);
        }
    }

    /**
     * 批量删除文件
     */
    public void batchDelete(String bucket, String keyPrefix) {
        List<String> list = list(bucket, keyPrefix);

        List<List<String>> batchList = batching(list, BATCH_SIZE);

        List<Future<?>> futureList = new ArrayList<>();
        AtomicInteger deleted = new AtomicInteger();
        for (List<String> batch : batchList) {
            DeleteObjectsRequest request = new DeleteObjectsRequest(bucket)
                    .withKeys(batch.toArray(new String[]{}))
                    .withQuiet(true);
            Future<?> future = executorService.submit(() -> {
                cosClient.deleteObjects(request);
                deleted.getAndAdd(BATCH_SIZE);
                log.info("deleting... {}", deleted.get());
            });
            futureList.add(future);
        }
        for (Future<?> future : futureList) {
            try {
                future.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        log.info("{}/{} deleted: {}", bucket, keyPrefix, deleted.get());
    }

    /**
     * 获取指定key下载链接
     */
    public String getDownloadUrl(String bucket, String key) {
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, key)
                .withExpiration(new Date(System.currentTimeMillis() + EXPIRE_30_DAYS));
        return cosClient.generatePresignedUrl(request).toString();
    }

    /**
     * 关闭cos流
     */
    public void shutdown() {
        transferManager.shutdownNow();
        cosClient.shutdown();
    }

    /**
     * 按指定大小分批
     */
    private static <T> List<List<T>> batching(Collection<T> c, int batchSize) {
        List<T> list = new ArrayList<>(c);
        return Stream.iterate(0, i -> i + batchSize)
                .limit((list.size() + batchSize - 1) / batchSize)
                .map(i -> list.subList(i, Math.min(i + batchSize, list.size())))
                .collect(Collectors.toList());
    }

    /**
     * 对象转数组
     */
    private static byte[] getBytes(Object obj) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
            oos.flush();
            return bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}