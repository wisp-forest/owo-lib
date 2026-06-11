package io.wispforest.owo.neoforge.env;

import net.neoforged.fml.loading.FMLEnvironment;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class EnvironmentStripperTransformer {
    private static final String ANNOTATION_DESC = "Lio/wispforest/owo/neoforge/env/Environment;";
    private static final boolean IS_CLIENT = FMLEnvironment.getDist().name().equals("CLIENT");

    public static void stripUnwantedEnvironments(ClassNode classNode) {
        if (classNode.visibleAnnotations != null) {
            for (var anim : classNode.visibleAnnotations) {
                if (ANNOTATION_DESC.equals(anim.desc) && shouldStrip(anim)) {
                    for (var method : classNode.methods) {
                        replaceMethodBodyWithError(method);
                    }
                    classNode.fields.clear();
                    return;
                }
            }
        }

        for (var method : classNode.methods) {
            if (method.visibleAnnotations != null) {
                for (var anim : method.visibleAnnotations) {
                    if (ANNOTATION_DESC.equals(anim.desc) && shouldStrip(anim)) {
                        // TODO: IS THEIR ANY ISSUES WITH USING STUB METHODS?
                        replaceMethodBodyWithError(method);

                        break;
                    }
                }
            }
        }

        var fieldIterator = classNode.fields.iterator();
        while (fieldIterator.hasNext()) {
            FieldNode field = fieldIterator.next();
            if (field.visibleAnnotations != null) {
                for (var anim : field.visibleAnnotations) {
                    if (ANNOTATION_DESC.equals(anim.desc) && shouldStrip(anim)) {
                        fieldIterator.remove();
                        break;
                    }
                }
            }
        }
    }

    private static boolean shouldStrip(AnnotationNode annotation) {
        if (annotation.values != null) {
            for (int i = 0; i < annotation.values.size(); i += 2) {
                var name = (String) annotation.values.get(i);
                if ("value".equals(name)) {
                    var enumDetails = (String[]) annotation.values.get(i + 1);
                    var targetEnvironment = enumDetails[1];

                    return IS_CLIENT == targetEnvironment.equals("CLIENT");
                }
            }
        }
        return false;
    }

    private static void replaceMethodBodyWithError(MethodNode method) {
        method.instructions.clear();
        var errorInstructions = new InsnList();
        errorInstructions.add(new TypeInsnNode(Opcodes.NEW, "java/lang/RuntimeException"));
        errorInstructions.add(new InsnNode(Opcodes.DUP));
        errorInstructions.add(new LdcInsnNode("Attempted to invoke an environment-stripped method!"));
        errorInstructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;)V", false));
        errorInstructions.add(new InsnNode(Opcodes.ATHROW));
        method.instructions.add(errorInstructions);
    }
}
