package me.qianxia.obfuscator.utils;

/**
 * @author: QianXia
 * @create: 2021-02-25 15:21
 **/
public class TimerUtils {
    private long time = 0;

    public void start() {
        this.time = System.currentTimeMillis();
    }

    public void stop() {
        int mills = (int) (System.currentTimeMillis() - time);
        int seconds = mills / 1000;
        int minutes = seconds / 60;

        LogUtils.log("耗费 " + (seconds == 0 ? mills + " 毫秒！"
                : ((minutes == 0) ? seconds + " 秒！" : minutes + " 分 " + (seconds - (minutes * 60)) + " 秒！")));
        LogUtils.log("\n");
        time = 0;
    }

    public void stop(boolean state) {
        int mills = (int) (System.currentTimeMillis() - time);
        int seconds = mills / 1000;
        int minutes = seconds / 60;

        LogUtils.log("耗费 " + (seconds == 0 ? mills + " 毫秒！"
                : ((minutes == 0) ? seconds + " 秒！" : minutes + " 分 " + (seconds - (minutes * 60)) + " 秒！")));
        LogUtils.log("\n");
        time = 0;
    }
}
