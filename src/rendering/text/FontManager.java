package src.rendering.text;

import src.rendering.ShaderHelper;
import src.rendering.Texture;
import src.utility.Logging;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/** State Machine */
public class FontManager {
    private static final int AsciiFrom = 32;
    private static final int AsciiTo = 256;
    private static final boolean antiAlias = false;

    public static class Glyph {
        public final int width, height;
        public final int x, y;

        public Glyph(int width, int height, int x, int y) {
            this.width = width;     this.height = height;
            this.x = x;             this.y = y;
        }
    }

    public static final int DEFAULT_TEXTURE_SLOT = 0;
    public static final int LORA = 0;

    private static int fontStyle = Font.PLAIN;
    private static int fontSize = 20;

    static Font loadedFont;
    static HashMap<Character, Glyph> glyphMap = new HashMap<>();

    public static void loadFont(String font) {
        loadedFont = new Font(font, fontStyle, fontSize);

        if (!loadedFont.getFamily().equalsIgnoreCase(font)) {
            Logging.warn("Font '%s' could not be found, using '%s' instead", font, loadedFont.getFamily());
        }
    }

    public static void loadCustomFont(int font) {
        try {
            loadedFont = Font.createFont(Font.TRUETYPE_FONT, new File(getFileForFont(font)));
            loadedFont.deriveFont(fontStyle, fontSize);
        } catch (IOException | FontFormatException e) {
            Logging.danger("Error preparing font, aborting. Thrown message:\n%s", e);
        }
    }

    public static String getFileForFont(int font) {
        return switch (font) {
            case LORA -> "res/fonts/Lora/Lora-VariableFont_wght.ttf";
            default -> {
                Logging.danger("Font '%s' does not exist and could not be loaded", font);
                yield "";
            }
        };
    }

    public static void setFontAttributes(int newFontStyle, int newFontSize) {
        setFontStyle(newFontStyle);
        setFontSize(newFontSize);
    }
    public static void setFontStyle(int newFontStyle) {
        fontStyle = newFontStyle;
    }
    public static void setFontSize(int newFontSize) {
        fontSize = newFontSize;
    }

    public static void generateAndBindFontTexture(ShaderHelper sh) {
        if (loadedFont == null) {
            Logging.danger("No font is loaded. Aborting image generation.");
            return;
        }

        generateFontImage(loadedFont).bind(DEFAULT_TEXTURE_SLOT, sh);
    }

    private static Texture generateFontImage(Font font) {
        int imgWidth = 0, imgHeight = 0;

        // find the dimensions of image
        for (int i = AsciiFrom; i < AsciiTo; i++) {
            CharFondler.CharMetrics charSize = CharFondler.getCharSize(font, (char) i, antiAlias);
            imgWidth += charSize.width;
            imgHeight = Math.max(imgHeight, charSize.height);
        }

        // actual image
        BufferedImage image = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();

        // draw glyphs
        int x = 0;
        for (int i = AsciiFrom; i < AsciiTo; i++) {
            BufferedImage charImage = CharFondler.createCharImage(font, (char) i, antiAlias);
            if (charImage == null) continue;

            int charWidth = charImage.getWidth();
            int charHeight = charImage.getHeight();
            graphics.drawImage(charImage, x, 0, null);

            x += charWidth;
            glyphMap.put((char) i, new Glyph(charWidth, charHeight, x, image.getHeight() - charHeight));
        }

        return new Texture(image);
    }
}
