package me.qianxia.obfuscator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import me.qianxia.obfuscator.asm.MyClassWriter;
import me.qianxia.obfuscator.exceptions.ClassPathNotFoundException;
import me.qianxia.obfuscator.transformer.Transformer;
import me.qianxia.obfuscator.transformer.Transformers;

public class JavaObfuscator {
	public Map<String, ClassNode> classes = new HashMap<>();
	public Map<String, byte[]> junkFiles = new HashMap<>();
    public Map<String, ClassReader> classpath = new HashMap<>();
    
    private String inputName = "in.jar";
	private String outputName = "";
    private String classpathPath = "";
    
    private static final String NAME = "JavaObfuscator";
    private static final double VERSION = 1.0;
    private static final String AUTHOR = "千夏";
	private final String COMMENT = "https://github.com/L0serQianXia/JavaObfuscator";
	
	public static JavaObfuscator INSTANCE;

	public JavaObfuscator() {
		INSTANCE = this;
	}
	
	public void run(String[] args) {
		try {
	        loadConfig(args);
	        loadClasspath();
			loadInput(inputName);
			runTransformer();
			writeFile();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
    public void writeFile() {
        File outputFile = new File(outputName);
        if (outputFile.exists()) {
            outputFile.renameTo(new File(outputName + System.currentTimeMillis()));
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
                        cw = new MyClassWriter(ClassWriter.COMPUTE_MAXS);
                        classNode.accept(cw);
					} catch (NegativeArraySizeException | ArrayIndexOutOfBoundsException e) {
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
                }

            });
            
            junkFiles.forEach((name, b) -> {
                ZipEntry entry = new ZipEntry(name);

                try {
                    zOut.putNextEntry(entry);
                    if (entry.getName().contains("MANIFEST.MF")) {
                        String someString = new String(b).substring(0, new String(b).length() - 1) + "Obfuscator By QianXia(https://github.com/L0serQianXia/JavaObfuscator)";
                        b = someString.getBytes("UTF-8");
                    }
                    zOut.write(b);
                    zOut.closeEntry();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            zOut.setComment(COMMENT );
            zOut.flush();
            zOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	public void runTransformer() {
		for(Object transformer : Transformers.transformers) {
			Transformer transformer2 = (Transformer) transformer;
			int times = transformer2.run();
			System.out.println(transformer2.getName() +" did " + times + " times");
		}
	}
	
	public void loadInput(String inputFilePath) throws ZipException, IOException {
		File inputFile = new File(inputFilePath);
		ZipFile zipFile = new ZipFile(inputFile);
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		
		while(entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			String name = entry.getName();
			InputStream inputStream = zipFile.getInputStream(entry);
			
			if(name.endsWith(".class")) {
				ClassReader classReader = new ClassReader(inputStream);
				ClassNode classNode = new ClassNode();
				classReader.accept(classNode, ClassReader.SKIP_FRAMES);
				classes.put(classNode.name, classNode);
			} else {
				junkFiles.put(name, toByteArray(inputStream));
			}
		}
		
		zipFile.close();
	}

    private void loadClasspath() {
        if (classpathPath.isEmpty()) {
            return;
        }
        File fileDir = new File(classpathPath);
        Stream<File> files = Arrays.stream(fileDir.listFiles());
        files.filter(this::isJarFile).forEach(file -> {
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
                e.printStackTrace();
            }
        });
    }
    
    private boolean isJarFile(File file) {
        String fileName = file.getName();
        return fileName.endsWith(".jar");
    }
    
    private void loadConfig(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if ("--input".equalsIgnoreCase(args[i])) {
                inputName = args[i + 1];
            } else if ("--output".equalsIgnoreCase(args[i])) {
                outputName = args[i + 1];
            } else if ("--classpath".equalsIgnoreCase(args[i])) {
                classpathPath = args[i + 1].trim();
            } else if ("--help".equalsIgnoreCase(args[i])) {
                this.showHelpMenu();
                System.exit(0);
            } 
        }

        if (outputName.isEmpty()) {
            outputName = inputName.replace(".jar", ".obf.jar");
        }
    }
    
    public void showHelpMenu() {
        System.out.println("--------------------------------------");
        System.out.println("--input 需要被混淆的文件");
        System.out.println("--output 输出的文件");
        System.out.println("--classpath 支持库的目录 （必须选定一个目录 目录下需要放支持库）");
        System.out.println("--------------------------------------");
        System.out.println();
        System.out.println();
    }
    
    public void showVersionMessage() {
        System.out.println("--------------------------------------");
        System.out.println(String.format("%s V%s  by %s", NAME, VERSION, AUTHOR));
        System.out.println("--------------------------------------");
        System.out.println();
    }
    
	private byte[] toByteArray(InputStream inputStream) throws IOException {
		ByteArrayOutputStream bOutputStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024 * 4];
		
		int temp = 0;
		while((temp = inputStream.read(buffer)) != -1) {
			bOutputStream.write(buffer, 0, temp);
		}
		bOutputStream.close();
		
		return bOutputStream.toByteArray();
	}
}
