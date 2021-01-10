package common;

import battlecode.common.*;

/**
 * Holds static common funcs to be used across all robots
 */
public class Pathfinding {

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

        //(x, y)
        //-+ NorthWest
        //++ NorthEast
        //-- SouthWest
        //+- SouthEast
        //=+ North
        //-= South
        //+= East
        //=- West

        if (dx < 0 && dy > 0 && robotController.canMove(Direction.NORTHWEST)) {
            return Direction.NORTHWEST;
        } else if ( dx > 0 && dy > 0 && robotController.canMove(Direction.NORTHEAST)) {
            return Direction.NORTHEAST;
        } else if ( dx < 0 && dy < 0 && robotController.canMove(Direction.SOUTHWEST)) {
            return Direction.SOUTHWEST;
        } else if ( dx > 0 && dy < 0 && robotController.canMove(Direction.SOUTHEAST)) {
            return Direction.SOUTHEAST;
        }

        if ( dy > 0 && robotController.canMove(Direction.NORTH)) {
            return Direction.NORTH;
        } else if (dy > 0 && robotController.canMove(Direction.NORTHEAST)){
            return Direction.NORTHEAST;
        } else if (dy > 0 && robotController.canMove(Direction.NORTHWEST)){
            return Direction.NORTHWEST;
        } else if ( robotController.canMove(Direction.EAST)) {
            return Direction.EAST;
        } else if ( robotController.canMove(Direction.WEST)) {
            return Direction.WEST;
        }

        if ( dy < 0 && robotController.canMove(Direction.SOUTH)) {
            return Direction.SOUTH;
        } else if (dy > 0 && robotController.canMove(Direction.SOUTHEAST)){
            return Direction.SOUTHEAST;
        } else if (dy > 0 && robotController.canMove(Direction.SOUTHWEST)){
            return Direction.SOUTHWEST;
        } else if ( robotController.canMove(Direction.EAST)) {
            return Direction.EAST;
        } else if ( robotController.canMove(Direction.WEST)) {
            return Direction.WEST;
        }

        if ( dx > 0 && robotController.canMove(Direction.EAST)) {
            return Direction.EAST;
        } else if (dy > 0 && robotController.canMove(Direction.SOUTHEAST)){
            return Direction.SOUTHEAST;
        } else if (dy > 0 && robotController.canMove(Direction.NORTHEAST)){
            return Direction.NORTHEAST;
        } else if ( robotController.canMove(Direction.NORTH)) {
            return Direction.NORTH;
        } else if ( robotController.canMove(Direction.SOUTH)) {
            return Direction.SOUTH;
        }

        if (dx < 0 && robotController.canMove(Direction.WEST)){
            return Direction.WEST;
        } else if (dy > 0 && robotController.canMove(Direction.SOUTHWEST)){
            return Direction.SOUTHWEST;
        } else if (dy > 0 && robotController.canMove(Direction.NORTHWEST)){
            return Direction.NORTHWEST;
        } else if ( robotController.canMove(Direction.NORTH)) {
            return Direction.NORTH;
        } else if ( robotController.canMove(Direction.SOUTH)) {
            return Direction.SOUTH;
        }
        return direction;
    }

    /**
     * Makes a single move and yields the clocks
     * @param destination where to move to
     * @param robotController robot controller
     * @throws GameActionException Should never be thrown, as {Pathfinding.findPath} checks
     * if move is valid before committing
     */
    public static void move(MapLocation destination, RobotController robotController) throws GameActionException {
        robotController.move(findPath(destination, robotController));
    }

    /**
     * idk another one that takes a dir instead of a map location
     * @param direction
     * @param robotController
     * @throws GameActionException
     */
    public static void move(Direction direction, RobotController robotController) throws GameActionException {
        if (robotController.canMove(direction)) {
            robotController.move(direction);
            //TODO Make sure removing this yield doesn't break anything
            Clock.yield();
        } else {
            direction = findPath(new MapLocation(direction.dx, direction.dy), robotController);
            robotController.move(direction);
            //TODO Make sure removing this yield doesn't break anything
            Clock.yield();
        }
    }

    /**
     * Move in direction if can
     * Does nothing if cannot move
     * 
     * @param direction Direction to move in
     * @param robotController controller for robot instance
     * @return true if robot has moved
     * @throws GameActionException
     */
    public static boolean tryMove(Direction direction, RobotController robotController) throws GameActionException {
        boolean hasMoved = false;
        if (robotController.canMove(direction)) {
            robotController.move(direction);
            hasMoved = true;
        }

        return (hasMoved);
    }
}
