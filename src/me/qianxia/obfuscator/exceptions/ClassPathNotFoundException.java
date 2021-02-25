package me.qianxia.obfuscator.exceptions;

public class ClassPathNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 5541071156532794101L;
    private String classPathName;

    public ClassPathNotFoundException(String classPathName) {
        this.classPathName = classPathName;
    }

    public String getClassPathName() {
        return this.classPathName;
    }
}
