package dev.infinitycraft.loading;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

public final class SplashRenderer {
    private static final String PRIMARY_RESOURCE = "/assets/infinityclient/textures/gui/loading/fullscreen.png";
    private static final long THROTTLE_MS = 120L;

    private static int texture;
    private static int texWidth;
    private static int texHeight;
    private static boolean failed;
    private static boolean announced;
    private static volatile boolean finished;
    private static volatile Thread glThread;
    private static long lastTickMs;
    private static int stepCount;
    private static int phase;
    private static float progress = 0.04F;

    private SplashRenderer() {
    }

    public static boolean draw() {
        if (failed || finished) {
            return false;
        }
        try {
            if (!Display.isCreated()) {
                return false;
            }
            if (glThread == null) {
                glThread = Thread.currentThread();
            } else if (Thread.currentThread() != glThread) {
                return true;
            }
            if (progress < 0.06F) {
                progress = 0.06F;
            }
            lastTickMs = System.currentTimeMillis();
            pumpMessages();
            boolean ok = renderFrame(progress);
            if (ok && !announced) {
                announced = true;
                System.out.println("[InfinityCraft] Moss Island fullscreen splash displayed on AEDF1 1.5.2");
            }
            return ok;
        } catch (Throwable t) {
            failed = true;
            System.err.println("[InfinityCraft] Fullscreen splash unavailable: " + t.getClass().getSimpleName());
            return false;
        }
    }

    public static void step(String stage) {
        if (failed || finished) {
            return;
        }
        try {
            if ("beginMinecraftLoading".equals(stage)) {
                phase = 1;
                stepCount = 0;
                if (progress < 0.05F) {
                    progress = 0.05F;
                }
                if (glThread == null && Display.isCreated()) {
                    glThread = Thread.currentThread();
                }
                renderIfMainThread(true);
            } else if ("loadScreen".equals(stage)) {
                if (phase < 2) {
                    phase = 2;
                }
                if (progress < 0.42F) {
                    progress = 0.42F;
                }
                renderIfMainThread(true);
            } else if ("finishMinecraftLoading".equals(stage)) {
                phase = 3;
                stepCount = 0;
                if (progress < 0.50F) {
                    progress = 0.50F;
                }
                renderIfMainThread(true);
            } else if ("onInitializationComplete".equals(stage)) {
                phase = 4;
                stepCount = 0;
                if (progress < 0.85F) {
                    progress = 0.85F;
                }
                renderIfMainThread(true);
            } else {
                tickProgress();
            }
        } catch (Throwable ignored) {
        }
    }

    public static void tickProgress() {
        if (failed || finished) {
            return;
        }
        try {
            Thread current = Thread.currentThread();
            if (glThread == null) {
                if (!Display.isCreated()) {
                    return;
                }
                glThread = current;
            } else if (current != glThread) {
                return;
            }
            stepCount++;
            float target = computeTargetProgress();
            if (target > progress) {
                progress = target;
            }
            renderIfMainThread(false);
        } catch (Throwable ignored) {
        }
    }

    public static void finish() {
        if (finished) {
            return;
        }
        try {
            if (!failed && glThread != null && Thread.currentThread() == glThread && Display.isCreated()) {
                progress = 1.0F;
                pumpMessages();
                renderFrame(1.0F);
            }
        } catch (Throwable ignored) {
        } finally {
            finished = true;
        }
    }

    private static float computeTargetProgress() {
        if (phase <= 1) {
            float curve = 1.0F - (1.0F / (1.0F + stepCount * 0.018F));
            return 0.05F + 0.36F * curve;
        }
        if (phase == 2) {
            float curve = 1.0F - (1.0F / (1.0F + stepCount * 0.008F));
            return 0.42F + 0.07F * curve;
        }
        if (phase == 3) {
            float curve = 1.0F - (1.0F / (1.0F + stepCount * 0.010F));
            return 0.50F + 0.34F * curve;
        }
        float curve = 1.0F - (1.0F / (1.0F + stepCount * 0.006F));
        return 0.85F + 0.13F * curve;
    }

    private static void renderIfMainThread(boolean force) throws Exception {
        if (failed || finished || glThread == null || Thread.currentThread() != glThread) {
            return;
        }
        long now = System.currentTimeMillis();
        if (!force && (now - lastTickMs) < THROTTLE_MS) {
            return;
        }
        lastTickMs = now;
        if (!Display.isCreated()) {
            return;
        }
        pumpMessages();
        renderFrame(progress);
    }

    private static void pumpMessages() {
        try {
            Display.processMessages();
        } catch (Throwable ignored) {
        }
    }

    private static boolean renderFrame(float currentProgress) throws Exception {
        if (texture == 0) {
            upload();
        }
        int width = Display.getWidth();
        int height = Display.getHeight();
        if (width < 1 || height < 1) {
            return false;
        }

        int prevTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        int prevMatrixMode = GL11.glGetInteger(GL11.GL_MATRIX_MODE);
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glOrtho(0.0D, (double) width, (double) height, 0.0D, -1.0D, 1.0D);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();

        try {
            GL11.glViewport(0, 0, width, height);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_FOG);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glEnable(GL11.GL_TEXTURE_2D);

            // Moss Island deep forest background (#0b1111)
            GL11.glClearColor(0.043137256F, 0.06666667F, 0.06666667F, 1.0F);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            float targetAspect = 16.0F / 9.0F;
            float windowAspect = (float) width / (float) height;
            float u0 = 0.0F;
            float v0 = 0.0F;
            float u1 = 1.0F;
            float v1 = 1.0F;
            if (windowAspect > targetAspect) {
                float visibleHeight = targetAspect / windowAspect;
                float pad = (1.0F - visibleHeight) * 0.5F;
                v0 = pad;
                v1 = 1.0F - pad;
            } else if (windowAspect < targetAspect) {
                float visibleWidth = windowAspect / targetAspect;
                float pad = (1.0F - visibleWidth) * 0.5F;
                u0 = pad;
                u1 = 1.0F - pad;
            }

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glTexCoord2f(u0, v1);
            GL11.glVertex2f(0.0F, (float) height);
            GL11.glTexCoord2f(u1, v1);
            GL11.glVertex2f((float) width, (float) height);
            GL11.glTexCoord2f(u1, v0);
            GL11.glVertex2f((float) width, 0.0F);
            GL11.glTexCoord2f(u0, v0);
            GL11.glVertex2f(0.0F, 0.0F);
            GL11.glEnd();

            drawProgressBar(width, height, currentProgress);
        } finally {
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glPopMatrix();
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glPopMatrix();
            GL11.glPopAttrib();
            GL11.glMatrixMode(prevMatrixMode);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, prevTexture);
        }

        Display.update();
        return true;
    }

    private static void drawProgressBar(int width, int height, float rawProgress) {
        float clamped = rawProgress < 0.03F ? 0.03F : (rawProgress > 1.0F ? 1.0F : rawProgress);
        float barWidth = Math.max(260.0F, Math.min(width * 0.54F, 640.0F));
        float barHeight = Math.max(10.0F, Math.min(height * 0.022F, 16.0F));
        float barX = (width - barWidth) * 0.5F;
        float barY = height - Math.max(36.0F, height * 0.085F);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glShadeModel(GL11.GL_SMOOTH);

        // Outer shadow backdrop (#0b1111, alpha 0.80)
        fillRect(barX - 5.0F, barY - 5.0F, barWidth + 10.0F, barHeight + 10.0F,
                0.043F, 0.067F, 0.067F, 0.80F);

        // Moss Island border (#34413b)
        fillRect(barX - 2.0F, barY - 2.0F, barWidth + 4.0F, barHeight + 4.0F,
                0.204F, 0.255F, 0.231F, 0.96F);

        // Moss Island dark track (#121b1a)
        fillRect(barX, barY, barWidth, barHeight,
                0.071F, 0.106F, 0.102F, 0.98F);

        // Progress fill: horizontal gradient from moss green (#7ea172) to warm gold (#e3ba63)
        float fillWidth = Math.max(4.0F, barWidth * clamped);
        GL11.glBegin(GL11.GL_QUADS);
        // Left: #7ea172 (0.494, 0.631, 0.447)
        GL11.glColor4f(0.494F, 0.631F, 0.447F, 1.0F);
        GL11.glVertex2f(barX, barY);
        GL11.glVertex2f(barX, barY + barHeight);
        // Right: #e3ba63 (0.890, 0.729, 0.388)
        GL11.glColor4f(0.890F, 0.729F, 0.388F, 1.0F);
        GL11.glVertex2f(barX + fillWidth, barY + barHeight);
        GL11.glVertex2f(barX + fillWidth, barY);
        GL11.glEnd();

        // Top cream highlight (#f4f1e7, alpha 0.34)
        fillRect(barX, barY, fillWidth, Math.max(1.5F, barHeight * 0.22F),
                0.957F, 0.945F, 0.906F, 0.34F);

        // Subtle live pulse at the leading edge (#f4f1e7)
        float pulse = 0.45F + 0.35F * (float) Math.sin((System.currentTimeMillis() % 2000L) * (Math.PI / 1000.0D));
        float tipWidth = Math.min(fillWidth, Math.max(6.0F, barHeight * 1.1F));
        fillRect(barX + fillWidth - tipWidth, barY, tipWidth, barHeight,
                0.957F, 0.945F, 0.906F, pulse);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void fillRect(float x, float y, float w, float h, float r, float g, float b, float a) {
        GL11.glColor4f(r, g, b, a);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x, y + h);
        GL11.glVertex2f(x + w, y + h);
        GL11.glVertex2f(x + w, y);
        GL11.glEnd();
    }

    private static void upload() throws Exception {
        InputStream stream = SplashRenderer.class.getResourceAsStream(PRIMARY_RESOURCE);
        if (stream == null) {
            throw new IllegalStateException("Missing Moss Island fullscreen artwork");
        }
        BufferedImage image;
        try {
            image = ImageIO.read(stream);
        } finally {
            stream.close();
        }
        if (image == null || image.getWidth() < 1 || image.getHeight() < 1) {
            throw new IllegalStateException("Invalid fullscreen artwork dimensions");
        }
        texWidth = image.getWidth();
        texHeight = image.getHeight();
        int totalPixels = texWidth * texHeight;
        int[] argb = new int[totalPixels];
        image.getRGB(0, 0, texWidth, texHeight, argb, 0, texWidth);

        ByteBuffer buffer = BufferUtils.createByteBuffer(totalPixels * 4);
        for (int i = 0; i < totalPixels; i++) {
            int pixel = argb[i];
            buffer.put((byte) ((pixel >> 16) & 255));
            buffer.put((byte) ((pixel >> 8) & 255));
            buffer.put((byte) (pixel & 255));
            buffer.put((byte) ((pixel >>> 24) & 255));
        }
        buffer.flip();

        texture = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, 33071);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, 33071);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, 32856, texWidth, texHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
    }
}
