package com.irain.utils;

import lombok.extern.log4j.Log4j;

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
}