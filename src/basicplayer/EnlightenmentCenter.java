package basicplayer;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import common.Directions;
import common.Flags.Type;

public class EnlightenmentCenter {
    private final RobotController rc;

    static final RobotType[] spawnableRobot = {
        RobotType.POLITICIAN,
        RobotType.SLANDERER,
        RobotType.MUCKRAKER,
    };

    private static class TypeAndInfluence {
        public final RobotType robotType;
        public final int influence;

        public TypeAndInfluence(final RobotType robotType, final int influence) {
            this.robotType = robotType;
            this.influence = influence;
        }
    }

    private static final TypeAndInfluence[] startupSequence = new TypeAndInfluence[] {
        new TypeAndInfluence(RobotType.SLANDERER, 100),
        new TypeAndInfluence(RobotType.MUCKRAKER, 1),
        new TypeAndInfluence(RobotType.MUCKRAKER, 1),
        new TypeAndInfluence(RobotType.MUCKRAKER, 1),
        new TypeAndInfluence(RobotType.MUCKRAKER, 1)
    };

    private int[] myRobots = new int[1000];
    private int robotCount = 0;

    public EnlightenmentCenter(final RobotController rc) {
        this.rc = rc;
    }

    public void run() throws GameActionException {
        while (true) {
            final TypeAndInfluence next = getRobotToBuild(robotCount);

            for (final Direction dir : Directions.directions) {
                if (rc.canBuildRobot(next.robotType, dir, next.influence)) {
                    myRobots[robotCount] = buildRobot(next.robotType, dir, next.influence).ID;
                    robotCount++;
                    System.out.println("robot " + robotCount + " created: " + myRobots[robotCount - 1]);
                }
            }

            Clock.yield();
        }
    }

    public TypeAndInfluence getRobotToBuild(int robotCount) {
        if (robotCount < startupSequence.length) {
            return startupSequence[robotCount];
        } else {
            return new TypeAndInfluence(randomSpawnableRobotType(), 50);
        }
    }

    private RobotInfo buildRobot(RobotType robotType, Direction dir, int influence)
        throws GameActionException {

        rc.buildRobot(robotType, dir, influence);
        final MapLocation ecLocation = rc.getLocation();
        return rc.senseRobotAtLocation(ecLocation.add(dir));
    }

    /**
     * Returns a random spawnable RobotType
     *
     * @return a random RobotType
     */
    static RobotType randomSpawnableRobotType() {
        return spawnableRobot[(int) (Math.random() * spawnableRobot.length)];
    }
}
