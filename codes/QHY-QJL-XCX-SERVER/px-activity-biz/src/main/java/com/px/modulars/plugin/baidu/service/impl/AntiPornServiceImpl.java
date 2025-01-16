package com.px.modulars.plugin.baidu.service.impl;

import com.px.modulars.plugin.baidu.service.AntiPornService;
import com.px.modulars.plugin.baidu.service.BaiduTokenService;
import com.px.utils.baidu.HttpUtil;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;

@Service
public class AntiPornServiceImpl implements AntiPornService {

    @Autowired
    private BaiduTokenService baiduTokenService;

    @Value("${system.domain}")
    private String domain;

    @Override
    public String imgCensor(String imgUrl) {
        // 请求url
        String url = "https://aip.baidubce.com/rest/2.0/solution/v1/img_censor/v2/user_defined";
        try {
            String param = "imgUrl=" + URLEncoder.encode(imgUrl, "UTF-8");

            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
            String accessToken = baiduTokenService.getAuth();
            String result = HttpUtil.post(url, accessToken, param);

            JSONObject jsonObject = new JSONObject(result);
            System.out.println("jsonObject:"+jsonObject);
            return jsonObject.get("conclusionType") != null ? jsonObject.get("conclusionType").toString() : null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String textCensor(String text) {
        // 请求url
        String url = "https://aip.baidubce.com/rest/2.0/solution/v1/text_censor/v2/user_defined";
        try {
            String param = "text=" + URLEncoder.encode(text, "UTF-8");
            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
            String accessToken = baiduTokenService.getAuth();
            String result = HttpUtil.post(url, accessToken, param);
            JSONObject jsonObject = new JSONObject(result);

            return jsonObject.get("conclusionType") != null ? jsonObject.get("conclusionType").toString() : null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String voiceCensor(String voiceUrl, Integer vid) {


        // 请求url
        String url = "https://aip.baidubce.com/rest/2.0/solution/v1/async_voice/submit";
        try {
            // 本地文件路径
            String param = "url=" + voiceUrl +
                    "&fmt=" + voiceUrl.substring(voiceUrl.lastIndexOf(".")+1) +
                    "&audioId=" + vid +
                    "&rate=16000" +
                    "&callbackUrl=" + domain + "/antiPorn/callBack" +
                    "&rawText=true";

            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
            String accessToken = baiduTokenService.getAuth();
            String result = HttpUtil.post(url, accessToken, param);

            return result;
        } catch (Exception e) {
            e.printStackTrace();

        }
        return null;
    }

    @Override
    public String syncVoiceCensor(String voiceUrl) {

        // 请求url
        String url = "https://aip.baidubce.com/rest/2.0/solution/v1/voice_censor/v3/user_defined";
        try {
            // 本地文件路径
            String param = "url=" + voiceUrl +
                    "&fmt=" + voiceUrl.substring(voiceUrl.lastIndexOf(".")+1) +
                    "&rate=16000" +
                    "&rawText=true";
            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
            String accessToken = baiduTokenService.getAuth();
            String result = HttpUtil.post(url, accessToken, param);
            JSONObject jsonObject = new JSONObject(result);
            return jsonObject.get("conclusionType") != null ? jsonObject.get("conclusionType").toString() : null;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }


}
