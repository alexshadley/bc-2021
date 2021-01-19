package basicplayer;

import battlecode.common.*;

/**
 * Slanderers
 * 
 * Run from enemies
 * 
 * Stats:
 * Initial Conviction: Ceil(1.0C)
 * Base Action cool-down: 2.0
 * Action r^2: 0
 * Sensor r^2: 20
 * Detect r^2: 20
 */
public class Slanderer implements Robot {
    private final RobotController robotController;
    private final RobotInfo parent;

    private static final int ACTION_R2 = 0;
    private static final int SENSOR_R2 = 20;
    private static final int DETECT_R2 = 20;

    private Planner planner;

    private Direction runningDirection;

    private final int HIT_LIMIT = 50;
    private int hitCount;

    /**
     * Constructor for Slanderer
     * 
     * @param robotController controller for current slanderer
     */
    public Slanderer(final RobotController robotController, final RobotInfo parent) {
        this.robotController = robotController;
        this.parent = parent;

        planner = new Planner( robotController );
        runningDirection = parent.getLocation().directionTo( robotController.getLocation() );
        hitCount = 0;
    }

    /**
     * Main execution loop
     **/
    public void run() throws GameActionException {
        while ( true ) {
            // if we've become a politician, switch to that code
            if (robotController.getType() == RobotType.POLITICIAN) {
                Logging.info( "I've become a politican, transitioning" );
                final Politician politician = new Politician(robotController, parent);
                politician.run();
                return;
            }

            // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
            try {
                // Try and run from enemies
                flee();

                // Yield
                Clock.yield();
            } catch ( Exception e ) {
                Logging.error( robotController.getType() + " Exception: " + e.getMessage() );
            }
        }
    }

    /** 
     * Slanderer brain logic
     * If spot enemy - RUN!
     *
     * @throws GameActionException
     */
    private void flee() throws GameActionException {
        // Current location
        MapLocation currLocation = robotController.getLocation();

        // Enemy and detection radius
        Team enemy = robotController.getTeam().opponent();
        
        // Minimum distance and location for closest enemy
        double minDistance = Double.POSITIVE_INFINITY;
        MapLocation enemyLocation = null;

        // Find closest enemy
        for ( RobotInfo robot : robotController.senseNearbyRobots( SENSOR_R2, enemy ) ) {
            // Is this the closest enemy?
            int distance = currLocation.distanceSquaredTo( robot.getLocation() );
            if ( distance < minDistance ) {
                minDistance = distance;
                enemyLocation = robot.getLocation();
            }
        }

        // Try to move in opposite direction
        if ( null != enemyLocation ) {
            Direction movementDir = currLocation.directionTo( enemyLocation );
            runningDirection = movementDir.opposite();
            hitCount = 0;
        } else {
            hitCount++;
        }

        if ( hitCount > HIT_LIMIT ) {
            runningDirection = getFleeDirection();
            hitCount = 0;
        }
        
        planner.move( runningDirection );
    }

    /**
     * Get best flee direction
     *
     * @return Direction to flee
     * @throws GameActionException
     */
    private Direction getFleeDirection() throws GameActionException {
        // Loop through adjacent tiles and find best direction to move in
        Direction bestDirection = Direction.CENTER;
        double minCost = 1.0 / 0.1;

        for ( Direction dir : Directions.directions ) {
            MapLocation adjLoc = robotController.adjacentLocation( dir );
            if ( robotController.onTheMap( adjLoc ) ) {
                double cost = 1.0 / robotController.sensePassability( adjLoc );
                if ( cost < minCost ) {
                    minCost = cost;
                    bestDirection = dir;
                }
            }
        }

        return ( bestDirection );
    }
}
