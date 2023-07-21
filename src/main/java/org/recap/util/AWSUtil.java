package org.recap.util;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import lombok.extern.slf4j.Slf4j;
import org.recap.PropertyKeyConstants;
import org.recap.ScsbCommonConstants;
import org.recap.ScsbConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * @author Dinakar N created on 21/07/23
 */
@Service
@Slf4j
public class AWSUtil {

    @Value("${" + PropertyKeyConstants.SCSB_BUCKET_NAME + "}")
    private String s3BucketName;

    @Autowired
    AmazonS3 awsS3Client;

    public void copyFromAWSToLocal() {
        S3Object fetchFile = awsS3Client.getObject(new GetObjectRequest(s3BucketName, "text.xml"));
        final BufferedInputStream i = new BufferedInputStream(fetchFile.getObjectContent());
        InputStream objectData = fetchFile.getObjectContent();
        try {
            Files.copy(objectData, new File("/data/matching-services/source-data/text.xml").toPath()); //location to local path
            objectData.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
