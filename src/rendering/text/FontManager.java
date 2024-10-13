package src.rendering.text;

import src.rendering.ShaderHelper;
import src.rendering.Texture;
import src.utility.Logging;
import src.utility.Vec2f;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

/** State Machine */
public class FontManager {
    public static class Glyph {
        public final int width, height;
        public final int x, y;
        public final Vec2f topLeft;
        public final Vec2f bottomRight;

        public Glyph(int x, int y, int width, int height) {
            this.width = width; this.height = height;
            this.x = x; this.y = y;

            this.topLeft = new Vec2f(
                    (float) x / fullWidth,
                    (float) y / fullHeight
            );
            this.bottomRight = new Vec2f(
                    topLeft.x + (float) width / fullWidth,
                    topLeft.y + (float) height / fullHeight
            );
        }

        @Override
        public String toString() {
            return String.format("(x=%s, y=%s, w=%s, h=%s)", x, y, width, height);
        }
    }

    public static class LoadedFont {
        private final boolean aa;  // anti-aliasing
        public int atlasOffset;
        public final HashMap<Character, Glyph> glyphMap = new HashMap<>();

        public final String name;
        public final Font font;

        public int imgWidth = 0, imgHeight = 0;

        public LoadedFont(String fontName, int fontStyle, int fontSize) {
            this.aa = FontManager.antiAlias;
            this.name = String.format("%s_%s_%s_%s", fontName, fontStyle, fontSize, aa);
            this.font = new Font(fontName, fontStyle, fontSize);

            findImageDimensions();

            if (!font.getFamily().equalsIgnoreCase(fontName)) {
                Logging.warn("Font '%s' could not be found, using '%s' instead", fontName, font.getFamily());
            }
        }

        /** Find the dimensions of the image */
        private void findImageDimensions() {
            for (int i = AsciiFrom; i < AsciiTo; i++) {
                CharFondler.CharMetrics charSize = CharFondler.getCharSize(font, (char) i, aa);
                imgWidth += charSize.width;
                imgHeight = Math.max(imgHeight, charSize.height);
            }
        }

        /** Draw the fonts glyphs at the y offset onto the given image */
        public void drawOntoFinalImage(BufferedImage image, Graphics2D graphics, int yOffset) {
            this.atlasOffset = yOffset;
            int x = 0;

            for (int i = AsciiFrom; i < AsciiTo; i++) {
                BufferedImage charImage = CharFondler.createCharImage(font, (char) i, aa);
                if (charImage == null) continue;

                int charWidth = charImage.getWidth();
                int charHeight = charImage.getHeight();
                graphics.drawImage(charImage, x, yOffset, null);

                x += charWidth;
                glyphMap.put((char) i, new Glyph(x, yOffset, charWidth, charHeight));
            }
        }
    }

    public static final int DEFAULT_TEXTURE_SLOT = 0;

    private static final int AsciiFrom = 32;
    private static final int AsciiTo = 256;
    public static boolean antiAlias = true;

    private final static ArrayList<LoadedFont> allLoadedFonts = new ArrayList<>();
    private final static HashMap<String, Integer> loadedFontUids = new HashMap<>();

    public static int fullWidth = 0, fullHeight = 0;
    private static Texture finalTexture;
    private static boolean initialized = false;

    /** Loads the default font first */
    public static void init() {
        initialized = true;
        loadFont(Font.DIALOG, Font.PLAIN, 16);
    }

    /** Load given font. Returns the font id / index */
    public static int loadFont(String font, int style, int size) {
        if (!initialized) {
            Logging.danger("FontManager has not been initialized! Aborting");
            return -1;
        }

        if (finalTexture != null) {
            Logging.danger("The font image atlas has already been generated, and so can't be appended to. Aborting.");
            return -1;
        }

        LoadedFont newFont = new LoadedFont(font, style, size);
        String fn = newFont.name;
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

    public static LoadedFont getLoadedFont(int loadedFontId) {
        if (loadedFontId < 0 || loadedFontId > allLoadedFonts.size() - 1) {
            Logging.danger("Font with id '%s' does not exist. Returning default font.", loadedFontId);
            return allLoadedFonts.getFirst();
        }
        return allLoadedFonts.get(loadedFontId);
    }

    public static int getLoadedId(String loadedFontName) {
        if (!loadedFontUids.containsKey(loadedFontName)) {
            Logging.danger("Font with name '%s' does not exist. Returning default font id.", loadedFontName);
            return 0;
        }
        return loadedFontUids.get(loadedFontName);
    }
}
