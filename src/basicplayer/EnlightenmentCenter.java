package basicplayer;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import basicplayer.Flags.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EnlightenmentCenter implements Robot {
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

    private static class RobotTypeDecider {
        public final int politicianFrequency;
        public final int slandererFrequency;
        public final int muckrackerFrequency;

        private int next = 0;

        public RobotTypeDecider(final int politicianFrequency,
                                final int slandererFrequency,
                                final int muckrackerFrequency) {

            this.politicianFrequency = politicianFrequency;
            this.slandererFrequency = slandererFrequency;
            this.muckrackerFrequency = muckrackerFrequency;
        }

        public RobotType next() {
            if (next < politicianFrequency) {
                next++;
                return RobotType.POLITICIAN;
            } else if (next < politicianFrequency + slandererFrequency) {
                next++;
                return RobotType.SLANDERER;
            } else if (next < politicianFrequency + slandererFrequency + muckrackerFrequency) {
                next++;
                return RobotType.MUCKRAKER;
            } else {
                next = 0;
                return next();
            }
        }
    }

    private enum ECMode {
        SCOUTING,
        BUILDING,
        RUSHING
    }

    private static final Map<ECMode, RobotTypeDecider> typeDeciders = new HashMap() {{
        put(ECMode.SCOUTING, new RobotTypeDecider(1, 2, 2));
        put(ECMode.BUILDING, new RobotTypeDecider(2, 4, 1));
        put(ECMode.RUSHING, new RobotTypeDecider(2, 1, 1));
    }};

    private static final int MAGIC_RUSH_TURN = 800;

    private static final TypeAndInfluence[] startupSequence = new TypeAndInfluence[]{
        new TypeAndInfluence(RobotType.SLANDERER, 146),
        new TypeAndInfluence(RobotType.MUCKRAKER, 1),
        new TypeAndInfluence(RobotType.MUCKRAKER, 1),
        new TypeAndInfluence(RobotType.MUCKRAKER, 1),
        new TypeAndInfluence(RobotType.MUCKRAKER, 1)
    };

    private static final Bidder bidder = new ConstantBidder(1);

    private final RobotController rc;
    private final CoordinateSystem coordinateSystem;
    private final BidderRunner bidderRunner;
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
        this.bidderRunner = new BidderRunner(rc.getTeamVotes());
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
                    final int robotId = EnlightenmentCenterUtils.buildRobot(rc, next.robotType, dir, next.influence).ID;
                    myRobots[robotCount] = robotId;
                    robotCount++;
                    if (next.robotType == RobotType.MUCKRAKER) {
                        scouts.add(robotId);
                    }
                }
            }

            bidderRunner.attemptBid(rc, bidder);
            Clock.yield();
        }
    }

    private void initiateRush() throws GameActionException {
        if (enemyECCount == 0) {
            if ( Logging.LOGGING ) {
                System.out.println("Failed to rush, no enemy ECs known");
            }
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

                    // start building up forces
                    if (mode == ECMode.SCOUTING) {
                        mode = ECMode.BUILDING;
                    }
                }

            } catch (final GameActionException e) {
                // TODO: should we really be catching an exception here?
                if ( Logging.LOGGING ) {
                    System.out.println("Couldn't get scout flag, removing id: " + e);
                }
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

        if ( Logging.LOGGING ) {
            System.out.println("New enemy EC found: " + enemyECLocation);
        }

        // TODO: this is limited to 3 ECs
        if ( enemyECCount < 3 ) {
            enemyECs[enemyECCount] = enemyECLocation;
            enemyECCount++;
        }
    }

    private TypeAndInfluence getRobotToBuild(int robotCount) {
        if (robotCount < startupSequence.length) {
            return startupSequence[robotCount];
        } else {
            final RobotType type = typeDeciders.get(mode).next();
            final int influence = type == RobotType.MUCKRAKER
                ? 1
                : Math.max(50, rc.getInfluence() / 4);

            return new TypeAndInfluence(
                type,
                influence
            );
        }
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
