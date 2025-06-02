package boilerplate.rendering;

import boilerplate.utility.Logging;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL45;

import java.awt.image.BufferedImage;

import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;

/**
 * faces order:
 * 1: right  (+x)
 * 2: left  (-x)
 * 3: top  (+y)
 * 4: bottom  (-y)
 * 5: back  (+z)
 * 6: front  (-z)
 */
public class CubeMap {
    protected Integer id;

    public Integer facesNum;

    public int textureType = GL45.GL_UNSIGNED_BYTE;
    public int storedFormat = GL45.GL_RGBA;
    public int givenImgFormat = GL45.GL_RGBA;

    public CubeMap() {

    }

    public CubeMap(boolean generateId) {
        if (generateId) genId();
    }

    public void loadFaces(String... faceResourcePaths) {
        Texture.Image[] images = new Texture.Image[faceResourcePaths.length];
        for (int i = 0; i < faceResourcePaths.length; i++) {
            BufferedImage img = Texture.resourcePathToBufferedImage(faceResourcePaths[i]);
            assert img != null;
            images[i] = Texture.bufferedImageToByteImage(img);
        }
        loadFaces(images);
    }

    public void loadFaces(Texture.Image... faces) {
        if (faces.length > 6) {
            Logging.danger("Cube map cannot have more than 6 faces (%s were given). Aborting.", faces.length);
            return;
        }

        facesNum = faces.length;
        for (int i = 0; i < facesNum; i++) {
            Texture.Image image = faces[i];
            GL45.glTexImage2D(GL45.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, storedFormat, image.width, image.height, 0, givenImgFormat, textureType, image.buffer);
        }
    }

    public void genId() {
        if (id != null) {
            Logging.warn("Attempting to re-generate already generated cube map id, aborting");
            return;
        }
        id = GL45.glGenTextures();
    }

    public void bind() {
        GL45.glBindTexture(GL45.GL_TEXTURE_CUBE_MAP, id);
    }

    public void unbind() {
        GL45.glBindTexture(GL45.GL_TEXTURE_CUBE_MAP, 0);
    }
}
