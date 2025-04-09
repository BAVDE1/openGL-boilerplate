package boilerplate.rendering;

import boilerplate.utility.Vec2;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Creates shapes to be passed to a BufferBuilder.
 */
public class Shape2d {
    public static class Poly {
        public ShapeMode mode = null;
        public List<Vec2> points;
        public Vec2 pos = new Vec2();

        public Poly(Vec2... points) {
            this.points = List.of(points);
        }

        public Poly(ShapeMode mode, Vec2... points) {
            this(points);
            this.mode = mode;
        }

        public Poly addPos(Vec2 newPos) {
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

    public static Poly createRect(Vec2 topLeft, Vec2 size) {
        return new Poly(
                topLeft,
                topLeft.add(0, size.y),
                topLeft.add(size.x, 0),
                topLeft.add(size)
        );
    }

    public static Poly createRect(Vec2 topLeft, Vec2 size, ShapeMode mode) {
        Poly p = createRect(topLeft, size);
        p.mode = mode;
        return p;
    }

    public static Poly createLine(Vec2 point1, Vec2 point2, int thickness) {
        Vec2 normalised = point2.sub(point1).normalized();
        Vec2 perp = normalised.perpendicular().mul(thickness * .5f);
        return new Poly(
            point1.add(perp), point1.sub(perp),
            point2.add(perp), point2.sub(perp)
        );
    }
    public static Poly createLine(Vec2 point1, Vec2 point2, int thickness, ShapeMode mode) {
        Poly p = createLine(point1, point2, thickness);
        p.mode = mode;
        return p;
    }

    public static Poly createRectOutline(Vec2 topLeft, Vec2 size, int thickness) {
        Vec2 pos = topLeft.add(size.div(2));

        topLeft = topLeft.sub(pos);
        Vec2 topRight = topLeft.add(size.x, 0);
        Vec2 btmRight = topLeft.add(size);
        Vec2 btmLeft = topLeft.add(0, size.y);

        return new Poly(
                topLeft,  topLeft.add(thickness),
                topRight, topRight.add(-thickness, thickness),
                btmRight, btmRight.sub(thickness),
                btmLeft,  btmLeft.sub(-thickness, thickness),
                topLeft,  topLeft.add(thickness)
        ).addPos(pos);
    }

    public static Poly createRectOutline(Vec2 topLeft, Vec2 size, int thickness, ShapeMode mode) {
        Poly p = createRectOutline(topLeft, size, thickness);
        p.mode = mode;
        return p;
    }

    public static void sortPoints(Poly p) {
        sortPoints(p, new Vec2(1));
    }

    /** Sort points of a polygon so they are all listed in a clockwise direction */
    public static void sortPoints(Poly p, Vec2 center) {
        if (center.equals(0)) center = new Vec2(1);
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
            Vec2 point = p.points.get(i);
            float onSide = (point.x * center.y) - (point.y * center.x);
            float dot = point.sub(center).normalized().dot(center.normalized());
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
                Vec2[] newPoints = new Vec2[p.points.size()];
                for (int j = 0; j < p.points.size(); j++) {
                    newPoints[j] = p.points.get(indexOrder[j]);
                }
                p.points = List.of(newPoints);
                break;
            }
        }
    }

    /** Finds average of all points */
    public static Vec2 findCenter(List<Vec2> points) {
        ListIterator<Vec2> iterator = points.listIterator();
        Vec2 avg = iterator.next().getClone();
        while(iterator.hasNext()) {
            avg.addSelf(iterator.next());
        }
        return avg.div(points.size());
    }
}
