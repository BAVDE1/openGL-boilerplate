package rendering.text;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Various functions for finding char rendering information
 */
public class CharFondler {
    public static class CharMetrics {
        int width, height, ascent;
        CharMetrics(int w, int h, int asc) {
            width = w; height = h; ascent = asc;
        }
    }

    static class ImgContainer {
        final Graphics2D graphics;
        final BufferedImage img;
        ImgContainer(Graphics2D g, BufferedImage b) {
            graphics = g; img = b;
        }
    }

    private static ImgContainer generateImg(Font font, int w, int h, boolean antiAlias) {
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        if (antiAlias) {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        graphics.setFont(font);
        return new ImgContainer(graphics, image);
    }

    /** todo: the width doesn't take into account the lean of italicised characters :( */
    public static CharMetrics getCharSize(Font font, char ch, boolean antiAlias) {
        Graphics2D graphics = generateImg(font, 1, 1, antiAlias).graphics;
        FontMetrics metrics = graphics.getFontMetrics();
        graphics.dispose();
        return new CharMetrics(metrics.charWidth(ch), metrics.getHeight(), metrics.getAscent());
    }

    public static BufferedImage createCharImage(Font font, char ch, boolean antiAlias) {
        CharMetrics charSize = getCharSize(font, ch, antiAlias);
        if (charSize.width == 0) return null;

        // Create image for the char
        ImgContainer container = generateImg(font, charSize.width, charSize.height, antiAlias);
        container.graphics.setPaint(java.awt.Color.WHITE);
        container.graphics.drawString(String.valueOf(ch), 0, charSize.ascent);
        container.graphics.dispose();
        return container.img;
    }
}
