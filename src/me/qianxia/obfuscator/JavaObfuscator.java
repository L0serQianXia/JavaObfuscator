package me.qianxia.obfuscator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import me.qianxia.obfuscator.transformer.Transformer;
import me.qianxia.obfuscator.transformer.Transformers;

public class JavaObfuscator {
	public Map<String, ClassNode> classes = new HashMap<>();
	public Map<String, byte[]> junkFiles = new HashMap<>();
	private String outputName = "out.jar";
	private final String COMMENT = "https://github.com/L0serQianXia/JavaObfuscator";
	
	public static JavaObfuscator INSTANCE;
	
	public JavaObfuscator() {
		INSTANCE = this;
	}
	
	public void run(String[] arg) {
		try {
			loadInput("in.jar");
			runTransformer();
			writeFile();
		} catch (ZipException e) {
			e.printStackTrace();
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
                    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
                    try {
                        classNode.accept(cw);
                    } catch (NegativeArraySizeException | ArrayIndexOutOfBoundsException e) {
                        cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
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
