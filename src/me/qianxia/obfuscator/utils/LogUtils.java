package me.qianxia.obfuscator.utils;

import me.qianxia.obfuscator.JavaObfuscator;

import javax.swing.*;

/**
 * @description: 日志
 * @author: Qian_Xia
 * @create: 2020-08-06 17:25
 **/
public class LogUtils {
    private static TimerUtils timer = new TimerUtils();
    public static boolean isGUI = false;
    public static JTextArea GUIText = null;

    /**
     * 记录日志&记录时间
     *
     * @param state     状态 0为开始记录时间 1为结束记录时间
     * @param text      内容 %s
     * @param something %s所需
     */
    public static void log(int state, String text, Object... something) {
        String formated = String.format("[" + JavaObfuscator.NAME + "]" + text, something);
        if ("\n".equals(text)) {
            formated = "";
        }
        if (isGUI) {
            GUIText.append(formated + "\n");
            System.out.println(formated);
        } else {
            System.out.println(formated);
        }
        if (state == 0) {
            timer.start();
        } else {
            timer.stop();
        }
    }

    /**
     * 记录日志
     *
     * @param text      内容 %s
     * @param something %s所需
     */
    public static void log(String text, Object... something) {
        String formated = String.format("[" + JavaObfuscator.NAME + "]" + text, something);
        if ("\n".equals(text)) {
            formated = "";
        }
        if (isGUI) {
            GUIText.append(formated + "\n");
            System.out.println(formated);
        } else {
            System.out.println(formated);
        }
    }

    /**
     * 记录警告的日志
     *
     * @param text      内容 %s
     * @param something %s所需
     */
    public static void warn(String text, Object... something) {
        String formated = String.format(text, something);
        if ("\n".equals(text)) {
            formated = "";
        }
        if (isGUI) {
            GUIText.append("[--WARN--]" + formated + "\n");
            System.out.println("[--WARN--]" + formated);
        } else {
            System.out.println("[--WARN--]" + formated);
        }
    }

    /**
     * 记录错误的日志
     *
     * @param text      内容 %s
     * @param something %s所需
     */
    public static void error(String text, Object... something) {
        String formated = String.format(text, something);
        if ("\n".equals(text)) {
            formated = "";
        }
        if (isGUI) {
            GUIText.append("[--ERROR--]" + formated + "\n");
            System.out.println("[--ERROR--]" + formated);
        } else {
            System.out.println("[--ERROR--]" + formated);
        }
    }

    /**
     * 记录错误的日志
     *
     * @param text      内容 %s
     * @param something %s所需
     */
    public static void fatal(String text, Object... something) {
        String formated = String.format(text, something);
        if (isGUI) {
            GUIText.append("[--FATAL--]" + formated);
            System.out.println("[--FATAL--]" + formated);
        } else {
            System.out.println("[--FATAL--]" + formated);
        }
    }
}
