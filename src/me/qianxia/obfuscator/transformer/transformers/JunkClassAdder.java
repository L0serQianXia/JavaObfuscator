package me.qianxia.obfuscator.transformer.transformers;

import java.util.Random;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import me.qianxia.obfuscator.transformer.Transformer;
import me.qianxia.obfuscator.utils.AbuseUtils;

/**
 * @author QianXia
 * @create: 2021-02-26 18:01
 */
public class JunkClassAdder extends Transformer{
    private Random random = new Random();
    
    public JunkClassAdder() {
        super("JunkClassAdder");
    }

    @Override
    public int run() {
        int num = 0;
        
        ClassNode junkClass = new ClassNode();
        junkClass.superName = "java/lang/Object";
        junkClass.access = Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER;
        junkClass.version = Opcodes.V1_8;
        
        for (int i = 0; i < classes.values().size(); i++) {
            if (random.nextInt(5) > 2) {
                junkClass = new ClassNode();
                junkClass.superName = "java/lang/Object";
                junkClass.access = Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER;
                junkClass.version = Opcodes.V1_8;
                junkClass.name = AbuseUtils.getAbuse() + (random.nextInt(3) < 1 ? AbuseUtils.getAbuse() : "/" + AbuseUtils.getAbuse());
                
                int randomNum = random.nextInt(3);
                for(int j = 0; j < randomNum; j++) {
                    junkClass.name = junkClass.name + "/" + AbuseUtils.getAbuse();
                }
                
                if(randomNum < 3) {
                    junkClass.name = AbuseUtils.getAbuse();
                }
                
                for (int i1 = 0; i1 < (random.nextInt(10) + 1); i1++) {
                    MethodNode methodNode = new MethodNode(Opcodes.ACC_PUBLIC, AbuseUtils.getAbuse(), "()Ljava/lang/String;", null, null);
                    methodNode.visitLdcInsn("Obfuscator By QianXia(https://github.com/L0serQianXia/JavaObfuscator)");
                    methodNode.visitInsn(Opcodes.ARETURN);
                    junkClass.methods.add(methodNode);
                }
                
                getObfuscator().classes.put(junkClass.name, junkClass);
                num++;
            }
        }
        return num;
    }

}
