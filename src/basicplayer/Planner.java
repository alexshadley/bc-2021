package basicplayer;

import java.util.PriorityQueue;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

/**
 * Planner class for slightly advanced pathfinding
 */
public class Planner {
    // Min heap to get best next tile
    private PriorityQueue<? extends Location> locations;

    // Robot instance
    private final Robot robot;

    // Robot controller
    private final RobotController robotController;

    /**
     * Constructor to initialize planner with surrounding tiles
     */
    public Planner( Robot robot ) {
        // Set robot instance
        this.robot = robot;

        // Set robot controller
        robotController = robot.robotController;

        // Initialize priority queue
        locations = new PriorityQueue<>();
    }

    public Direction getNextDirection() throws GameActionException {
        locations = robot.getAdjLocations();
        
        return ( findBestDirection() );
    }

    /**
     * Try and get best next direction to move by one in direction specified
     *
     * @param direction Direction to try to move towards
     * @return direction to move in
     * @throws GameActionException
     */
    public Direction getNextDirection( Direction direction ) throws GameActionException {
        MapLocation destination = robotController.getLocation().add( direction );
        Direction nextDirection = getNextDirection( destination );

        return ( nextDirection );
    }

    /**
     * Try and get best next direction to get to final destination
     *
     * @return Best direction to move to reach goal
     * @throws GameActionException
     */
    public Direction getNextDirection( MapLocation destination ) throws GameActionException {
        locations = robot.getAdjLocations( destination );

        return ( findBestDirection() );
    }

    private Direction findBestDirection() {
        // Default to CENTER
        Direction nextDirection = Direction.CENTER;

        // Loop through adjacent locations to find 
        while ( false == locations.isEmpty() ) {
            Location location = locations.poll();
            Direction direction = robotController.getLocation().directionTo( location.getLocation() );
            
            if ( robotController.canMove( direction ) ) {
                nextDirection = direction;
                break;
            }
        }

        return ( nextDirection );
    }

    /**
     * Try and move in specified direction
     *
     * @return true if moved to location
     * @throws GameActionException
     */
    public boolean move( Direction direction ) throws GameActionException {
        boolean hasMoved = false;

        if ( robotController.canMove( direction ) ) {
            robotController.move( direction );
            hasMoved = true;
        }

        return ( hasMoved );
    }
}
