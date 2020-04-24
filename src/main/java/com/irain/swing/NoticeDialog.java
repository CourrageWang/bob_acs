package com.irain.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class NoticeDialog extends JFrame {
    public static boolean show = true;
    //采用单实例模式[懒汉式]保证只有一个窗体存在【由于是单线程程序暂时未考虑多线程】
    private static NoticeDialog instance = null;
    private JLabel label;

    public NoticeDialog setText(String text) {
        this.label.setText(text);
        return this;
    }

    public static NoticeDialog getInstance() {
        if (instance == null) {
            instance = new NoticeDialog();
        }
        return instance;
    }

    private NoticeDialog() {
        final int WIDTH = 400;// 提示框的宽度
        final int HEIGHT = 205;// 高度
        ImageIcon tipImage = new ImageIcon("H:\\image\\tip\\birthTip.png");

        JPanel jp = new JPanel();    //创建一个JPanel对象
        label = new JLabel();
        Font font = new Font("黑体", Font.PLAIN, 13);

        label = new JLabel(tipImage, SwingConstants.CENTER);
        label.setFont(font);

        jp.add(label);
        this.setTitle("第一个窗体");
        this.add(jp);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = (int) screenSize.getWidth();
        int screenHeight = (int) screenSize.getHeight();
        this.setBounds(screenWidth - WIDTH, screenHeight - HEIGHT, WIDTH, HEIGHT);
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.setVisible(true);

        // 添加窗口事件
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int selected = JOptionPane.showConfirmDialog(null, "确定退出", "关闭弹窗？", JOptionPane.YES_NO_OPTION);
                if (JOptionPane.YES_NO_OPTION == selected) {

                    NoticeDialog.show = false;
                    instance.setVisible(false);
                }
            }
        });
    }
}