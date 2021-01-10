package robotutils;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class EnlightenmentCenterUtils {
    public static RobotInfo buildRobot(RobotController rc, RobotType robotType, Direction dir, int influence)
        throws GameActionException {

        rc.buildRobot(robotType, dir, influence);
        final MapLocation ecLocation = rc.getLocation();
        return rc.senseRobotAtLocation(ecLocation.add(dir));
    }


}
