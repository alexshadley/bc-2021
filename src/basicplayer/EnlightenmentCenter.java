package basicplayer;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import common.CoordinateSystem;
import common.Flags;
import common.Flags.Type;

public class EnlightenmentCenter {
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

    private final RobotController rc;
    private final CoordinateSystem coordinateSystem;


    private int[] myRobots = new int[1000];
    private int robotCount = 0;

    private int[] myScouts = new int[100];
    private int scoutCount = 0;

    private MapLocation[] enemyECs = new MapLocation[3];
    private int enemyECCount = 0;

    public EnlightenmentCenter(final RobotController rc) {
        this.rc = rc;
        this.coordinateSystem = new CoordinateSystem(rc.getLocation());
    }

    public void run() throws GameActionException {
        while (true) {
            final TypeAndInfluence next = getRobotToBuild(robotCount);

            for (final Direction dir : Direction.allDirections()) {
                if (rc.canBuildRobot(next.robotType, dir, next.influence)) {
                    final int robotId = buildRobot(next.robotType, dir, next.influence).ID;
                    myRobots[robotCount] = robotId;
                    robotCount++;
                    if (next.robotType == RobotType.MUCKRAKER) {
                        myScouts[scoutCount] = robotId;
                        scoutCount++;
                    }
                    System.out.println("robot " + robotCount + " created: " + myRobots[robotCount - 1]);
                }
            }

            checkCommunications();

            Clock.yield();
        }
    }

    private void checkCommunications() {
        for (int i = 0; i < scoutCount; i++) {
            try {
                final int flag = rc.getFlag(myScouts[i]);

                if (Flags.getFlagType(flag) == Type.ENEMY_EC_FOUND) {
                    final int[] coords = Flags.getEnemyECFoundInfo(flag);
                    addEnemyEC(coordinateSystem.toAbsolute(coords[0], coords[1]));
                }

            } catch (final GameActionException e) {
                System.out.println("Couldn't get scout flag: " + e);
            }
        }
    }

    /**
     * Add enemy ec, deduping if this has already been found
     */
    private void addEnemyEC(final MapLocation enemyECLocation) {
        for (int i = 0; i < enemyECCount; i++) {
            if (enemyECLocation.equals(enemyECs[i])) {
                return;
            }
        }

        System.out.println("New enemy EC found: " + enemyECLocation);

        enemyECs[enemyECCount] = enemyECLocation;
        enemyECCount++;
    }

    private TypeAndInfluence getRobotToBuild(int robotCount) {
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
