package common;

public class CoordinateSystem {
    public static final int MAX_MAP_SIZE = 64;

    private final int originX;
    private final int originY;

    public CoordinateSystem(final int originX, final int originY) {
        this.originX = originX;
        this.originY = originY;
    }

    public int[] toRelative(final int x, final int y) {
        return new int[] {x - originX + MAX_MAP_SIZE, y - originY + MAX_MAP_SIZE};
    }

    public int[] toAbsolute(final int x, final int y) {
        return new int[] {x + originX - MAX_MAP_SIZE, y + originY - MAX_MAP_SIZE};
    }
}
