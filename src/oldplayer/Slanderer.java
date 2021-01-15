package oldplayer;

import battlecode.common.*;
import common.Pathfinding;
import common.Directions;
import common.Robot;
import common.Logging;

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

    /**
     * Constructor for Slanderer
     * 
     * @param robotController controller for current slanderer
     */
    public Slanderer(final RobotController robotController, final RobotInfo parent) {
        this.robotController = robotController;
        this.parent = parent;
    }

    /**
     * Main execution loop
     **/
    public void run() throws GameActionException {
        while ( true ) {
            // if we've become a politician, switch to that code
            if (robotController.getType() == RobotType.POLITICIAN) {
                if ( Logging.LOGGING ) {
                    System.out.println("I've become a politican, transitioning");
                }
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
                System.out.println( robotController.getType() + " Exception: " + e.getMessage() );
                e.printStackTrace();
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
        boolean cannotMove = false;
        if ( null != enemyLocation ) {
            Direction movementDir = currLocation.directionTo( enemyLocation );
            cannotMove = Pathfinding.tryMove( movementDir.opposite(), robotController );
        }

        if ( null == enemyLocation || cannotMove ) {
            Pathfinding.tryMove( Directions.getRandomDirection(), robotController );
        }
    }
}
