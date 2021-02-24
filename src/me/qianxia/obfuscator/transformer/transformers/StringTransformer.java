package me.qianxia.obfuscator.transformer.transformers;

import java.util.Collection;
import java.util.Random;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import me.qianxia.obfuscator.JavaObfuscator;
import me.qianxia.obfuscator.transformer.Transformer;

public class StringTransformer extends Transformer {

	public StringTransformer() {
		super("StringTransformer");
	}

	@Override
	public int run() {
		Collection<ClassNode> classes = JavaObfuscator.INSTANCE.classes.values();
		int num = 0;
		
        ClassNode stringPoolClassNode = new ClassNode();
        stringPoolClassNode.superName = "java/lang/Object";
        stringPoolClassNode.name = randomInt() + "/" + randomInt();
        stringPoolClassNode.access = Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER;
        stringPoolClassNode.version = Opcodes.V1_8;
        
		for(ClassNode classNode : classes) {
			for(MethodNode methodNode : classNode.methods) {
				for(AbstractInsnNode abstractInsnNode : methodNode.instructions) {
					if(!(abstractInsnNode instanceof LdcInsnNode)) {
						continue;
					}
					
					LdcInsnNode ldcInsnNode = (LdcInsnNode) abstractInsnNode;
					Object cst = ldcInsnNode.cst;
					
					if(!(cst instanceof String)) {
						continue;
					}

                    MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, randomInt()  + (num + ""),
                            "()Ljava/lang/String;", null, null);
                    method.visitLdcInsn(cst);
                    method.visitInsn(Opcodes.ARETURN);
                    stringPoolClassNode.methods.add(method);
                    
                    methodNode.instructions.insertBefore(abstractInsnNode, new MethodInsnNode(Opcodes.INVOKESTATIC, stringPoolClassNode.name,
                            method.name, "()Ljava/lang/String;", false));
                    methodNode.instructions.remove(abstractInsnNode);
                    
                    num++;
				}
			}
		}
		
		JavaObfuscator.INSTANCE.classes.put(stringPoolClassNode.name, stringPoolClassNode);
		return 0;
	}
	
	private int randomInt() {
		Random random = new Random();
		return random.nextInt(2333333);
	}
}
