package com.atguigu.gulimall.thirdparty.controller;

//import com.aliyun.oss.OSS;
//import com.aliyun.oss.common.utils.BinaryUtil;
//import com.aliyun.oss.model.MatchMode;
//import com.aliyun.oss.model.PolicyConditions;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.atguigu.common.utils.R;
import feign.Param;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.websocket.server.PathParam;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@RestController
public class OssController {

//    @Value("cloud.aws.credentials.access-key")
//    private String key;
//    @Value("cloud.aws.credentials.secret-key")
//    private String secretKey;
//
//    @Value("cloud.aws.region.static")
//    private String region;
//
//    @Value("application.bucket.name")
//    private String bucketName;


    @Value("${cloud.aws.credentials.access-key}")
    private String key;
    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Value("${application.bucket.name}")
    private String bucketName;


    private AmazonS3 s3client;

    @Autowired
    public OssController(AmazonS3 ps3)
    {
        s3client = ps3;
    }


    @RequestMapping("/s3/policy/{filename}")
    public R getPresignedUrl(@PathVariable("filename") String fileName){

        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000 * 200;

        expiration.setTime(expTimeMillis);

        log.info("gen pre signed url ");

        fileName = UUID.randomUUID().toString() + fileName ;
        GeneratePresignedUrlRequest genRequestUrl = new GeneratePresignedUrlRequest(
                bucketName,fileName, HttpMethod.POST);

        URL url = s3client.generatePresignedUrl(genRequestUrl);
        log.info("utl string:" + url.toString());



        String now = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String dir = now + "/";
        Map<String, String> respMap = new HashMap<>();
        Map<String, String> queryMap = new HashMap<>();
        Arrays.asList(url.getQuery().split("&")).stream().map((item)->{
            String[] params = url.getQuery().split("=");
            if (params.length>1 )
            {
                queryMap.put(params[0], params[1]) ;
            }
            else if (params.length  == 1 )
            {
                queryMap.put(params[0], "") ;
            }

            return "";
        });


        log.info("getPath "+ url.getPath());
        log.info("getHost " + url.getHost());
        log.info("getQuery " +url.getQuery());
        log.info("getFile " +url.getFile());
        log.info("getAuthority " +url.getAuthority());
        log.info("getRef " +url.getRef());
        log.info("getUserInfo " +url.getUserInfo());
        //log.info("getContent " + url.getContent().toString());
//        log.info("getUserInfo " +url.());

       respMap.put("accessid", key);
       respMap.put("policy", url.toString());
//        respMap.put("signature", postSignature);
        respMap.put("filename", fileName);
        respMap.put("host", url.getHost());
        respMap.put("query", url.getQuery());
        respMap.put("expire", String.valueOf(expTimeMillis / 1000));

        return R.ok().put("data", respMap).put("query",queryMap);
    }

//    @Autowired
//    OSS ossClient;
//
//    @Value("${spring.cloud.alicloud.oss.endpoint}")
//    private String endpoint;
//    @Value("${spring.cloud.alicloud.oss.bucket}")
//    private String bucket;
//
//    @Value("${spring.cloud.alicloud.access-key}")
//    private String accessId;
//
//
//    @RequestMapping("/oss/policy")
//    public R policy() {
//
//
//
//        //https://gulimall-hello.oss-cn-beijing.aliyuncs.com/hahaha.jpg
//
//        String host = "https://" + bucket + "." + endpoint; // host的格式为 bucketname.endpoint
//        // callbackUrl为 上传回调服务器的URL，请将下面的IP和Port配置为您自己的真实信息。
////        String callbackUrl = "http://88.88.88.88:8888";
//        String format = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
//        String dir = format + "/"; // 用户上传文件时指定的前缀。
//
//        Map<String, String> respMap = null;
//        try {
//            long expireTime = 30;
//            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
//            Date expiration = new Date(expireEndTime);
//            PolicyConditions policyConds = new PolicyConditions();
//            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
//            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);
//
//            String postPolicy = ossClient.generatePostPolicy(expiration, policyConds);
//            byte[] binaryData = postPolicy.getBytes("utf-8");
//            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
//            String postSignature = ossClient.calculatePostSignature(postPolicy);
//
//            respMap = new LinkedHashMap<String, String>();
//            respMap.put("accessid", accessId);
//            respMap.put("policy", encodedPolicy);
//            respMap.put("signature", postSignature);
//            respMap.put("dir", dir);
//            respMap.put("host", host);
//            respMap.put("expire", String.valueOf(expireEndTime / 1000));
//            // respMap.put("expire", formatISO8601Date(expiration));
//
//
//        } catch (Exception e) {
//            // Assert.fail(e.getMessage());
//            System.out.println(e.getMessage());
//        }
//
//        return R.ok().put("data",respMap);
//    }
}
