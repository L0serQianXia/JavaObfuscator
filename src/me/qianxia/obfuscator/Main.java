package me.qianxia.obfuscator;

import me.qianxia.obfuscator.exceptions.LoadFileException;
import me.qianxia.obfuscator.utils.LogUtils;

public class Main {
    public static void main(String[] args) {
	new JavaObfuscator().showUpdateMessage();

	if (args.length == 0) {
	    new JavaObfuscator().showHelpMenu();
	    GUI frame = new GUI();
	    frame.setVisible(true);
	    return;
	}

	Main.runObfuscator(args);
    }

    public static void runObfuscator(Object str) {
	try {
	    new JavaObfuscator().run(str);
	} catch (Exception e) {
	    if (e instanceof LoadFileException) {
		LogUtils.error("********加载文件时出现异常********");
		LogUtils.error("具体的异常信息：");
		LogUtils.error(e.getMessage());
		e.printStackTrace();
	    }
	}
    }

}
