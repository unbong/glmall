package com.atguigu.gulimall.thirdparty;

//import com.aliyun.oss.OSSClient;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;

@SpringBootTest
class GulimallThirdPartyApplicationTests {

    @Test
    void contextLoads() {


    }



    @Test
    public void testUpload() throws FileNotFoundException {

        // s3
        AWSCredentials credentials = new BasicAWSCredentials("AKIAZZMOWM7S2H24EZZ6",
                "bi0Kgj7mvHVY8BWxquzUa878tUK6CyaLJJVLJvna");



         final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withCredentials(
                 new AWSStaticCredentialsProvider(credentials)).withRegion("ap-northeast-1").build();


        try {
            // S3のオブジェクトを取得する
            S3Object o = s3.getObject("gulimall-image", "0d40c24b264aa511.jpg");
            S3ObjectInputStream s3is = o.getObjectContent();

            // ダウンロード先のファイルパスを指定する
            FileOutputStream fos = new FileOutputStream(new File("D:\\workspace\\s3demo.jpg"));

            // S3のオブジェクトを1024byteずつ読み込み、ダウンロード先のファイルに書き込んでいく
            byte[] read_buf = new byte[1024];
            int read_len = 0;
            while ((read_len = s3is.read(read_buf)) > 0) {
                fos.write(read_buf, 0, read_len);
            }

            s3is.close();
            fos.close();
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        System.out.println("Done!");
    }
}
