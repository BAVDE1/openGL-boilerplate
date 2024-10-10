package src.rendering.text;

import src.rendering.ShaderHelper;
import src.rendering.Texture;
import src.utility.Logging;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

/** State Machine */
public class FontManager {
    public static class Glyph {
        public final int width, height;
        public final int x, y;

        public Glyph(int width, int height, int x, int y) {
            this.width = width;     this.height = height;
            this.x = x;             this.y = y;
        }
    }

    public static class LoadedFont {
        public int atlasOffset;
        public final HashMap<Character, Glyph> glyphMap = new HashMap<>();

        private final boolean aa;  // anti-aliasing
        public final Font loadedFont;
        public final String fontName;
        public final int fontStyle, fontSize;
        public final String loadedFontName;

        public int imgWidth = 0, imgHeight = 0;

        public LoadedFont(String fontName, int fontStyle, int fontSize) {
            this.aa = FontManager.antiAlias;
            this.fontName = fontName;
            this.fontStyle = fontStyle; this.fontSize = fontSize;

            this.loadedFontName = String.format("%s_%s_%s_%s", fontName, fontStyle, fontSize, aa);
            this.loadedFont = new Font(fontName, fontStyle, fontSize);
            findImageDimensions();

            if (!loadedFont.getFamily().equalsIgnoreCase(fontName)) {
                Logging.warn("Font '%s' could not be found, using '%s' instead", fontName, loadedFont.getFamily());
            }
        }

        /** Find the dimensions of the image */
        private void findImageDimensions() {
            for (int i = AsciiFrom; i < AsciiTo; i++) {
                CharFondler.CharMetrics charSize = CharFondler.getCharSize(loadedFont, (char) i, aa);
                imgWidth += charSize.width;
                imgHeight = Math.max(imgHeight, charSize.height);
            }
        }

        /** Draw the fonts glyphs at the y offset onto the given image */
        public void drawOntoFinalImage(BufferedImage image, Graphics2D graphics, int yOffset) {
            this.atlasOffset = yOffset;int x = 0;

            for (int i = AsciiFrom; i < AsciiTo; i++) {
                BufferedImage charImage = CharFondler.createCharImage(loadedFont, (char) i, aa);
                if (charImage == null) continue;

                int charWidth = charImage.getWidth();
                int charHeight = charImage.getHeight();
                graphics.drawImage(charImage, x, yOffset, null);

                x += charWidth;
                glyphMap.put((char) i, new Glyph(charWidth, charHeight, x, image.getHeight() - charHeight));
            }
        }
    }

    public static final int DEFAULT_TEXTURE_SLOT = 0;

    private static final int AsciiFrom = 32;
    private static final int AsciiTo = 256;
    public static boolean antiAlias = true;

    public static ArrayList<LoadedFont> allLoadedFonts = new ArrayList<>();
    public static HashMap<String, Integer> loadedFontUids = new HashMap<>();

    public static int fullWidth = 0, fullHeight = 0;
    private static Texture finalTexture;

    /** Load given font. Returns the font id / index */
    public static int loadFont(String font, int style, int size) {
        if (finalTexture != null) {
            Logging.danger("The font image atlas has already been generated, and so can't be appended to. Aborting.");
            return -1;
        }

        LoadedFont newFont = new LoadedFont(font, style, size);
        String fn = newFont.loadedFontName;
        if (loadedFontUids.containsKey(fn)) {
            Logging.warn("The font '%s' has already been loaded with uid '%s'. Aborting loading duplicate font", fn, loadedFontUids.get(fn));
            return -1;
        }

        fullWidth = Math.max(newFont.imgWidth, fullWidth);
        fullHeight += newFont.imgHeight;

        int id = allLoadedFonts.size();
        allLoadedFonts.add(newFont);
        loadedFontUids.put(fn, id);
        return id;
    }

    /** generate all font images onto one universal image atlas at their y offsets */
    public static void generateAndBindAllFonts(ShaderHelper sh) {
        BufferedImage fullImage = new BufferedImage(fullWidth, fullHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = fullImage.createGraphics();

        int yOffset = 0;
        for (LoadedFont lFont : allLoadedFonts) {
            lFont.drawOntoFinalImage(fullImage, graphics, yOffset);
            yOffset += lFont.imgHeight;
        }

        graphics.dispose();
        finalTexture = new Texture(fullImage);
        finalTexture.bind(DEFAULT_TEXTURE_SLOT, sh);
        Texture.writeToFile(fullImage);
    }
}
