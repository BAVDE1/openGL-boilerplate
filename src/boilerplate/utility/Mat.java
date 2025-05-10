package boilerplate.utility;

public class Mat {
    private interface Operation {
        double call(int x, int y);
    }

    private final int width;
    private final int height;

    private double[] matrix = new double[0];

    public Mat(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Mat(int width, int height, double[] matrix) {
        this(width, height);
        this.matrix = matrix;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void set(int x, int y, double v) {
        matrix[(y * height) + x] = v;
    }

    public double get(int x, int y) {
        return matrix[(y * height) + x];
    }

    private void performOperation(Mat mat, Operation op) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                mat.set(x, y, op.call(x, y));
            }
        }
    }

    public void addSelf(Mat other) {
        this.matrix = add(other).matrix;
    }

    public Mat add(Mat other) {
        if (doMatricesConflict(other)) return null;
        Mat newMat = new Mat(width, height);
        performOperation(newMat, (x, y) -> get(x, y) + other.get(x, y));
        return newMat;
    }

    public void scalarMulSelf(double s) {
        this.matrix = scalarMul(s).matrix;
    }

    public Mat scalarMul(double s) {
        Mat newMat = new Mat(width, height);
        performOperation(newMat, (x, y) -> get(x, y) * s);
        return newMat;
    }

    public void subSelf(Mat other) {
        this.matrix = sub(other).matrix;
    }

    public Mat sub(Mat other) {
        if (doMatricesConflict(other)) return null;
        return add(other.scalarMul(-1));
    }

    /**
     * just are their with and height the same lol
     */
    public boolean doMatricesConflict(Mat other) {
        if (width != other.width || height != other.height) {
            Logging.danger("Cannon perform operation on matrix(%s, %s), and other matrix(%s, %s).", width, height, other.width, other.height);
            return true;
        }
        return false;

    }
}
