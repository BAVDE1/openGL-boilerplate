package boilerplate.rendering.builders;

import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Creates shapes to be passed to a BufferBuilder.
 */
public class Shape2d {
    public static class Poly {
        public ShapeMode mode = null;
        public List<Vector2f> points;
        public Vector2f pos = new Vector2f();

        public Poly(Vector2f... points) {
            this.points = List.of(points);
        }

        public Poly(ShapeMode mode, Vector2f... points) {
            this(points);
            this.mode = mode;
        }

        public Poly addPos(Vector2f newPos) {
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

    public static Poly createRect(Vector2f topLeft, Vector2f size) {
        return new Poly(
                topLeft,
                topLeft.add(0, size.y),
                topLeft.add(size.x, 0),
                topLeft.add(size)
        );
    }

    public static Poly createRect(Vector2f topLeft, Vector2f size, ShapeMode mode) {
        Poly p = createRect(topLeft, size);
        p.mode = mode;
        return p;
    }

    public static Poly createLine(Vector2f point1, Vector2f point2, int thickness) {
        Vector2f normalised = point2.sub(point1).normalize();
        Vector2f perp = normalised.perpendicular().mul(thickness * .5f);
        return new Poly(
            point1.add(perp), point1.sub(perp),
            point2.add(perp), point2.sub(perp)
        );
    }
    public static Poly createLine(Vector2f point1, Vector2f point2, int thickness, ShapeMode mode) {
        Poly p = createLine(point1, point2, thickness);
        p.mode = mode;
        return p;
    }

    public static Poly createRectOutline(Vector2f topLeft, Vector2f size, int thickness) {
        Vector2f pos = topLeft.add(size.div(2));

        topLeft = topLeft.sub(pos);
        Vector2f topRight = topLeft.add(size.x, 0);
        Vector2f btmRight = topLeft.add(size);
        Vector2f btmLeft = topLeft.add(0, size.y);

        return new Poly(
                topLeft,  topLeft.add(thickness, thickness),
                topRight, topRight.add(-thickness, thickness),
                btmRight, btmRight.sub(thickness, thickness),
                btmLeft,  btmLeft.sub(-thickness, thickness),
                topLeft,  topLeft.add(thickness, thickness)
        ).addPos(pos);
    }

    public static Poly createRectOutline(Vector2f topLeft, Vector2f size, int thickness, ShapeMode mode) {
        Poly p = createRectOutline(topLeft, size, thickness);
        p.mode = mode;
        return p;
    }

    public static void sortPoints(Poly p) {
        sortPoints(p, new Vector2f(1));
    }

    /** Sort points of a polygon so they are all listed in a clockwise direction */
    public static void sortPoints(Poly p, Vector2f center) {
        if (center.equals(0)) center = new Vector2f(1);
        final int fidelity = 2;

        // functions
        interface Func {void call(ArrayList<ArrayList<Integer>> radixGroups);}
        Func buildRadixGroups = radixGroups -> {
            radixGroups.clear();
            for (int i = 0; i < 10; i++) {radixGroups.add(new ArrayList<>());}
        };

        interface Func2 {void call(int[] arr, ArrayList<ArrayList<Integer>> radixGroups);}
        Func2 reOrderIndexes = (arr, radixGroups) -> {
            int i = 0;
            for (ArrayList<Integer> group : radixGroups) {
                for (int index : group) arr[i++] = index;
            }
        };

        // pre calculate comparisons
        int[] indexOrder = new int[p.points.size()];
        String[] comparableItems = new String[p.points.size()];
        for(int i = 0; i < p.points.size(); i++) {
            Vector2f point = p.points.get(i);
            float onSide = (point.x * center.y) - (point.y * center.x);
            float dot = point.sub(center).normalize().dot(center.normalize());
            dot = onSide > 0 ? dot+3 : (-dot)+1;

            indexOrder[i] = i;
            comparableItems[i] = String.valueOf((int) Math.floor(dot * Math.pow(10, fidelity)));
        }

        // radix sort all the points
        ArrayList<ArrayList<Integer>> radixGroups = new ArrayList<>();
        buildRadixGroups.call(radixGroups);

        int on_digit = 0;
        int i = 0;
        while(i < p.points.size()) {
            int index = indexOrder[i];
            String comparison = comparableItems[index];

            int groupInto = 0;
            try {groupInto = comparison.charAt(fidelity - on_digit) - '0';}
            catch (IndexOutOfBoundsException _){}
            radixGroups.get(groupInto).add(index);

            // next digit
            if (++i == p.points.size()) {
                i = 0;
                on_digit++;
                reOrderIndexes.call(indexOrder, radixGroups);
                buildRadixGroups.call(radixGroups);
            }

            // finish
            if (fidelity - on_digit < 0) {
                Vector2f[] newPoints = new Vector2f[p.points.size()];
                for (int j = 0; j < p.points.size(); j++) {
                    newPoints[j] = p.points.get(indexOrder[j]);
                }
                p.points = List.of(newPoints);
                break;
            }
        }
    }

    /** Finds average of all points */
    public static Vector2f findCenter(List<Vector2f> points) {
        ListIterator<Vector2f> iterator = points.listIterator();
        Vector2f avg = new Vector2f(iterator.next());
        while(iterator.hasNext()) {
            avg.add(iterator.next());
        }
        return avg.div(points.size());
    }
}
