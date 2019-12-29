package com.irain.swing;

import javax.swing.*;
import java.awt.*;

/**
 * @version: V1.0
 * @author: 王勇琪
 * @date: 2019/12/19 14:34
 * 检查设备状态异常时弹出对话框
 **/

public class ErrorDialog {
    static ImageIcon tipImage = new ImageIcon("H:\\image\\tip\\birthTip.png");
    public static JLabel label = new JLabel(tipImage, SwingConstants.CENTER);
    public static JFrame frame = null;

    public static final int WIDTH = 400;// 提示框的宽度
    public static final int HEIGHT = 205;// 高度

    //采用单实例模式[懒汉式]保证只有一个窗体存在【由于是单线程程序暂时未考虑多线程】
    private static ErrorDialog instance = null;

    public static ErrorDialog getInstance() {
        if (instance == null) {
            instance = new ErrorDialog();
        }
        return instance;
    }

    private ErrorDialog() {
        frame = new JFrame();
        frame.setLayout(new FlowLayout());
        Font font = new Font("黑体", Font.PLAIN, 13);
        label.setFont(font);
        frame.add(label);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = (int) screenSize.getWidth();
        int screenHeight = (int) screenSize.getHeight();
        frame.setBounds(screenWidth - WIDTH, screenHeight - HEIGHT, WIDTH, HEIGHT);
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setVisible(true);
    }
}