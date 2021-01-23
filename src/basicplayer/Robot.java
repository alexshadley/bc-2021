package basicplayer;

import java.util.PriorityQueue;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

/**
 * Robot parent class
 */
public class Robot {
    // Robot controller
    protected final RobotController robotController;

    /**
     * Robot constructor
     *
     * @param robotController robot controller instance
     */
    public Robot( RobotController robotController ) {
        this.robotController = robotController;        
    }

    /**
     * Get adjacent locations
     *
     * @param locations Priority queue of locations
     */
    protected void getAdjLocations( PriorityQueue<Location> locations ) throws GameActionException {
        for ( Direction direction : Directions.directions ) {
            MapLocation adjLoc = robotController.adjacentLocation( direction );

            if ( robotController.onTheMap( adjLoc ) ) {
                double passability = robotController.sensePassability( adjLoc );

                locations.add( new Location( adjLoc, passability ) );
            }
        }
    }

    /**
     * Get adgacent locations with distance weight
     *
     * @param locations Priority queue of locations
     * @param destination Final destination
     */
    protected void getAdjLocations( PriorityQueue<Location> locations, MapLocation destination ) throws GameActionException {
        for ( Direction direction : Directions.directions ) {
            MapLocation adjLoc = robotController.adjacentLocation( direction );

            if ( robotController.onTheMap( adjLoc ) ) {
                double passability = robotController.sensePassability( adjLoc );
                double distance = adjLoc.distanceSquaredTo( destination );

                locations.add( new DestLocation( adjLoc, passability, distance ) );
            }
        }
    }
}
