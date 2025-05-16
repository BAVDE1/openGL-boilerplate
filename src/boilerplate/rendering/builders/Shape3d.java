package boilerplate.rendering.builders;

import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class Shape3d {
    public static class Poly3d {
        public ShapeMode mode = null;
        public List<Vector3f> points;
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
                l.add(i, new float[] {points.get(i).x, points.get(i).y});
            }
            return l;
        }
    }
}
