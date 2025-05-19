package boilerplate.rendering.builders;

import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class Shape3d {
    public static class Poly3d {
        public ShapeMode mode = null;
        public List<Vector3f> points;
        public int[] elementIndex;
        public Vector3f pos = new Vector3f();

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
    }

    public static Poly3d createCubeE(Vector3f center, float size) {
        float s = size * .5f;
        Poly3d p = new Poly3d();
        p.pos = center;
        p.points = List.of(
                new Vector3f(-s, s, s),  // tl
                new Vector3f(s, s, s),  // tr
                new Vector3f(s, -s, s),  // br
                new Vector3f(-s, -s, s),  // bl
                new Vector3f(-s, s, -s),
                new Vector3f(s, s, -s),
                new Vector3f(s, -s, -s),
                new Vector3f(-s, -s, -s)
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
        return p;
    }
}
