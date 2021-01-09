package common;

import battlecode.common.Direction;

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
}
