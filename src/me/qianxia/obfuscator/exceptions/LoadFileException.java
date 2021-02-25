package me.qianxia.obfuscator.exceptions;

/**
 * @author: Qian_Xia
 * @create: 2021-02-25 15:02
 **/
public class LoadFileException extends RuntimeException {
    private static final long serialVersionUID = -1045508419801241199L;

    public LoadFileException(String str) {
        super(str);
    }
}
