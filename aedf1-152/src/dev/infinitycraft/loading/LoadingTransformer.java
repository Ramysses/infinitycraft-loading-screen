package dev.infinitycraft.loading;

import cpw.mods.fml.relauncher.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public final class LoadingTransformer implements IClassTransformer, Opcodes {
    private static final String SPLASH_OWNER = "dev/infinitycraft/loading/SplashRenderer";
    private static final String TRANSFORMER_OWNER = "dev/infinitycraft/loading/LoadingTransformer";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) {
            return null;
        }
        try {
            if ("net.minecraft.client.Minecraft".equals(transformedName)) {
                return transformMinecraft(basicClass);
            }
            if ("cpw.mods.fml.common.LoadController".equals(transformedName)
                    || "cpw.mods.fml.common.LoadController".equals(name)) {
                return transformLoadController(basicClass);
            }
            if ("net.minecraft.client.renderer.texture.TextureManager".equals(transformedName)
                    || "biq".equals(name)) {
                return transformTextureManager(basicClass);
            }
            if ("br.com.infinitycrafts.hubui.LobbyStartup".equals(transformedName)
                    || "br.com.infinitycrafts.hubui.LobbyStartup".equals(name)) {
                return transformLobbyStartup(basicClass);
            }
        } catch (Throwable t) {
            System.err.println("[InfinityCraft] LoadingTransformer skipped " + transformedName + ": " + t.getClass().getSimpleName());
        }
        return basicClass;
    }

    public static boolean isLobbyOrDefaultTarget(String expected, Object actual) {
        if (actual == null) {
            return true;
        }
        String value = actual.toString().trim();
        return value.isEmpty() || "lobby".equalsIgnoreCase(value) || (expected != null && expected.equalsIgnoreCase(value));
    }

    private byte[] transformMinecraft(byte[] basicClass) {
        ClassNode classNode = new ClassNode();
        new ClassReader(basicClass).accept(classNode, 0);
        int splashPatched = 0;

        for (Object obj : classNode.methods) {
            MethodNode method = (MethodNode) obj;
            if (!"()V".equals(method.desc)) {
                continue;
            }
            boolean hasMojangSplash = false;
            boolean hasFmlLifecycle = false;
            AbstractInsnNode[] nodes = method.instructions.toArray();
            for (int i = 0; i < nodes.length; i++) {
                AbstractInsnNode insn = nodes[i];
                if (insn instanceof LdcInsnNode && "/title/mojang.png".equals(((LdcInsnNode) insn).cst)) {
                    hasMojangSplash = true;
                } else if (insn instanceof MethodInsnNode) {
                    MethodInsnNode call = (MethodInsnNode) insn;
                    if ("cpw/mods/fml/client/FMLClientHandler".equals(call.owner)
                            && ("beginMinecraftLoading".equals(call.name)
                            || "finishMinecraftLoading".equals(call.name)
                            || "onInitializationComplete".equals(call.name))) {
                        hasFmlLifecycle = true;
                    }
                }
            }

            if (hasMojangSplash) {
                LabelNode fallback = new LabelNode();
                InsnList list = new InsnList();
                list.add(new LdcInsnNode("loadScreen"));
                list.add(new MethodInsnNode(INVOKESTATIC, SPLASH_OWNER, "step", "(Ljava/lang/String;)V"));
                list.add(new MethodInsnNode(INVOKESTATIC, SPLASH_OWNER, "draw", "()Z"));
                list.add(new JumpInsnNode(IFEQ, fallback));
                list.add(new InsnNode(RETURN));
                list.add(fallback);
                method.instructions.insert(list);
                splashPatched++;
            }

            if (hasFmlLifecycle) {
                instrumentStartGame(method);
            }
        }

        if (splashPatched != 1) {
            throw new IllegalStateException("Expected exactly one Minecraft 1.5.2 splash method, found " + splashPatched);
        }

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(cw);
        return cw.toByteArray();
    }

    private void instrumentStartGame(MethodNode method) {
        AbstractInsnNode[] nodes = method.instructions.toArray();
        for (int i = 0; i < nodes.length; i++) {
            AbstractInsnNode insn = nodes[i];
            if (!(insn instanceof MethodInsnNode)) {
                continue;
            }
            MethodInsnNode call = (MethodInsnNode) insn;
            if ("cpw/mods/fml/client/FMLClientHandler".equals(call.owner)) {
                if ("beginMinecraftLoading".equals(call.name)) {
                    InsnList before = new InsnList();
                    before.add(new LdcInsnNode("beginMinecraftLoading"));
                    before.add(new MethodInsnNode(INVOKESTATIC, SPLASH_OWNER, "step", "(Ljava/lang/String;)V"));
                    method.instructions.insertBefore(call, before);
                } else if ("finishMinecraftLoading".equals(call.name)) {
                    InsnList before = new InsnList();
                    before.add(new LdcInsnNode("finishMinecraftLoading"));
                    before.add(new MethodInsnNode(INVOKESTATIC, SPLASH_OWNER, "step", "(Ljava/lang/String;)V"));
                    method.instructions.insertBefore(call, before);
                } else if ("onInitializationComplete".equals(call.name)) {
                    InsnList before = new InsnList();
                    before.add(new LdcInsnNode("onInitializationComplete"));
                    before.add(new MethodInsnNode(INVOKESTATIC, SPLASH_OWNER, "step", "(Ljava/lang/String;)V"));
                    method.instructions.insertBefore(call, before);

                    InsnList after = new InsnList();
                    after.add(new MethodInsnNode(INVOKESTATIC, SPLASH_OWNER, "finish", "()V"));
                    method.instructions.insert(call, after);
                }
            }
        }
    }

    private byte[] transformLoadController(byte[] basicClass) {
        ClassNode classNode = new ClassNode();
        new ClassReader(basicClass).accept(classNode, 0);
        boolean modified = false;

        for (Object obj : classNode.methods) {
            MethodNode method = (MethodNode) obj;
            if ("sendEventToModContainer".equals(method.name)
                    || "propogateStateMessage".equals(method.name)
                    || "transition".equals(method.name)) {
                InsnList hook = new InsnList();
                hook.add(new MethodInsnNode(INVOKESTATIC, SPLASH_OWNER, "tickProgress", "()V"));
                method.instructions.insert(hook);
                modified = true;
            }
        }

        if (!modified) {
            return basicClass;
        }
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(cw);
        return cw.toByteArray();
    }

    private byte[] transformTextureManager(byte[] basicClass) {
        ClassNode classNode = new ClassNode();
        new ClassReader(basicClass).accept(classNode, 0);
        boolean modified = false;

        for (Object obj : classNode.methods) {
            MethodNode method = (MethodNode) obj;
            if ("createNewTexture".equals(method.name)) {
                InsnList hook = new InsnList();
                hook.add(new MethodInsnNode(INVOKESTATIC, SPLASH_OWNER, "tickProgress", "()V"));
                method.instructions.insert(hook);
                modified = true;
            }
        }

        if (!modified) {
            return basicClass;
        }
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(cw);
        return cw.toByteArray();
    }

    private byte[] transformLobbyStartup(byte[] basicClass) {
        ClassNode classNode = new ClassNode();
        new ClassReader(basicClass).accept(classNode, 0);
        boolean modified = false;

        for (Object obj : classNode.methods) {
            MethodNode method = (MethodNode) obj;
            if (!"connectLegacy152".equals(method.name)) {
                continue;
            }
            AbstractInsnNode[] nodes = method.instructions.toArray();
            for (int i = 0; i < nodes.length; i++) {
                AbstractInsnNode insn = nodes[i];
                if (insn instanceof IntInsnNode) {
                    IntInsnNode intInsn = (IntInsnNode) insn;
                    if (intInsn.getOpcode() == BIPUSH && intInsn.operand == 40) {
                        intInsn.operand = 20;
                        modified = true;
                    }
                } else if (insn instanceof MethodInsnNode) {
                    MethodInsnNode call = (MethodInsnNode) insn;
                    if (call.getOpcode() == INVOKEVIRTUAL
                            && "java/lang/String".equals(call.owner)
                            && ("equals".equals(call.name) || "equalsIgnoreCase".equals(call.name))
                            && "(Ljava/lang/Object;)Z".equals(call.desc)) {
                        MethodInsnNode replacement = new MethodInsnNode(
                                INVOKESTATIC,
                                TRANSFORMER_OWNER,
                                "isLobbyOrDefaultTarget",
                                "(Ljava/lang/String;Ljava/lang/Object;)Z");
                        method.instructions.set(call, replacement);
                        modified = true;
                    }
                }
            }
        }

        if (!modified) {
            return basicClass;
        }
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(cw);
        return cw.toByteArray();
    }
}
