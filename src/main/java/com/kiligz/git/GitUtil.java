package com.kiligz.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Git工具类
 *
 * @author Ivan
 */
public class GitUtil {

    private static final String GIT_URL = "${git_url}";
    private static final String PROJECT_NAME = "${project_name}";
    public static final String LOCAL_DIR = System.getProperty("user.dir") + File.separator + PROJECT_NAME;
    private static final String BRANCH = "${branch}";
    private static final String USERNAME = "${username}";
    private static final String PASSWORD = "${password}";

    /**
     * git clone or pull (default)
     */
    public static void download() {
        File dir = new File(LOCAL_DIR);
        try {
            download(GIT_URL, BRANCH, dir, new UsernamePasswordCredentialsProvider(USERNAME, PASSWORD));
        } catch (Exception exception) {
            try {
                Files.deleteIfExists(Paths.get(LOCAL_DIR));
                download(GIT_URL, BRANCH, dir, new UsernamePasswordCredentialsProvider(USERNAME, PASSWORD));
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("famous-data git update error");
            }
        }
      	// download(GIT_URL, BRANCH, dir, new UsernamePasswordCredentialsProvider(USERNAME, PASSWORD));
    }

    /**
     * git clone or pull
     */
    public static void download(String gitUrl, String branch, File localDir, UsernamePasswordCredentialsProvider credentialsProvider) throws GitAPIException, IOException {
        if (localDir.exists())
            gitPull(localDir, branch, credentialsProvider);
        else
            gitClone(gitUrl, branch, localDir, credentialsProvider);
    }

    /**
     * git clone
     */
    private static void gitClone(String gitUrl, String branch, File localDir, UsernamePasswordCredentialsProvider credentialsProvider) throws GitAPIException {
        Git.cloneRepository()
                .setURI(gitUrl)
                .setBranch(branch)
                .setDirectory(localDir)
                .setCredentialsProvider(credentialsProvider)
                .call();
    }

    /**
     * git pull
     */
    private static void gitPull(File localDir, String branch, UsernamePasswordCredentialsProvider credentialsProvider) throws GitAPIException, IOException {
        Git.open(localDir)
                .pull()
                .setRemoteBranchName(branch)
                .setCredentialsProvider(credentialsProvider)
                .call();
    }
}



