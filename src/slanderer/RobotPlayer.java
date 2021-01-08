package examplefuncsplayer;
import battlecode.common.*;

public strictfp class RobotPlayer {
    static RobotController rc;

    /**
     * Method run when robot spwans in world
     **/
    @SuppressWarnings( "unused" )
    public static void run( RobotController rc ) throws GameActionException {

        // Instance of robot for control actions
        RobotPlayer.rc = rc;

        while ( true ) {
            // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
            try {
                // Who am I?
                switch ( rc.getType() ) {
                    case SLANDERER:     runSlanderer();     break;
                    default:            doNothing();        break;
                }

                // Makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch ( Exception e ) {
                System.out.println( rc.getType() + " Exception: " + e.getMessage() );
                e.printStackTrace();
            }
        }
    }

    // Do nothing method
    static void doNothing() throws GameActionException {
        return;
    }

    /** 
     * Slanderer brain logic
     * If spot enemy... RUN! oh baby lord
     *
     * @throws GaneActionException
     */
    static void runSlanderer() throws GameActionException {
        // Current location
        MapLocation currLocation = rc.getLocation();

        // Enemy and detection radius
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        
        // Minimum distance and location for closest enemy
        double minDistance = Double.POSITIVE_INFINITY;
        MapLocation enemyLocation = null;

        // Find closest enemy
        for ( RobotInfo robot : rc.senseNearbyRobots( actionRadius, enemy ) ) {
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
        MapLocation invEnemyLocation = new MapLocation( enemyLocation.x * -1, enemyLocation.y * -1 );
        Direction movementDir = currLocation.directionTo( invEnemyLocation );
        tryMove( movementDir );
    }

    /**
     * Attempts to move in a given direction.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove( Direction dir ) throws GameActionException {
        if ( rc.canMove( dir ) ) {
            rc.move( dir );
            return true;
        } else {
            return false;
        }
    }
}
