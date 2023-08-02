package org.recap.util;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.recap.PropertyKeyConstants;
import org.recap.ScsbConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * @author Dinakar N created on 21/07/23
 */
@Service
@Slf4j
public class AWSUtil {

    @Autowired
    AmazonS3 awsS3Client;

    String seperator = File.separator;

    @Value("${" + PropertyKeyConstants.SCSB_BUCKET_NAME + "}")
    private String s3BucketName;

    public void copyFromAWSToLocal() {
        //List<String> s3FileList = deleteDuplicateFiles();
        ProcessBuilder processBuilder = new ProcessBuilder();
        List<String> commandsList = new ArrayList<>();
        //commandsList.add("aws s3 cp s3://scsb-test/HL-HD-Data-InitialAccession/ s3://scsb-test/matching-services --recursive --exclude \"*/*\"");
        commandsList.add("aws s3 cp s3://scsb-test/matching-services/ /data/matching-services/source-data --recursive --exclude \"*/*\"");
        commandsList.add("aws s3 cp s3://scsb-test/matching-services/ s3://scsb-test/matching-services/.done --recursive --exclude \"*/*\"");
        commandsList.add("aws s3 rm s3://scsb-test/matching-services/ --recursive --exclude \"*/*\"");

        try {
            for (int i = 0; i < commandsList.size(); i++) {
                processBuilder.command("/bin/bash", "-c", commandsList.get(i));
                Process process = processBuilder.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                     log.info(line);
                }
                int exitCode = process.waitFor();
                log.info("completed with error code : " + exitCode);
            }
        } catch (Exception e) {
            log.info("exception occurred : {}",e.fillInStackTrace());

        }
    }

    private List<String> deleteDuplicateFiles() {
        List<File> fileList = new ArrayList<>();
        List<String> localFileList = new ArrayList<>();
        List<String> s3FileList = new ArrayList<>();
        File[] files = new File(ScsbConstants.DATA + ScsbConstants.MATCHING_SERVICES + seperator + ScsbConstants.SOURCE_DATA).listFiles();
        fileList = Arrays.asList(files);
        localFileList = fileList.stream().map(a -> a.getName()).toList();
        ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(s3BucketName).withPrefix(ScsbConstants.MATCHING_SERVICES);
        ListObjectsV2Result listing = awsS3Client.listObjectsV2(req);
        for (S3ObjectSummary summary : listing.getObjectSummaries()) {
            if ((summary.getKey().endsWith(".xml") || summary.getKey().endsWith(".gz")) && !summary.getKey().contains(".done")) {
                s3FileList.add(summary.getKey().replace(ScsbConstants.MATCHING_SERVICES + seperator, ""));
            }
        }
        for (String fileName : localFileList) {
            if (s3FileList.contains(fileName)) {
                try {
                    Files.deleteIfExists(
                            Paths.get(ScsbConstants.DATA + ScsbConstants.MATCHING_SERVICES + seperator + ScsbConstants.SOURCE_DATA + seperator + fileName));
                } catch (NoSuchFileException e) {
                    log.info(ScsbConstants.EXCEPTION_MESSAGE, e.getMessage());
                } catch (DirectoryNotEmptyException e) {
                    log.info(ScsbConstants.EXCEPTION_MESSAGE, e.getMessage());
                } catch (Exception e) {
                    log.info(ScsbConstants.EXCEPTION_MESSAGE, e.getMessage());
                }
            }
        }
        return s3FileList;
    }
}
