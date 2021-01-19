package basicplayer;

import basicplayer.Flags.Type;
import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;
import java.util.Arrays;

public class Politician implements Robot {
    public static final int GUARD_POLITICAN_SIZE = 30;

    private static final int ACTION_R2 = RobotType.POLITICIAN.actionRadiusSquared;
    private static final int SENSOR_R2 = RobotType.POLITICIAN.sensorRadiusSquared;
    private static final int DETECT_R2 = RobotType.POLITICIAN.detectionRadiusSquared;

    private enum PoliticanMode {
        ROAMING,
        RUSHING
    }

    private enum PoliticanType {
        GENERAL,
        GUARD
    }

    private final RobotController rc;
    private final CoordinateSystem coordinateSystem;
    private final RobotInfo parent;
    private final PoliticanType type;
    private PoliticanMode mode;
    // only valid for RUSHING mode, indicates the coords of the base to rush
    private MapLocation rushCoords;

    private Planner planner;

    private final Team enemy;

    // we wait some turns before attacking
    private static final int TIME_TO_WAIT = 1;
    private int timeWaited = 0;

    public Politician(final RobotController rc, final RobotInfo parent) {
        this.rc = rc;
        this.parent = parent;

        if (null == parent) {
            this.coordinateSystem = null;
        } else {
            this.coordinateSystem = new CoordinateSystem(parent.location);
        }

        this.mode = PoliticanMode.ROAMING;
        this.enemy = rc.getTeam().opponent();

        this.type = rc.getInfluence() == GUARD_POLITICAN_SIZE
            ? PoliticanType.GUARD
            : PoliticanType.GENERAL;

        planner = new Planner(rc);
    }

    /**
     * Main execution loop
     **/
    public void run() throws GameActionException {
        while (true) {
            if (null != parent) {
                checkCommunications();
            }

            if (type == PoliticanType.GENERAL) {
                if (mode == PoliticanMode.ROAMING) {
                    attackIfPossible();
                    planner.move(Directions.getRandomDirection());
                } else {
                    rushAttack();

                    // stop attacking (and tell our EC we won) if we find a friendly EC at the expected coords
                    if (checkIfECWon()) {
                        Logging.log("EC won!");
                        rc.setFlag(Flags.encodeECTakenFlag());
                        mode = PoliticanMode.ROAMING;
                    }

                    planner.move(planner.getNextDirection(rushCoords));
                }
            } else {
                guard();
            }

            Clock.yield();
        }
    }

    private boolean checkIfECWon() {
        final RobotInfo[] friendly = rc.senseNearbyRobots(SENSOR_R2, enemy.opponent());

        return Arrays.stream(friendly).anyMatch(robotInfo ->
            robotInfo.type == RobotType.ENLIGHTENMENT_CENTER && robotInfo.location.equals(rushCoords));
    }

    private void guard() throws GameActionException {
        // can't take any actions if cooldown too high
        if (rc.getCooldownTurns() >= 1) {
            return;
        }
        final RobotInfo furthestMuck = furthestEnemyMuckrakerInRange();

        // if we don't see any enemy muckrakers, just amble about
        if (furthestMuck == null) {
            // see if there's a muckraker in sensors range at all
            final RobotInfo anyMuck = anyMuckraker();
            if (anyMuck != null) {
                if (Logging.LOGGING) {
                    System.out.println("Found muckraker out of empower range, moving in");
                }
                planner.move(planner.getNextDirection(anyMuck.location));
                return;
            }

            if (Logging.LOGGING) {
                System.out.println("No nearby muckraker");
            }
            planner.move(Directions.getRandomDirection());
            return;
        }

        final int furthestMuckDistSquared = rc.getLocation().distanceSquaredTo(furthestMuck.location);

        // see if we can attack the muckraker effectively
        final int blastRadiusAllies = rc.senseNearbyRobots(furthestMuckDistSquared, enemy.opponent()).length;
        if (blastRadiusAllies <= 3 && timeWaited >= TIME_TO_WAIT) {
            if (Logging.LOGGING) {
                System.out.println("Able to attack enemy muckraker");
            }
            rc.empower(furthestMuckDistSquared);
            return;
        } else {
            timeWaited++;
        }

        // if we couldn't attack muckraker, get closer
        if (Logging.LOGGING) {
            System.out.println("Moving in on enemy muckraker");
        }
        planner.move(planner.getNextDirection(furthestMuck.location));
    }

    private RobotInfo anyMuckraker() {
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(
            RobotType.POLITICIAN.sensorRadiusSquared,
            enemy);

        return Arrays.stream(enemyRobots)
            .filter(robot -> robot.type == RobotType.MUCKRAKER)
            .findAny().orElse(null);
    }

    private RobotInfo furthestEnemyMuckrakerInRange() {
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(
            RobotType.POLITICIAN.sensorRadiusSquared,
            enemy);

        final MapLocation myLocation = rc.getLocation();
        RobotInfo[] enemyMuckrakers = Arrays.stream(enemyRobots)
            .filter(robot -> robot.type == RobotType.MUCKRAKER)
            .filter(robot ->
                robot.location.isWithinDistanceSquared(myLocation, RobotType.POLITICIAN.actionRadiusSquared))
            .toArray(RobotInfo[]::new);

        if (enemyMuckrakers.length == 0) {
            return null;
        }

        RobotInfo furthest = enemyMuckrakers[0];
        int distanceSquared = myLocation.distanceSquaredTo(furthest.location);
        for (final RobotInfo robot : enemyMuckrakers) {
            if (myLocation.distanceSquaredTo(robot.location) > distanceSquared) {
                furthest = robot;
                distanceSquared = myLocation.distanceSquaredTo(robot.location);
            }
        }

        return furthest;
    }

    private void checkCommunications() throws GameActionException {
        // don't check while rushing to save bytecode
        if (mode != PoliticanMode.RUSHING) {
            if (rc.canGetFlag(parent.ID)) {
                final int parentFlag = rc.getFlag(parent.ID);
                if (Flags.getFlagType(parentFlag) == Type.ATTACK_ENEMY_EC) {
                    if (Logging.LOGGING) {
                        System.out.println("Recieved attack orders from EC");
                    }
                    this.mode = PoliticanMode.RUSHING;

                    final int[] coords = Flags.getAttackEnemyECInfo(parentFlag);
                    if (Logging.LOGGING) {
                        System.out.println("X: " + coords[0]);
                        System.out.println("Y: " + coords[1]);
                    }
                    this.rushCoords = coordinateSystem.toAbsolute(coords[0], coords[1]);
                }
            }
        }
    }

    private void attackIfPossible() throws GameActionException {
        int actionRadius = rc.getType().actionRadiusSquared;
        final RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
        if (attackable.length != 0) {
            attemptAttack(actionRadius);
            return;
        }

        final RobotInfo[] neutral = rc.senseNearbyRobots(actionRadius, Team.NEUTRAL);
        if (neutral.length != 0) {
            attemptAttack(actionRadius);
        }
    }

    /**
     * Try to avoid attacking units and focus on ECs
     */
    private void rushAttack() throws GameActionException {
        final int actionRadius = rc.getType().actionRadiusSquared;

        // the only neutral units are ECs, so explode if we find one
        final RobotInfo[] neutral = rc.senseNearbyRobots(actionRadius, Team.NEUTRAL);
        if (neutral.length > 0) {
            attemptAttack(actionRadius);
            return;
        }

        final RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
        for (final RobotInfo target : attackable) {
            if (target.type == RobotType.ENLIGHTENMENT_CENTER) {
                if (Logging.LOGGING) {
                    System.out.println("Enemy EC found, attacking");
                }
                attemptAttack(actionRadius);
                return;
            }
        }

        if (attackable.length >= 5) {
            if (Logging.LOGGING) {
                System.out.println("Overwhelmed, attacking");
            }
            attemptAttack(actionRadius);
        }
    }

    private void attemptAttack(final int actionRadius) throws GameActionException {
        if (rc.canEmpower(actionRadius)) {
            rc.empower(actionRadius);
        }
    }
}
