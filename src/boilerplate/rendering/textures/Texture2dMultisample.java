package boilerplate.rendering.textures;

import org.lwjgl.opengl.GL45;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.lwjgl.opengl.GL32.GL_TEXTURE_2D_MULTISAMPLE;

public class Texture2dMultisample extends Texture2d {
    public Texture2dMultisample() {
        textureTarget = GL_TEXTURE_2D_MULTISAMPLE;
    }

    public Texture2dMultisample(Dimension size, boolean generateId) {
        this();
        this.size = size;
        if (generateId) genId();
    }

    public Texture2dMultisample(String path) {
        this();
        createTextureFromImage(Image.loadImageFromPathSTB(path));
    }

    public Texture2dMultisample(BufferedImage buffImg) {
        this();
        createTextureFromImgBuff(buffImg);
    }

    public void createTexture2d(int storedFormat, int samples) {
        GL45.glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, samples, storedFormat, size.width, size.height, true);
    }
}
