package basicplayer;

import java.util.PriorityQueue;

import battlecode.common.RobotController;
import battlecode.common.MapLocation;
import battlecode.common.Direction;
import battlecode.common.GameActionException;

public class Robot {
    protected final int NUM_ADJACENT = 8;

    protected final RobotController robotController;

    public Robot( RobotController robotController ) {
        this.robotController = robotController;        
    }

    protected PriorityQueue<? extends Location> getAdjLocations() throws GameActionException {
        PriorityQueue<Location> locations = new PriorityQueue<Location>( NUM_ADJACENT, new LocationComparator() );

        for ( Direction direction : Directions.directions ) {
            MapLocation adjLoc = robotController.adjacentLocation( direction );

            if ( robotController.onTheMap( adjLoc ) ) {
                double passability = robotController.sensePassability( adjLoc );

                locations.add( new Location( adjLoc, passability ) );
            }
        }

        return ( locations );
    }

    protected PriorityQueue<? extends Location> getAdjLocations( MapLocation destination ) throws GameActionException {
        // Add adjacent robot tiles to queue
        PriorityQueue<DestLocation> locations = new PriorityQueue<DestLocation>( NUM_ADJACENT, new LocationComparator() );

        for ( Direction direction : Directions.directions ) {
            MapLocation adjLoc = robotController.adjacentLocation( direction );

            if ( robotController.onTheMap( adjLoc ) ) {
                double passability = robotController.sensePassability( adjLoc );
                double distance = adjLoc.distanceSquaredTo( destination );

                locations.add( new DestLocation( adjLoc, passability, distance ) );
            }
        }

        return ( locations );
    }
}
