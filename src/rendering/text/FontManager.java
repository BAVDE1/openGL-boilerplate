package src.rendering.text;

import src.rendering.ShaderHelper;
import src.rendering.Texture;
import src.utility.Logging;
import src.utility.Vec2;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/** State Machine */
public class FontManager {
    public static class Glyph {
        public final int width, height;
        public final int x, y;

        // texture coordinates
        public final Vec2 topLeft;
        public final Vec2 size;

        public Glyph(int x, int y, int width, int height) {
            this.width = width; this.height = height;
            this.x = x; this.y = y;

            this.topLeft = new Vec2(
                    (float) x / fullWidth,
                    (float) y / fullHeight
            );
            this.size = new Vec2(
                    (float) width / fullWidth,
                    (float) height / fullHeight
            );
        }

        @Override
        public String toString() {
            return String.format("(x=%s, y=%s, w=%s, h=%s)", x, y, width, height);
        }
    }

    public static class LoadedFont {
        private final boolean aa;  // anti-aliasing
        public final HashMap<Character, Glyph> glyphMap = new HashMap<>();

        private final String name;
        private Font font;

        public int imgWidth = 0, imgHeight = 0;

        public LoadedFont(String fontName, int fontStyle, int fontSize, boolean antiAlias) {
            aa = antiAlias;
            name = String.format("%s_%s_%s_%s", fontName, fontStyle, fontSize, aa);

            font = new Font(fontName, fontStyle, fontSize);
            findImageDimensions();

            if (!font.getFamily().equalsIgnoreCase(fontName)) {
                Logging.warn("Font '%s' could not be found, using '%s' instead", fontName, font.getFamily());
            }
        }

        public LoadedFont(int fontEnum, int fontStyle, int fontSize, boolean antiAlias) {
            aa = antiAlias;
            name = String.format("%s_%s_%s_%s", fontEnum, fontStyle, fontSize, aa);

            File fontFile = getCustomFontFile(fontEnum);
            if (fontFile == null) {
                Logging.danger("Font with enum value '%s' does not exist or has not been registered.", fontEnum);
                return;
            }

            try {
                font = Font.createFont(Font.TRUETYPE_FONT, fontFile);
                font = font.deriveFont(fontStyle, fontSize);
            } catch (IOException | FontFormatException e) {
                Logging.danger("Failed to load the font file '%s'\nError message: %s", fontFile.getPath(), e);
                return;
            }

            findImageDimensions();
        }

        private File getCustomFontFile(int fontEnum) {
            File fontFile = null;
            switch (fontEnum) {
                case FONT_NOVA -> fontFile = new File("res/fonts/Bona_Nova_SC/BonaNovaSC-Regular.ttf");
                case FONT_JACQUARD -> fontFile = new File("res/fonts/Jacquard_24/Jacquard24-Regular.ttf");
                case FONT_TINY -> fontFile = new File("res/fonts/Tiny5/Tiny5-Regular.ttf");
                case FONT_CASTORO -> fontFile = new File("res/fonts/Castoro_Titling/CastoroTitling-Regular.ttf");
                case FONT_LUGRASIMO -> fontFile = new File("res/fonts/Lugrasimo/Lugrasimo-Regular.ttf");
            }
            return fontFile;
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
        public void drawOntoFinalImage(Graphics2D graphics, int yOffset) {
            int x = 0;
            for (int i = AsciiFrom; i < AsciiTo; i++) {
                BufferedImage charImage = CharFondler.createCharImage(font, (char) i, aa);
                if (charImage == null) continue;

                int charWidth = charImage.getWidth();
                int charHeight = charImage.getHeight();
                graphics.drawImage(charImage, x, yOffset, null);

                glyphMap.put((char) i, new Glyph(x, yOffset, charWidth, charHeight));
                x += charWidth;
            }
        }
    }

    public static final int FONT_TEXTURE_SLOT = 0;

    public static final int FONT_NOVA = 0;
    public static final int FONT_JACQUARD = 1;  // recommended size: 42
    public static final int FONT_TINY = 2;      // recommended size: 24
    public static final int FONT_CASTORO = 3;
    public static final int FONT_LUGRASIMO = 4;

    private static final int AsciiFrom = 32;
    private static final int AsciiTo = 256;

    private final static ArrayList<LoadedFont> allLoadedFonts = new ArrayList<>();
    private final static HashMap<String, Integer> loadedFontUids = new HashMap<>();

    public static int fullWidth = 0, fullHeight = 0;
    private static Texture finalTexture;
    private static boolean initialized = false;

    /** Loads the default font first */
    public static void init() {
        initialized = true;
        loadFont(Font.DIALOG, Font.PLAIN, 32, true);
    }

    /** Returns -1 if there was an error */
    private static int runLoadFontChecks() {
        if (!initialized) {
            Logging.danger("FontManager has not been initialized! Aborting");
            return -1;
        }

        if (finalTexture != null) {
            Logging.danger("The font image atlas has already been generated, and so can't be appended to. Aborting.");
            return -1;
        }
        return 1;
    }

    public static int loadFont(int font, int style, int size, boolean antiAlias) {
        return loadFont("", font, style, size, antiAlias, false);
    }

    public static int loadFont(String font, int style, int size, boolean antiAlias) {
        return loadFont(font, -1, style, size, antiAlias, true);
    }

    /** Load given font. Returns the loaded font id / index */
    private static int loadFont(String fontStr, int fontInt, int style, int size, boolean antiAlias, boolean useString) {
        if (runLoadFontChecks() == -1) return -1;

        LoadedFont newFont = useString ? new LoadedFont(fontStr, style, size, antiAlias) : new LoadedFont(fontInt, style, size, antiAlias);
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
            lFont.drawOntoFinalImage(graphics, yOffset);
            yOffset += lFont.imgHeight;
        }

        graphics.dispose();
        finalTexture = new Texture(fullImage);
        finalTexture.bind(FONT_TEXTURE_SLOT, sh);
        Texture.writeToFile(fullImage);
        Logging.debug("%s fonts generated, bound to slot %s", allLoadedFonts.size(), FONT_TEXTURE_SLOT);
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
