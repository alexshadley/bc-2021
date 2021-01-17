package basicplayer;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import basicplayer.Flags.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

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

    private static class TypeAndInfluenceFunction {
        public final RobotType robotType;
        public final Function<Integer, Integer> influenceFunction;

        public TypeAndInfluenceFunction(final RobotType robotType, final Function<Integer, Integer> influenceFunction) {
            this.robotType = robotType;
            this.influenceFunction = influenceFunction;
        }

        public TypeAndInfluence toTypeAndInfluence(final int ecInfluence) {
            return new TypeAndInfluence(
                robotType,
                influenceFunction.apply(ecInfluence)
            );
        }
    }

    private static class RobotTypeDecider {
        private final List<TypeAndInfluenceFunction> options;
        private final List<Integer> frequencies;

        private int currentOption = 0;
        private int unitsOfOptionMade = 0;

        public RobotTypeDecider(final List<TypeAndInfluenceFunction> options, final List<Integer> frequencies) {
            this.options = options;
            this.frequencies = frequencies;
        }

        public TypeAndInfluence next(final int ecInfluence) {
            if (unitsOfOptionMade < frequencies.get(currentOption)) {
                unitsOfOptionMade++;
                return options.get(currentOption).toTypeAndInfluence(ecInfluence);
            } else {
                currentOption = (currentOption + 1) % options.size();
                unitsOfOptionMade = 0;
                return next(ecInfluence);
            }
        }
    }

    private enum ECMode {
        SCOUTING,
        BUILDING,
        RUSHING
    }

    private static final List<TypeAndInfluenceFunction> scoutingOptions = Arrays.asList(
        new TypeAndInfluenceFunction(RobotType.POLITICIAN, i -> Politician.GUARD_POLITICAN_SIZE),
        new TypeAndInfluenceFunction(RobotType.SLANDERER, i -> 40),
        new TypeAndInfluenceFunction(RobotType.MUCKRAKER, i -> 1)
    );
    private static final List<Integer> scoutingFrequencies = Arrays.asList(1, 2, 5);

    private static final List<TypeAndInfluenceFunction> buildingOptions = Arrays.asList(
        new TypeAndInfluenceFunction(RobotType.POLITICIAN, i -> Politician.GUARD_POLITICAN_SIZE),
        new TypeAndInfluenceFunction(RobotType.POLITICIAN, i -> i/4),
        new TypeAndInfluenceFunction(RobotType.SLANDERER, i -> 85),
        new TypeAndInfluenceFunction(RobotType.MUCKRAKER, i -> 1)
    );
    private static final List<Integer> buildingFrequencies = Arrays.asList(2, 1, 2, 5);

    private static final List<TypeAndInfluenceFunction> rushingOptions = Arrays.asList(
        new TypeAndInfluenceFunction(RobotType.POLITICIAN, i -> i/4),
        new TypeAndInfluenceFunction(RobotType.SLANDERER, i -> 85),
        new TypeAndInfluenceFunction(RobotType.MUCKRAKER, i -> 1)
    );
    private static final List<Integer> rushingFrequencies = Arrays.asList(3, 1, 1);

    private static final Map<ECMode, RobotTypeDecider> typeDeciders = new HashMap() {{
        put(ECMode.SCOUTING, new RobotTypeDecider(scoutingOptions, scoutingFrequencies));
        put(ECMode.BUILDING, new RobotTypeDecider(buildingOptions, buildingFrequencies));
        put(ECMode.RUSHING, new RobotTypeDecider(rushingOptions, rushingFrequencies));
    }};

    private static final int MAGIC_RUSH_TURN = 600;

    private static final TypeAndInfluence[] startupSequence = new TypeAndInfluence[]{
        new TypeAndInfluence(RobotType.SLANDERER, 130),
        new TypeAndInfluence(RobotType.MUCKRAKER, 1),
        new TypeAndInfluence(RobotType.MUCKRAKER, 1),
        new TypeAndInfluence(RobotType.MUCKRAKER, 1),
        new TypeAndInfluence(RobotType.MUCKRAKER, 1),
        new TypeAndInfluence(RobotType.MUCKRAKER, 1),
        new TypeAndInfluence(RobotType.MUCKRAKER, 1),
        new TypeAndInfluence(RobotType.MUCKRAKER, 1),
        new TypeAndInfluence(RobotType.MUCKRAKER, 1),
        new TypeAndInfluence(RobotType.POLITICIAN, Politician.GUARD_POLITICAN_SIZE),
        new TypeAndInfluence(RobotType.SLANDERER, 40),
        new TypeAndInfluence(RobotType.SLANDERER, 40)
    };

    private static final Bidder constantBidder = new ConstantBidder(1);
    private static final Bidder adaptiveBidder = new LinearBidder(5, 5, 50);

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

    // has a value if we want to make something but don't have influence yet. Otherwise null
    private TypeAndInfluence nextToBuild;

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

            buildRobot();

            if (rc.getInfluence() >= 200) {
                bidderRunner.attemptBid(rc, adaptiveBidder);
            } else {
                bidderRunner.attemptBid(rc, constantBidder);
            }
            Clock.yield();
        }
    }

    private void buildRobot() throws GameActionException {
        final TypeAndInfluence next;
        if (nextToBuild == null) {
            next = getRobotToBuild(robotCount, rc.getInfluence());
            if ( Logging.LOGGING ) {
                System.out.println("Will build " + next.robotType + " at " + next.influence);
            }
        } else {
            next = nextToBuild;
        }

        boolean unitBuilt = false;
        for (final Direction dir : Direction.allDirections()) {
            if (rc.canBuildRobot(next.robotType, dir, next.influence)) {
                final int robotId = EnlightenmentCenterUtils.buildRobot(rc, next.robotType, dir, next.influence).ID;
                myRobots[robotCount] = robotId;
                robotCount++;
                if (next.robotType == RobotType.MUCKRAKER) {
                    scouts.add(robotId);
                }
                unitBuilt = true;
                break;
            }
        }

        if (unitBuilt) {
            nextToBuild = null;
        } else {
            nextToBuild = next;
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

    private TypeAndInfluence getRobotToBuild(final int robotCount, final int ecInfluence) {
        if (robotCount < startupSequence.length) {
            return startupSequence[robotCount];
        } else {
            return typeDeciders.get(mode).next(ecInfluence);
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
