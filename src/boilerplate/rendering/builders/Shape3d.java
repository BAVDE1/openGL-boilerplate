package boilerplate.rendering.builders;

import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class Shape3d {
    public static class Poly3d {
        public ShapeMode mode = null;
        public List<Vector3f> points;
        public Vector3f pos = new Vector3f();

        public int[] elementIndex;
        public int elementVertCount = 0;

        public Poly3d(Vector3f... points) {
            this.points = List.of(points);
        }

        public Poly3d(ShapeMode mode, Vector3f... points) {
            this(points);
            this.mode = mode;
        }

        public Poly3d addPos(Vector3f newPos) {
            this.pos = newPos;
            return this;
        }

        public List<float[]> toArray() {
            List<float[]> l = new ArrayList<>(List.of());
            for (int i = 0; i < points.size(); i++) {
                l.add(i, new float[]{points.get(i).x, points.get(i).y});
            }
            return l;
        }

        public void rotatePoints(float roll, float pitch, float yaw) {
            for (Vector3f point : points) {
                point.rotateZ((float) Math.toRadians(roll));
                point.rotateY((float) Math.toRadians(yaw));
                point.rotateX((float) Math.toRadians(pitch));
            }
        }

        /** Centers all points around the current position */
        public void centerPointsAroundPos() {
            Vector3f offset = findCenter(points);
            for (Vector3f p : points) p.sub(offset);
        }
    }

    public static Poly3d createCubeE(Vector3f center, float size) {
        return createCuboidE(center, size, size, size);
    }

    public static Poly3d createCuboidE(Vector3f center, float width, float height, float depth) {
        float w = width * .5f;
        float h = height * .5f;
        float d = depth * .5f;
        Poly3d p = new Poly3d(
                new Vector3f(-w, h, d),  // tl
                new Vector3f(w, h, d),  // tr
                new Vector3f(w, -h, d),  // br
                new Vector3f(-w, -h, d),  // bl

                new Vector3f(-w, h, -d),
                new Vector3f(w, h, -d),
                new Vector3f(w, -h, -d),
                new Vector3f(-w, -h, -d)
        );
        p.elementIndex = new int[]{
                0, 2, 1,  // front
                0, 3, 2,
                4, 1, 5,  // top
                4, 0, 1,
                4, 3, 0,  // left
                4, 7, 3,
                1, 6, 5,  // right
                1, 2, 6,
                3, 6, 2,  // bottom
                3, 7, 6,
                6, 4, 5,  // back
                6, 7, 4
        };
        p.pos = center;
        p.elementVertCount = 36;
        return p;
    }

    public static Poly3d createRightAngleTriE(Vector3f cornerCenter, float height, float baseLength, float depth) {
        return createTriE(cornerCenter, height, baseLength, 0, depth);
    }

    public static Poly3d createRightAngleTriFlippedE(Vector3f cornerCenter, float height, float baseLength, float depth) {
        return createTriE(cornerCenter, height, 0, baseLength, depth);
    }

    public static Poly3d createTriE(Vector3f baseCenter, float height, float baseLeftLength, float baseRightLength, float depth) {
        float d = depth * .5f;
        Poly3d p = new Poly3d(
                new Vector3f(baseRightLength, 0, d),  // corner
                new Vector3f(-baseLeftLength, 0, d),  // left
                new Vector3f(0, height, d),  // top
                new Vector3f(baseRightLength, 0, -d),
                new Vector3f(-baseLeftLength, 0, -d),
                new Vector3f(0, height, -d)
        );
        p.elementIndex = new int[]{
                0, 2, 1,  // front
                1, 2, 4,  // hypotenuse
                4, 2, 5,
                2, 3, 5,  // opposite
                2, 0, 3,
                0, 1, 3,  // adjacent
                3, 1, 4,
                3, 4, 5  // back
        };
        p.pos = baseCenter;
        p.elementVertCount = 24;
        return p;
    }

    public static Poly3d createCube(Vector3f center, float size) {
        return createCuboid(center, size, size, size);
    }

    public static Poly3d createCuboid(Vector3f center, float width, float height, float depth) {
        float w = width * .5f;
        float h = height * .5f;
        float d = depth * .5f;
        return new Poly3d(
                new Vector3f(-w, -h, d),  // front
                new Vector3f(w, -h, d),
                new Vector3f(w, h, d),
                new Vector3f(w, h, d),
                new Vector3f(-w, h, d),
                new Vector3f(-w, -h, d),

                new Vector3f(w, -h, -d),  // back
                new Vector3f(-w, -h, -d),
                new Vector3f(w, h, -d),
                new Vector3f(-w, h, -d),
                new Vector3f(w, h, -d),
                new Vector3f(-w, -h, -d),

                new Vector3f(w, h, -d),  // right
                new Vector3f(w, h, d),
                new Vector3f(w, -h, -d),
                new Vector3f(w, -h, d),
                new Vector3f(w, -h, -d),
                new Vector3f(w, h, d),

                new Vector3f(-w, h, d),  // left
                new Vector3f(-w, h, -d),
                new Vector3f(-w, -h, -d),
                new Vector3f(-w, -h, -d),
                new Vector3f(-w, -h, d),
                new Vector3f(-w, h, d),

                new Vector3f(w, h, -d),  // top
                new Vector3f(-w, h, -d),
                new Vector3f(w, h, d),
                new Vector3f(-w, h, d),
                new Vector3f(w, h, d),
                new Vector3f(-w, h, -d),

                new Vector3f(-w, -h, -d),  // bottom
                new Vector3f(w, -h, -d),
                new Vector3f(w, -h, d),
                new Vector3f(w, -h, d),
                new Vector3f(-w, -h, d),
                new Vector3f(-w, -h, -d)
        );
    }

    public static float[][] defaultCubeNormals() {
        return new float[][] {
                new float[] {0, 0, -1},  // front
                new float[] {0, 0, -1},
                new float[] {0, 0, -1},
                new float[] {0, 0, -1},
                new float[] {0, 0, -1},
                new float[] {0, 0, -1},

                new float[] {0, 0, 1},  // back
                new float[] {0, 0, 1},
                new float[] {0, 0, 1},
                new float[] {0, 0, 1},
                new float[] {0, 0, 1},
                new float[] {0, 0, 1},

                new float[] {-1, 0, 0},  // right
                new float[] {-1, 0, 0},
                new float[] {-1, 0, 0},
                new float[] {-1, 0, 0},
                new float[] {-1, 0, 0},
                new float[] {-1, 0, 0},

                new float[] {1, 0, 0},  // left
                new float[] {1, 0, 0},
                new float[] {1, 0, 0},
                new float[] {1, 0, 0},
                new float[] {1, 0, 0},
                new float[] {1, 0, 0},

                new float[] {0, -1, 0},  // top
                new float[] {0, -1, 0},
                new float[] {0, -1, 0},
                new float[] {0, -1, 0},
                new float[] {0, -1, 0},
                new float[] {0, -1, 0},

                new float[] {0, 1, 0},  // bottom
                new float[] {0, 1, 0},
                new float[] {0, 1, 0},
                new float[] {0, 1, 0},
                new float[] {0, 1, 0},
                new float[] {0, 1, 0}
        };
    }

    /** Finds average of all points */
    public static Vector3f findCenter(List<Vector3f> points) {
        ListIterator<Vector3f> iterator = points.listIterator();
        Vector3f avg = new Vector3f(iterator.next());
        while(iterator.hasNext()) avg.add(iterator.next());
        return avg.div(points.size());
    }
}
