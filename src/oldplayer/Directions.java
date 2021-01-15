package oldplayer;

import battlecode.common.Direction;
import battlecode.common.MapLocation;

/**
 * Class to handle direction helper functions
 */
public class Directions {
    // List of directions
    public static final Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    };

    /**
     * Returns a random Direction.
     *
     * @return a random Direction
     */
    public static Direction getRandomDirection() {
        return directions[(int) ( Math.random() * directions.length )]; 
    }

    /**
     * Returns the relative distance to a location
     * @param to where are we measuring to
     * @param from where are we measuring from
     * @return squared distance
     */
    public static int distanceTo(MapLocation to, MapLocation from) {
        int dx = Math.abs(to.x - from.x);
        int dy = Math.abs(to.y - from.y);
        return (int) (Math.pow(dx, 2) + (Math.pow(dy, 2)));
    }
}
