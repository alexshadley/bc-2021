package robots;

import battlecode.common.*;

/**
 * Holds static common funcs to be used across all robots
 */
public class Common {

    /**
     * Returns the next direction a robot should move
     * Will attempt to move either NESW if one of the diagonals fail.
     * Prioritizes diagonal movements
     * @param destination The ultimate destination
     * @param robotController The robots controller. We use this instead of a location in case we want to weight the movements
     * @return the next step in the path
     */
    public static Direction findPath(MapLocation destination, RobotController robotController) {
        int dx = destination.x - robotController.getLocation().x;
        int dy = destination.y - robotController.getLocation().y;
        Direction direction = Direction.CENTER;

        //+- NorthWest
        //++ NorthEast
        //-- SouthWest
        //-+ SouthEast
        //+= North
        //=- South
        //=+ East
        //-= West

        if (dx > 0 && dy < 0 && robotController.canMove(Direction.NORTHWEST)) {
            direction = Direction.NORTHWEST;
        } else if ( dx > 0 && dy > 0 && robotController.canMove(Direction.NORTHEAST)) {
            direction = Direction.NORTHEAST;
        } else if ( dx < 0 && dy < 0 && robotController.canMove(Direction.SOUTHWEST)) {
            direction = Direction.SOUTHWEST;
        } else if ( dx < 0 && dy > 0 && robotController.canMove(Direction.SOUTHEAST)) {
            direction = Direction.SOUTHEAST;
        }
        if ( dy > 0 && robotController.canMove(Direction.NORTH)) {
            direction = Direction.NORTH;
        }
        if ( dy < 0 && robotController.canMove(Direction.SOUTH)) {
            direction = Direction.SOUTH;
        }
        if ( dx > 0 && robotController.canMove(Direction.EAST)) {
            direction = Direction.EAST;
        }
        if (dx < 0 && robotController.canMove(Direction.WEST)){
            direction = Direction.WEST;
        }

        return direction;
    }

    /**
     * Makes a single move and yields the clocks
     * @param destination where to move to
     * @param robotController robot controller
     * @throws GameActionException Should never be thrown, as {@link Common.findPath} checks
     * if move is valid before committing
     */
    public static void move(MapLocation destination, RobotController robotController) throws GameActionException {
        robotController.move(findPath(destination, robotController));
        Clock.yield();
    }
}
