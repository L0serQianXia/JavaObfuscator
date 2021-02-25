package me.qianxia.obfuscator.transformer.transformers;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import me.qianxia.obfuscator.transformer.Transformer;

public class InvokeMethodTransformer extends Transformer {
	private ClassNode methodCallsClassNode = new ClassNode();
	private Map<String, ClassNode> newClass = new HashMap<>();
	
	public InvokeMethodTransformer() {
		super("InvokeMethodTranformer");
	}

	@SuppressWarnings("deprecation")
	@Override
	public int run() {
		int num = 0;
		int temp = 0;
		
		String methodCallsName = new Random().nextInt(23333333) + "";
		methodCallsClassNode = new ClassNode();
		methodCallsClassNode.superName = "java/lang/Object";
		methodCallsClassNode.name = methodCallsName;
		methodCallsClassNode.access = Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER;
		methodCallsClassNode.version = Opcodes.V1_8;
		
		for(ClassNode classNode : classes.values()) {
			for(MethodNode methodNode : classNode.methods) {
				for(AbstractInsnNode abstractInsnNode : methodNode.instructions) {
					if(abstractInsnNode.getOpcode() != Opcodes.INVOKESTATIC) {
						continue;
					}

					if (temp > 1000) {
						newClass.put(methodCallsClassNode.name, methodCallsClassNode);
						methodCallsName = new Random().nextInt(2333333) + "";
						methodCallsClassNode = new ClassNode();
						methodCallsClassNode.superName = "java/lang/Object";
						methodCallsClassNode.name = methodCallsName;
						methodCallsClassNode.access = Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER;
						methodCallsClassNode.version = Opcodes.V1_8;
						temp = 0;
					}
					
					String randomName = new Random().nextInt(23333333) + "";
					
					MethodInsnNode methodInsnNode = (MethodInsnNode) abstractInsnNode;
					MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, randomName, methodInsnNode.desc, null, null);
					
					MethodNode testMethod = getMethodNode(classNode, methodInsnNode.name, methodInsnNode.desc);
					if(testMethod != null) {
						if(testMethod.access == 10 || testMethod.access == 2) {
							continue;
						}
					}
					
					Type[] types = Type.getArgumentTypes(methodInsnNode.desc);
					
					for (int i = 0; i < types.length; i++) {
						method.visitVarInsn(getVarOpcode(types[i], false), i);
					}

					method.visitMethodInsn(Opcodes.INVOKESTATIC, methodInsnNode.owner, methodInsnNode.name, methodInsnNode.desc);
					
					Type returnType = Type.getReturnType(methodInsnNode.desc);
					method.visitInsn(getReturnOpcode(returnType));
					
					methodCallsClassNode.methods.add(method);
					
					methodNode.instructions.insertBefore(abstractInsnNode, 
							new MethodInsnNode(Opcodes.INVOKESTATIC,
							methodCallsClassNode.name, randomName, 
							methodInsnNode.desc, false));
					methodNode.instructions.remove(abstractInsnNode);
					
					num++;
					temp++;
				}
			}
		}
		
		if (num == 0) {
			return 0;
		}
		
		getObfuscator().classes.put(methodCallsClassNode.name, methodCallsClassNode);
		newClass.forEach((name, clazz) -> {
			getObfuscator().classes.put(name, clazz);
		});
		
		return num;
	}
	
	private MethodNode getMethodNode(ClassNode classNode, String name, String desc) {
		for(MethodNode methodNode : classNode.methods) {
			if(methodNode.name.equals(name) && methodNode.desc.equals(desc)) {
				return methodNode;
			}
		}
		return null;
	}
	
	/**
	 * https://github.com/ItzSomebody/radon
	 */
    public int getReturnOpcode(Type type) {
        switch (type.getSort()) {
            case Type.BOOLEAN:
            case Type.CHAR:
            case Type.BYTE:
            case Type.SHORT:
            case Type.INT:
                return Opcodes.IRETURN;
            case Type.FLOAT:
                return Opcodes.FRETURN;
            case Type.LONG:
                return Opcodes.LRETURN;
            case Type.DOUBLE:
                return Opcodes.DRETURN;
            case Type.ARRAY:
            case Type.OBJECT:
                return Opcodes.ARETURN;
            case Type.VOID:
                return Opcodes.RETURN;
            default:
                throw new AssertionError("Unknown type sort: " + type.getClassName());
        }
    }
    
	/**
	 * https://github.com/ItzSomebody/radon
	 */
    public int getVarOpcode(Type type, boolean store) {
        switch (type.getSort()) {
            case Type.BOOLEAN:
            case Type.CHAR:
            case Type.BYTE:
            case Type.SHORT:
            case Type.INT:
                return store ? Opcodes.ISTORE : Opcodes.ILOAD;
            case Type.FLOAT:
                return store ? Opcodes.FSTORE : Opcodes.FLOAD;
            case Type.LONG:
                return store ? Opcodes.LSTORE : Opcodes.LLOAD;
            case Type.DOUBLE:
                return store ? Opcodes.DSTORE : Opcodes.DLOAD;
            case Type.ARRAY:
            case Type.OBJECT:
                return store ? Opcodes.ASTORE : Opcodes.ALOAD;
            default:
                throw new AssertionError("Unknown type: " + type.getClassName());
        }
    }
}
