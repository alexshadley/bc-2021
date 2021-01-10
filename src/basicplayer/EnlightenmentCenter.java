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
import java.util.HashSet;
import java.util.Set;

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

    private static enum ECMode {
        SCOUTING,
        RUSHING
    }

    private static final int MAGIC_RUSH_TURN = 1500;

    private static final TypeAndInfluence[] startupSequence = new TypeAndInfluence[] {
        new TypeAndInfluence(RobotType.SLANDERER, 100),
        new TypeAndInfluence(RobotType.MUCKRAKER, 1),
        new TypeAndInfluence(RobotType.MUCKRAKER, 1),
        new TypeAndInfluence(RobotType.MUCKRAKER, 1),
        new TypeAndInfluence(RobotType.MUCKRAKER, 1)
    };

    private final RobotController rc;
    private final CoordinateSystem coordinateSystem;
    private ECMode mode;

    private int[] myRobots = new int[1000];
    private int robotCount = 0;

    // TODO (alex): replace this with something more bytecode efficient if necessary
    private final Set<Integer> scouts = new HashSet<>();

    private MapLocation[] enemyECs = new MapLocation[3];
    private int enemyECCount = 0;

    public EnlightenmentCenter(final RobotController rc) {
        this.rc = rc;
        this.coordinateSystem = new CoordinateSystem(rc.getLocation());
        this.mode = ECMode.SCOUTING;
    }

    public void run() throws GameActionException {
        while (true) {
            // check comms for things like scouting
            checkCommunications();

            // start rush if we're past rush turn and not yet rushing
            if (rc.getRoundNum() >= MAGIC_RUSH_TURN && mode != ECMode.RUSHING) {
                initiateRush();
            }

            final TypeAndInfluence next = getRobotToBuild(robotCount);

            for (final Direction dir : Direction.allDirections()) {
                if (rc.canBuildRobot(next.robotType, dir, next.influence)) {
                    final int robotId = buildRobot(next.robotType, dir, next.influence).ID;
                    myRobots[robotCount] = robotId;
                    robotCount++;
                    if (next.robotType == RobotType.MUCKRAKER) {
                        scouts.add(robotId);
                    }
                }
            }

            Clock.yield();
        }
    }

    private void initiateRush() throws GameActionException {
        if (enemyECCount == 0) {
            System.out.println("Failed to rush, no enemy ECs known");
            return;
        }

        final int[] attackCoords = coordinateSystem.toRelative(enemyECs[0]);
        rc.setFlag(Flags.encodeAttackEnemyECFlag(attackCoords[0], attackCoords[1]));
        mode = ECMode.RUSHING;
    }

    private void checkCommunications() {
        final Set<Integer> deadScouts = new HashSet<>();
        for (final int id : scouts) {
            try {
                final int flag = rc.getFlag(id);

                if (Flags.getFlagType(flag) == Type.ENEMY_EC_FOUND) {
                    final int[] coords = Flags.getEnemyECFoundInfo(flag);
                    addEnemyEC(coordinateSystem.toAbsolute(coords[0], coords[1]));
                }

            } catch (final GameActionException e) {
                System.out.println("Couldn't get scout flag, removing id: " + e);
                deadScouts.add(id);
            }
        }

        scouts.removeAll(deadScouts);
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
