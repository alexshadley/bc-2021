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
    // Number of adjacent tiles
    private final int NUM_ADJACENT = 8;

    // Min heap to get best next tile
    private PriorityQueue<Location> locations;

    // Robot controller
    private final RobotController robotController;

    /**
     * Constructor to initialize planner with surrounding tiles
     */
    public Planner( RobotController robotController ) {
        // Set robot controller
        this.robotController = robotController;

        // Initialize priority queue with surrounding tiles
        locations = new PriorityQueue<Location>( NUM_ADJACENT, new LocationComparator() );
    }

    /**
     * Try and get best next direction to get to final destination
     *
     * @return Best direction to move to reach goal
     * @throws GameActionException
     */
    public Direction getNextDirection( MapLocation destination ) throws GameActionException {
        // Default to CENTER
        Direction nextDirection = Direction.CENTER;

        // TODO: this is because Mukraker plans with null destination... need to fix that root cause I think
        if ( null == destination ) {
            return ( nextDirection );
        } 
        

        if ( locations.isEmpty() ) {
            // Add adjacent robot tiles to queue
            for ( Direction direction : Directions.directions ) {
                MapLocation adjLoc = robotController.adjacentLocation( direction );
                if ( robotController.onTheMap( adjLoc ) ) {
                    double passability = robotController.sensePassability( adjLoc );
                    double distance = adjLoc.distanceSquaredTo( destination );
                    locations.add( new Location( adjLoc, passability, distance ) );
                }
            }
        }

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

        if ( Direction.CENTER == direction ) {
            hasMoved = true;
        } else {
            hasMoved = Pathfinding.tryMove( direction, robotController );
        
            if ( hasMoved ) {
                locations.clear();
            }
        }

        return ( hasMoved );
    }
}
