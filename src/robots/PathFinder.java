package robots;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

/**
 * Figured it might be nice to have this in its own class
 * Maybe just have this hold all of our shared funcs
 */
public class PathFinder {

    /**
     * Returns the next direction a robot should move
     * @param destination The ultimate destination
     * @param source The robots controller. We use this instead of a location in case we want to weight the movements
     * @return the next step in the path
     */
    public static Direction findPath(MapLocation destination, RobotController source) {
        int dx = destination.x - source.getLocation().x;
        int dy = destination.y - source.getLocation().y;
        Direction direction = null;

        //+- NorthWest
        //++ NorthEast
        //-- SouthWest
        //-+ SouthEast
        //+= North
        //=- South
        //=+ East
        //-= West

        if (dx > 0 && dy < 0) {
            direction = Direction.NORTHWEST;
        } else if ( dx > 0 && dy > 0) {
            direction = Direction.NORTHEAST;
        } else if ( dx < 0 && dy < 0) {
            direction = Direction.SOUTHWEST;
        } else if ( dx < 0 && dy > 0) {
            direction = Direction.SOUTHEAST;
        } else if ( dy > 0) {
            direction = Direction.NORTH;
        } else if ( dy < 0) {
            direction = Direction.SOUTH;
        } else if ( dx > 0) {
            direction = Direction.EAST;
        } else {
            direction = Direction.WEST;
        }

        return direction;
    }
}
