package me.qianxia.obfuscator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import me.qianxia.obfuscator.asm.MyClassWriter;
import me.qianxia.obfuscator.config.Config;
import me.qianxia.obfuscator.exceptions.ClassPathNotFoundException;
import me.qianxia.obfuscator.exceptions.LoadFileException;
import me.qianxia.obfuscator.transformer.Transformer;
import me.qianxia.obfuscator.transformer.transformers.InvokeMethodTransformer;
import me.qianxia.obfuscator.transformer.transformers.StringTransformer;
import me.qianxia.obfuscator.utils.LogUtils;
import me.qianxia.obfuscator.utils.TimerUtils;

/**
 * @author: QianXia
 * @create: 2021-02-25 15:57
 **/
public class JavaObfuscator {
    private static final String AUTHOR = "QianXia";
    public static final String OBFUSCATORS = "1-2";
    public static final String DESCRIBES = "字符串-调用";
    public static final double VERSION = 1.0;
    public static final String NAME = "JavaObfuscator";
    private final String COMMENT = "https://github.com/L0serQianXia/JavaObfuscator";

    private final List<Transformer> TRANSFORMERS = new ArrayList<>();

    public Map<String, ClassNode> classes = new HashMap<>();
    public Map<String, ClassNode> excludedClasses = new HashMap<>();
    private Map<String, byte[]> resources = new HashMap<>();
    public static Map<String, ClassReader> classpath = new HashMap<>();
    private Map<String, byte[]> invaildClasses = new HashMap<>();

    private String inputName = "input.jar";
    private String outputName = "output.jar";
    private String myObfuscators = "12";
    private String classpathPath = "";
    private String excluded = "";

    private static boolean outputSetted = false;

    private TimerUtils timer = new TimerUtils();

    /**
     * 混淆器功能在这添加
     */
    private void addTransformers() {
        TRANSFORMERS.add(new StringTransformer());
        TRANSFORMERS.add(new InvokeMethodTransformer());
    }

    /**
     * 开始运行混淆器
     *
     * @param args 用Object以适配两种运行方式
     */
    public void run(Object args) {
        System.gc();
        this.startTimer();
        this.addTransformers();
        this.loadConfig(args);
        this.loadClasspath();
        this.loadInput();
        this.runObfuscators();
        this.writeFile();
        this.stopTimer();
        System.gc();
    }

    /**
     * 设置输入文件的路径
     *
     * @param path 文件的路径
     */
    public void setInput(String path) {
        this.inputName = path;
    }

    /**
     * 设置输出文件的路径
     *
     * @param path 路径
     */
    public void setOutput(String path) {
        this.outputName = path;
    }

    /**
     * 设置支持库的路径
     *
     * @param path 路径
     */
    public void setClasspath(String path) {
        this.classpathPath = path;
    }

    /**
     * 导出文件
     */
    private void writeFile() {
        LogUtils.log(0, "开始导出文件");
        File outputFile = new File(outputName);
        if (outputFile.exists()) {
            outputFile.renameTo(new File(outputName + System.currentTimeMillis()));
            LogUtils.log("文件已经存在 将老文件保存为%s", outputName + System.currentTimeMillis());
            outputFile = new File(outputName);
        }
        try {
            ZipOutputStream zOut = new ZipOutputStream(new FileOutputStream(outputFile));
            classes.values().forEach(classNode -> {
                try {
                    ClassWriter cw = new MyClassWriter(ClassWriter.COMPUTE_FRAMES);
                    try {
                        classNode.accept(cw);
                    } catch (ClassPathNotFoundException e) {
                        LogUtils.error("%s缺少支持库:%s  将会使用COMPUTE_MAX", classNode.name, e.getClassPathName());
                        cw = new MyClassWriter(ClassWriter.COMPUTE_MAXS);
                        classNode.accept(cw);
                    } catch (NegativeArraySizeException | ArrayIndexOutOfBoundsException e) {
                        LogUtils.error("%s计算帧失败？！", classNode.name);
                        cw = new MyClassWriter(ClassWriter.COMPUTE_MAXS);
                        classNode.accept(cw);
                    }
                    byte[] b = cw.toByteArray();
                    ZipEntry entry = new ZipEntry(classNode.name + (classNode.name.endsWith(".class") ? "" : ".class"));
                    zOut.putNextEntry(entry);
                    zOut.write(b);
                    zOut.closeEntry();
                } catch (Exception e) {
                    e.printStackTrace();
                    LogUtils.error(e.getMessage());
                }

            });

            invaildClasses.forEach((name, b) -> {
                ZipEntry entry = new ZipEntry(name + (name.endsWith(".class") ? "" : ".class"));

                try {
                    zOut.putNextEntry(entry);
                    zOut.write(b);
                    zOut.closeEntry();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            resources.forEach((name, b) -> {
                ZipEntry entry = new ZipEntry(name);

                try {
                    zOut.putNextEntry(entry);
                    if (entry.getName().contains("MANIFEST.MF")) {
                        String someString = new String(b).substring(0, new String(b).length() - 1)
                                + "Obfuscator-By: QianXia(https://github.com/L0serQianXia/JavaObfuscator)\r\t";
                        b = someString.getBytes("UTF-8");
                    }
                    zOut.write(b);
                    zOut.closeEntry();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            zOut.setComment(COMMENT);
            zOut.flush();
            zOut.close();
            LogUtils.log(1, "导出完毕 导出为%s", outputFile);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.error("导出%s出错 错误信息：%s", outputFile.getName(), e.getMessage());
        }
    }

    /**
     * 开始计时
     */
    private void startTimer() {
        timer.start();
    }

    /**
     * 停止计时
     */
    private void stopTimer() {
        LogUtils.log("混淆完毕");
        timer.stop(false);
    }

    /**
     * 开始跑混淆器
     */
    private void runObfuscators() {
        String[] obfuscators = myObfuscators.split("|");
        String[] describes = DESCRIBES.split("-");
        Transformer.init(this, classes);
        for (String indexStr : obfuscators) {
            int index = Integer.parseInt(indexStr) - 1;
            LogUtils.log(0, "开始进行%s混淆", describes[index]);
            Transformer tran = TRANSFORMERS.get(index);
            int num = tran.run();
            LogUtils.log(1, "%s混淆完毕 执行了%s次", describes[index], num);
        }
    }

    /**
     * 加载支持库
     */
    private void loadClasspath() {
        if (classpathPath.isEmpty()) {
            return;
        }
        LogUtils.log(0, "开始加载支持库");
        File fileDir = new File(classpathPath);
        Stream<File> files = Arrays.stream(fileDir.listFiles());
        files.filter(this::isJarFile).forEach(file -> {
            LogUtils.log("开始加载%s......", file.getName());
            try {
                ZipFile zip = new ZipFile(file);
                Enumeration<? extends ZipEntry> zipEntries = zip.entries();
                while (zipEntries.hasMoreElements()) {
                    ZipEntry entry = zipEntries.nextElement();
                    if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                        InputStream is = zip.getInputStream(entry);
                        ClassReader cr = new ClassReader(is);
                        classpath.put(entry.getName().replace(".class", ""), cr);
                    }
                }
                zip.close();
            } catch (IOException e) {
                LogUtils.error("%s:%s", file.getAbsolutePath(), e.getCause());
            }
        });
        LogUtils.log(1, "支持库加载完毕");
    }

    /**
     * 判断是否为Jar文件
     *
     * @param file 文件
     * @return 返回真就是 假就不是
     */
    private boolean isJarFile(File file) {
        String fileName = file.getName();
        return fileName.endsWith(".jar");
    }

    /**
     * 加载需要被混淆的文件
     */
    private void loadInput() throws LoadFileException {
        LogUtils.log(0, "开始加载%s", inputName);
        try {
            ZipFile zip = new ZipFile(inputName, Charset.forName("UTF-8"));
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = null;
                try {
                    entry = entries.nextElement();
                } catch (IllegalArgumentException e) {
                    zip.close();
                    zip = new ZipFile(inputName, Charset.forName("GBK"));
                    LogUtils.log("%s 编码不兼容 已自动切换为GBK编码", inputName);
                }
                InputStream in = zip.getInputStream(entry);
                if (!entry.isDirectory()) {
                    if (entry.getName().endsWith(".class")) {
                        try {
                            ClassReader cr = new ClassReader(in);
                            ClassNode cn = new ClassNode();
                            cr.accept(cn, ClassReader.SKIP_FRAMES);
                            classes.put(cn.name, cn);
                            classpath.put(cr.getClassName(), cr);

                            /** 开始加载排除项 */
                            if (!excluded.isEmpty()) {
                                String[] excludes = excluded.split("\r\n");
                                for (String exclude : excludes) {
                                    if (entry.getName().startsWith(exclude)) {
                                        excludedClasses.put(entry.getName(), cn);
                                    }
                                }
                            }
                            if (excludedClasses.containsKey(entry.getName())) {
                                continue;
                            }
                            /** 判断排除项完成 */

                        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
                            LogUtils.error("加载%s失败,错误信息:%s", entry.getName(), e.getMessage());
                            invaildClasses.put(entry.getName().replace(".class", ""), toByteArray(in));
                        }
                    } else {
                        resources.put(entry.getName(), toByteArray(in));
                    }
                } else {
                    if (excluded.contains(entry.getName())) {
                        resources.put(entry.getName(), toByteArray(in));
                    }
                }
            }
            zip.close();
        } catch (FileNotFoundException e) {
            throw new LoadFileException("未找到文件");
        } catch (IOException e) {
            throw new LoadFileException(e.getMessage());
        }
        LogUtils.log(1, "加载%s完成", inputName);
    }

    /**
     * 加载配置
     *
     * @param obj 配置原文
     */
    private void loadConfig(Object obj) {
        if (obj instanceof String[]) {
            String[] args = (String[]) obj;
            for (int i = 0; i < args.length; i++) {
                if ("--input".equalsIgnoreCase(args[i])) {
                    inputName = args[i + 1];
                } else if ("--output".equalsIgnoreCase(args[i])) {
                    outputName = args[i + 1];
                    outputSetted = true;
                } else if ("--using".equalsIgnoreCase(args[i])) {
                    myObfuscators = args[i + 1];
                } else if ("--classpath".equalsIgnoreCase(args[i])) {
                    classpathPath = args[i + 1].trim();
                } else if ("--help".equalsIgnoreCase(args[i])) {
                    this.showHelpMenu();
                    System.exit(0);
                } else if ("--exclude".equalsIgnoreCase(args[i])) {
                    int num = (i + 1);
                    try {
                        while (!args[num].contains("--") && !args[num].isEmpty()) {
                            if (num + 1 > args.length) {
                                break;
                            }
                            excluded += args[num] + "\r\n";
                            num++;
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {

                    }
                }
            }

            // 设置输出文件名
            if (!outputSetted) {
                outputName = inputName.replace(".jar", ".obf.jar");
            }
        } else {
            Config conf = (Config) obj;
            inputName = conf.getInputPath();
            outputName = conf.getOutputPath().isEmpty() ? inputName.replace(".jar", ".obf.jar") : conf.getOutputPath();
            myObfuscators = conf.getUsing();
            classpathPath = conf.getClassPath();
            for (String exclude : conf.getExcluded()) {
                excluded += exclude + "\r\n";
            }
        }
        LogUtils.log("加载配置完成");
    }

    /**
     * 显示帮助菜单
     */
    public void showHelpMenu() {
        System.out.println("--------------------------------------");
        System.out.println("--input 需要被混淆的文件");
        System.out.println("--output 输出的文件");
        System.out.println("--using 需要使用的混淆功能");
        this.printObfuscators();
        System.out.println("--classpath 支持库的目录 （必须选定一个目录 目录下需要放支持库）");
        System.out.println("--exclude 排除文件的目录 （必须选定一个目录 排除该目录下所有文件）");
        System.out.println("--------------------------------------");
        System.out.println();
        System.out.println();
    }

    /**
     * 显示更新的信息
     */
    public void showUpdateMessage() {
        System.out.println("--------------------------------------");
        System.out.println(NAME + " V" + VERSION + " by " + AUTHOR);
        System.out.println("--------------------------------------");
        System.out.println();
    }

    /**
     * 打印所有的混淆功能
     */
    private void printObfuscators() {
        String[] obfuscators = OBFUSCATORS.split("-");
        String[] descs = DESCRIBES.split("-");
        String obfuscatorsWithoutShit = "";
        String describesWithoutShit = "";
        final String SPACE = "        ";
        for (int i = 0; i < obfuscators.length; i++) {
            System.out.print(String.format("%s%s-%s混淆\n", SPACE, obfuscators[i], descs[i]));
            obfuscatorsWithoutShit += obfuscators[i];
            describesWithoutShit += descs[i] + ((i + 1) == obfuscators.length ? "混淆" : "混淆 ");
        }
        System.out.println(String.format("%s举例：--using %s(包含%s)", SPACE, obfuscatorsWithoutShit, describesWithoutShit));
    }

    /**
     * 转为字节集
     *
     * @param in 文件输入流
     * @return 返回字节集
     */
    private byte[] toByteArray(InputStream in) {
        try {
            ByteArrayOutputStream baros = new ByteArrayOutputStream();
            final byte[] BUFFER = new byte[1024];
            while (in.available() > 0) {
                int data = in.read(BUFFER);
                baros.write(BUFFER, 0, data);
            }
            return baros.toByteArray();
        } catch (IOException e) {
            LogUtils.error("读入出现错误");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 清空已经加载的类
     */
    public void clearClasses() {
        classes.clear();
    }
}
