package me.qianxia.obfuscator.config;

/**
 * @description: 配置
 * @author: QianXia
 * @create: 2021-02-25 15:47
 **/
public class Config {
    private String inputPath;
    private String outputPath;
    private String classPath;
    private String using;
    private String[] excluded;

    public Config(String inputPath, String outputPath, String classPath, String using, String... exclude) {
        this.inputPath = inputPath;
        this.outputPath = outputPath;
        this.classPath = classPath;
        this.using = using;
        this.excluded = exclude;
    }

    public Config() {
        super();
    }

    public String getInputPath() {
        return this.inputPath;
    }

    public String getOutputPath() {
        return this.outputPath;
    }

    public String getClassPath() {
        return this.classPath;
    }

    public String getUsing() {
        return this.using;
    }

    public String[] getExcluded() {
        return this.excluded;
    }

    public void setInputPath(String inputPath) {
        this.inputPath = inputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }

    public void setUsing(String using) {
        this.using = using;
    }
}
