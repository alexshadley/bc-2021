package basicplayer;

import battlecode.common.*;
import common.Pathfinding;

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
public class Slanderer {
    private final RobotController robotController;

    private static final int ACTION_R2 = 0;
    private static final int SENSOR_R2 = 20;
    private static final int DETECT_R2 = 20;

    /**
     * Constructor for Slanderer
     * 
     * @param robotController controller for current slanderer
     */
    public Slanderer( RobotController robotController ) {
        this.robotController = robotController;
    }

    /**
     * Main execution loop
     **/
    public void run() throws GameActionException {
        while ( true ) {
            // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
            try {
                // Try and run from enemies
                flee();

                // Yield
                Clock.yield();
            } catch ( Exception e ) {
                System.out.println( robotController.getType() + " Exception: " + e.getMessage() );
                e.printStackTrace();
            }
        }
    }

    /** 
     * Slanderer brain logic
     * If spot enemy - RUN!
     *
     * @throws GaneActionException
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
        // TODO: limit this.. if too many robots this will be quite the byte code count
        for ( RobotInfo robot : robotController.senseNearbyRobots( SENSOR_R2, enemy ) ) {
            // Should I be worried about this enemy?
            if ( RobotType.POLITICIAN == robot.type ||
                 RobotType.MUCKRAKER == robot.type ) {
                // Is this the closest enemy?
                int distance = currLocation.distanceSquaredTo( robot.getLocation() );
                if ( distance < minDistance ) {
                    minDistance = distance;
                    enemyLocation = robot.getLocation();
                }
            }
        }

        // Try to move in opposite direction
        if ( null != enemyLocation ) {
            Direction movementDir = currLocation.directionTo( enemyLocation );
            Pathfinding.tryMove( movementDir.opposite(), robotController );
        }
    }
}
