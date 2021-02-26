package me.qianxia.obfuscator.transformer;

import java.util.Map;

import org.objectweb.asm.tree.ClassNode;

import me.qianxia.obfuscator.JavaObfuscator;

public abstract class Transformer {
    private String name;
    protected static JavaObfuscator obfuscator;
    protected static Map<String, ClassNode> classes;

    public static void init(JavaObfuscator obf, Map<String, ClassNode> classess) {
        obfuscator = obf;
        classes = classess;
    }

    public Transformer(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public JavaObfuscator getObfuscator() {
        return obfuscator;
    }

    public abstract int run();
}
