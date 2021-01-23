package basicplayer;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;

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
public class Slanderer extends Robot implements RobotInterface {
    // Parent spawner
    private final RobotInfo parent;

    // Radii
    private static final int ACTION_R2 = 0;
    private static final int SENSOR_R2 = 20;
    private static final int DETECT_R2 = 20;

    // Planner
    private Planner planner;

    // Enemy team
    private Team enemy;

    /**
     * Constructor for Slanderer
     * 
     * @param robotController controller for current slanderer
     * @param parent Robot parent
     */
    public Slanderer(final RobotController robotController, final RobotInfo parent) {
        super( robotController );
        this.parent = parent;

        planner = new Planner( this );
        enemy = robotController.getTeam().opponent();
    }

    /**
     * Main execution loop
     *
     * @throws GameActionException
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
                // Try and run from enemies and ECs
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
        planner.move( planner.getNextDirection() );
    }

    /**
     * Get adjacent locations
     *
     * @param locations Priority queue of locations
     */
    @Override
    protected void getAdjLocations( PriorityQueue<Location> locations ) throws GameActionException {
        List<MapLocation> enlightenmentCenters = new ArrayList<MapLocation>();
        List<MapLocation> enemies = new ArrayList<MapLocation>();

        for ( RobotInfo robot : robotController.senseNearbyRobots( SENSOR_R2 ) ) {
            if ( RobotType.ENLIGHTENMENT_CENTER == robot.getType() ) {
                enlightenmentCenters.add( robot.getLocation() );
            } else if ( enemy == robot.getTeam() ) {
                enemies.add( robot.getLocation() );
            }
        }
        
        for ( Direction direction : Directions.directions ) {
            MapLocation adjLoc = robotController.adjacentLocation( direction );

            if ( robotController.onTheMap( adjLoc ) ) {
                double passability = robotController.sensePassability( adjLoc );

                locations.add( new FleeLocation( adjLoc, passability, enemies, enlightenmentCenters ) );
            }
        }
    }
}
