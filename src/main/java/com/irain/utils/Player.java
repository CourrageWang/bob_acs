package com.irain.utils;

import com.baidu.aip.speech.AipSpeech;
import com.baidu.aip.speech.TtsResponse;
import com.baidu.aip.util.Util;
import com.irain.conf.LoadConf;
import lombok.extern.log4j.Log4j;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @version: V1.0
 * @author: 王勇琪
 * @date: 2019/11/24 15:55
 * 连接异常时播放声音
 **/
@Log4j
public class Player {
    //声明一个全局的player对象
    public static javazoom.jl.player.Player player = null;

    /**
     * 播放格式为.MP3的音频文件；
     */
    public static void playMP3Music(String sourcePath) {
        try {
            //声明一个File对象
            File mp3 = new File(sourcePath);
            //创建一个输入流
            FileInputStream fileInputStream = new FileInputStream(mp3);
            //创建一个缓冲流
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            //创建播放器对象，把文件的缓冲流传入进去
            player = new javazoom.jl.player.Player(bufferedInputStream);
            player.play();
            Thread.sleep(4000);//休眠时长为播放连接异常信息时长
            player.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * 调用百度API获取语音合成
     *
     * @param Ip
     * @param port
     */
    public static void getVoice(String Ip, String port) {

        new LoadConf();
        // 初始化一个AipSpeech
        AipSpeech client = new AipSpeech(LoadConf.propertiesMap.get("APP_ID"), LoadConf.propertiesMap.get("API_KEY"),
                LoadConf.propertiesMap.get("SECRET_KEY"));

        //设置网络连接参数
        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);

        String content = String.format("Ip地址为%s端口为%s的设备连接异常，请重新连接", Ip, port);

        // 调用接口
        TtsResponse res = client.synthesis(content, "zh", 1, null);
        byte[] data = res.getData();
        JSONObject res1 = res.getResult();
        if (data != null) {
            try {
                Util.writeBytesToFileSystem(data, "/Users/yqwang/Workspace/java/home_work_v2/src/main/resources/output.mp3");
            } catch (IOException e) {
                log.error("Failed to synthesize speech" + e.getMessage());
            }
        }
        if (res1 != null) {
            log.info(res1.toString(2));
        }
    }
}