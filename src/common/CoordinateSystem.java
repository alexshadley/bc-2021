package common;

import battlecode.common.MapLocation;

/**
 * Produce a coordinate system relative to a MapLocation, allowing coordinates to be compressed enough to fit into a
 * single flag
 */
public class CoordinateSystem {
    public static final int MAX_MAP_SIZE = 64;

    private final int originX;
    private final int originY;

    public CoordinateSystem(final MapLocation origin) {
        this.originX = origin.x;
        this.originY = origin.y;
    }

    public int[] toRelative(final MapLocation mapLocation) {
        return new int[]{mapLocation.x - originX + MAX_MAP_SIZE, mapLocation.y - originY + MAX_MAP_SIZE};
    }

    public MapLocation toAbsolute(final int x, final int y) {
        return new MapLocation(x + originX - MAX_MAP_SIZE, y + originY - MAX_MAP_SIZE);
    }
}
