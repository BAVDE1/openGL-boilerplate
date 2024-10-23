package src.rendering;

import src.game.Constants;
import src.utility.Vec2;
import src.utility.Vec3;

import java.awt.*;
import java.util.*;
import java.util.List;

public class Shape {
    public static class Mode {
        int type;
        List<Vec3> vars;

        public Mode() {this(Constants.MODE_NIL);}
        public Mode(int texSlot, Vec2 texTopLeft, Vec2 texSize) {
            this(Constants.MODE_TEX);
            this.vars = List.of(new Vec3[]{
                    new Vec3(texTopLeft, texSlot),
                    new Vec3(texTopLeft.add(0, texSize.y), texSlot),
                    new Vec3(texTopLeft.add(texSize.x, 0), texSlot),
                    new Vec3(texTopLeft.add(texSize), texSlot)
            });
        }
        public Mode(Color col) {
            this(Constants.MODE_COL);
            this.vars = List.of(new Vec3[] {new Vec3(col)});
        }

        public Mode(int mode) {this.type = mode;}
        public Mode(int mode, Vec3... modeVars) {
            this(mode);
            this.vars = Arrays.stream(modeVars).toList();
        }

        /** Get vec3 at inx, or last (or empty vec3 if no vars exist) */
        public Vec3 getVar(int inx) {
            if (vars == null || vars.isEmpty()) return new Vec3();
            if (inx >= vars.size()) return vars.getLast();
            return vars.get(inx);
        }
    }

    public static class Quad {
        public Mode mode = new Mode();
        public Vec2 a; public Vec2 b;
        public Vec2 c; public Vec2 d;

        public Quad(Vec2 a, Vec2 b, Vec2 c, Vec2 d) {
            this.a = a; this.b = b;
            this.c = c; this.d = d;
        }
        public Quad(Vec2 a, Vec2 b, Vec2 c, Vec2 d, Mode mode) {
            this(a, b, c, d);
            this.mode = mode;
        }

        @Override
        public String toString() {return String.format("Quad(%s, %s, %s, %s)", a, b, c, d);}
    }

    public static class Poly {
        public Mode mode = new Mode();
        public List<Vec2> points;
        public Vec2 pos;

        public Poly(Vec2 pos, Vec2... points) {
            this.points = List.of(points);
            this.pos = pos;
        }
        public Poly(Vec2 pos, Mode mode, Vec2... points) {
            this(pos, points);
            this.mode = mode;
        }
    }

    public static Quad createRect(Vec2 topLeft, Vec2 size) {
        return new Quad(
                topLeft,
                topLeft.add(0, size.y),
                topLeft.add(size.x, 0),
                topLeft.add(size)
        );
    }
    public static Quad createRect(Vec2 topLeft, Vec2 size, Mode mode) {
        Quad q = createRect(topLeft, size);
        q.mode = mode;
        return q;
    }

    public static Quad createLine(Vec2 point1, Vec2 point2, int thickness) {
        Vec2 normalised = point2.sub(point1).normalized();
        Vec2 perp = normalised.perpendicular().mul(thickness * .5f);
        return new Quad(
            point1.add(perp), point1.sub(perp),
            point2.add(perp), point2.sub(perp)
        );
    }
    public static Quad createLine(Vec2 point1, Vec2 point2, int thickness, Mode mode) {
        Quad q = createLine(point1, point2, thickness);
        q.mode = mode;
        return q;
    }

    public static Poly createRectOutline(Vec2 topLeft, Vec2 size, int thickness) {
        Vec2 pos = topLeft.add(size.div(2));

        topLeft = topLeft.sub(pos);
        Vec2 topRight = topLeft.add(size.x, 0);
        Vec2 btmRight = topLeft.add(size);
        Vec2 btmLeft = topLeft.add(0, size.y);

        return new Poly(pos,
                topLeft,  topLeft.add(thickness),
                topRight, topRight.add(-thickness, thickness),
                btmRight, btmRight.sub(thickness),
                btmLeft,  btmLeft.sub(-thickness, thickness),
                topLeft,  topLeft.add(thickness)
        );
    }
    public static Poly createRectOutline(Vec2 topLeft, Vec2 size, int thickness, Mode mode) {
        Poly p = createRectOutline(topLeft, size, thickness);
        p.mode = mode;
        return p;
    }

    /** Sort points of a polygon so they are all listed in a clockwise direction */
    public static void sortPoints(Poly p) {
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
        int[] comparableItems = new int[p.points.size()];
        for(int i = 0; i < p.points.size(); i++) {
            Vec2 point = p.points.get(i);
            float onSide = (point.x * p.pos.y) - (point.y * p.pos.x);
            float dot = point.sub(p.pos).normalized().dot(p.pos.normalized());
            dot = onSide > 0 ? dot+3 : (-dot)+1;

            indexOrder[i] = i;
            comparableItems[i] = (int) Math.floor(dot * Math.pow(10, fidelity));
        }

        // radix sort all the points
        ArrayList<ArrayList<Integer>> radixGroups = new ArrayList<>();
        buildRadixGroups.call(radixGroups);

        int on_digit = 0;
        int i = 0;
        while(i < p.points.size()) {
            int index = indexOrder[i];
            int comparison = comparableItems[index];

            int groupInto = String.valueOf(comparison).charAt(fidelity - on_digit) - '0';
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
