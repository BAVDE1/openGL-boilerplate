package boilerplate.rendering.builders;

import boilerplate.utility.Logging;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

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
                0, 1, 2,  // front
                0, 2, 3,
                4, 5, 1,  // top
                4, 1, 0,
                4, 0, 3,  // left
                4, 3, 7,
                1, 5, 6,  // right
                1, 6, 2,
                3, 2, 6,  // bottom
                3, 6, 7,
                6, 5, 4,  // back
                6, 4, 7
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
                0, 1, 2,  // front
                1, 4, 2,  // hypotenuse
                4, 5, 2,
                2, 5, 3,  // opposite
                2, 3, 0,
                0, 3, 1,  // adjacent
                3, 4, 1,
                3, 5, 4  // back
        };
        p.pos = baseCenter;
        p.elementVertCount = 24;
        return p;
    }
}
