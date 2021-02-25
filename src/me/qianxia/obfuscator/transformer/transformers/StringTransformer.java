package me.qianxia.obfuscator.transformer.transformers;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import me.qianxia.obfuscator.JavaObfuscator;
import me.qianxia.obfuscator.transformer.Transformer;
import me.qianxia.obfuscator.utils.LogUtils;

public class StringTransformer extends Transformer {
	private String decryptMethodName = new Random().nextInt(23333333) + "";
	private ClassNode stringPoolClassNode = new ClassNode();
	private Map<String, ClassNode> newClass = new HashMap<>();

	public StringTransformer() {
		super("StringTransformer");
	}

	@Override
	public int run() {
		int num = 0;
		int temp = 0;
		String stringPoolName = new Random().nextInt(23333333) + "";
		stringPoolClassNode = new ClassNode();
		stringPoolClassNode.superName = "java/lang/Object";
		stringPoolClassNode.name = stringPoolName;
		stringPoolClassNode.access = Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER;
		stringPoolClassNode.version = Opcodes.V1_8;

		String randomName = new Random().nextInt(23333333) + "";
		for (ClassNode cn : classes.values()) {
			for (MethodNode mn : cn.methods) {
				for (AbstractInsnNode insn : mn.instructions.toArray()) {
					if (insn instanceof LdcInsnNode) {
						if (((LdcInsnNode) insn).cst instanceof String) {
							
							if (temp > 1000) {
								this.addDecryptMethod(stringPoolClassNode);
								newClass.put(stringPoolClassNode.name, stringPoolClassNode);
								stringPoolName = new Random().nextInt(2333333) + "";
								stringPoolClassNode = new ClassNode();
								stringPoolClassNode.superName = "java/lang/Object";
								stringPoolClassNode.name = stringPoolName;
								stringPoolClassNode.access = Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER;
								stringPoolClassNode.version = Opcodes.V1_8;
								temp = 0;
							}
							
							String ldc = (String) ((LdcInsnNode) insn).cst;

							if (ldc.toCharArray().length >= 21845 || ldc.toCharArray().length * 2 >= 21845) {
								LogUtils.log("%s 文本字符超长,自动跳过", (cn.name + "/" + mn.name));
								continue;
							}

							MethodNode methodNode = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
									randomName + num, "()Ljava/lang/String;", null, null);
							String crypt;
							try {
								crypt = URLEncoder.encode(transformer(ldc), "gbk");
							} catch (Exception e) {
								LogUtils.error("混淆文本加密错误%s", e.getMessage());
								continue;
							}
							
							methodNode.visitLdcInsn(crypt);
							methodNode.visitLdcInsn("gbk");
							methodNode.visitMethodInsn(Opcodes.INVOKESTATIC, "java/net/URLDecoder", "decode",
									"(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", false);
							methodNode.visitMethodInsn(Opcodes.INVOKESTATIC, stringPoolName, decryptMethodName,
									"(Ljava/lang/String;)Ljava/lang/String;", false);
							methodNode.visitInsn(Opcodes.ARETURN);
							stringPoolClassNode.methods.add(methodNode);
							mn.instructions.insertBefore(insn, new MethodInsnNode(Opcodes.INVOKESTATIC,
									stringPoolClassNode.name, randomName + num, "()Ljava/lang/String;", false));
							mn.instructions.remove(insn);
							
							num++;
							temp++;
						}
					}
				}
			}
		}
		this.addDecryptMethod(stringPoolClassNode);

		if (num == 0) {
			return 0;
		}
		
		getObfuscator().classes.put(stringPoolClassNode.name, stringPoolClassNode);
		newClass.forEach((name, clazz) -> {
			getObfuscator().classes.put(name, clazz);
		});
		
		return num;
	}

	public void addDecryptMethod(ClassNode strPool) {
		MethodNode de = new MethodNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, decryptMethodName,
				"(Ljava/lang/String;)Ljava/lang/String;", null, null);
		Label l3 = new Label();
		Label l4 = new Label();
		de.visitLabel(new Label());
		de.visitVarInsn(Opcodes.ALOAD, 0);
		de.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "getBytes", "()[B", false);
		de.visitVarInsn(Opcodes.ASTORE, 1);
		de.visitLabel(new Label());
		de.visitInsn(Opcodes.ICONST_0);
		de.visitVarInsn(Opcodes.ISTORE, 2);
		de.visitLabel(new Label());
		de.visitJumpInsn(Opcodes.GOTO, l3);
		de.visitLabel(l4);
		de.visitVarInsn(Opcodes.ALOAD, 1);
		de.visitVarInsn(Opcodes.ILOAD, 2);
		de.visitVarInsn(Opcodes.ALOAD, 1);
		de.visitVarInsn(Opcodes.ILOAD, 2);
		de.visitInsn(Opcodes.BALOAD);
		de.visitIntInsn(Opcodes.BIPUSH, 5);
		de.visitInsn(Opcodes.IXOR);
		de.visitInsn(Opcodes.I2B);
		de.visitInsn(Opcodes.BASTORE);
		de.visitLabel(new Label());
		de.visitIincInsn(2, 1);
		de.visitLabel(l3);
		de.visitVarInsn(Opcodes.ILOAD, 2);
		de.visitVarInsn(Opcodes.ALOAD, 1);
		de.visitInsn(Opcodes.ARRAYLENGTH);
		de.visitJumpInsn(Opcodes.IF_ICMPLT, l4);
		de.visitLabel(new Label());
		de.visitTypeInsn(Opcodes.NEW, "java/lang/String");
		de.visitInsn(Opcodes.DUP);
		de.visitVarInsn(Opcodes.ALOAD, 1);
		de.visitInsn(Opcodes.ICONST_0);
		de.visitVarInsn(Opcodes.ALOAD, 1);
		de.visitInsn(Opcodes.ARRAYLENGTH);
		de.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/String", "<init>", "([BII)V", false);
		de.visitVarInsn(Opcodes.ASTORE, 1);
		de.visitLabel(new Label());
		de.visitVarInsn(Opcodes.ALOAD, 1);
		de.visitInsn(Opcodes.ARETURN);
		de.visitLabel(new Label());
		strPool.methods.add(de);
	}

	public static String transformer(String str) {
		byte[] var1 = str.getBytes();

		for (int i = 0; i < var1.length; i++) {
			var1[i] = (byte) (var1[i] ^ 5);
		}

		return new String(var1, 0, var1.length);
	}
}
