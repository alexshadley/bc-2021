package common;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

/**
 * Common code for robot startup
 */
public class Startup {
    /**
     * Finds the location of the spawning EC by checking neighbors (used on robot startup)
     * @return
     */
    public static RobotInfo getParent(final RobotController rc) throws GameActionException {
        final MapLocation myLocation = rc.getLocation();
        for (final Direction direction : Direction.allDirections()) {
            final RobotInfo neighbor = rc.senseRobotAtLocation(myLocation.add(direction));
            if (neighbor != null && neighbor.type == RobotType.ENLIGHTENMENT_CENTER) {
                return neighbor;
            }
        }

        // should never be reached
        return null;
    }
}
