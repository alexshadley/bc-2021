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

    private static final int ACTION_R2 = 9;
    private static final int SENSOR_R2 = 25;
    private static final int DETECT_R2 = 25;

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
                    planner.move(planner.getNextDirection(rushCoords));
                }
            } else {
                guard();
            }

            Clock.yield();
        }
    }

    private void guard() throws GameActionException {
        // can't take any actions if cooldown too high
        if (rc.getCooldownTurns() >= 1) {
            return;
        }
        final RobotInfo nearestMuck = nearestEnemyMuckraker();

        // if we don't see any enemy muckrakers, just amble about
        if (nearestMuck == null) {
            if ( Logging.LOGGING ) {
                System.out.println("No nearest muckraker");
            }
            planner.move(Directions.getRandomDirection());
            return;
        }

        final int nearestMuckDistSquared = rc.getLocation().distanceSquaredTo(nearestMuck.location);

        // if we're right on top of it then just blow up, we don't want to lock up
        if (nearestMuckDistSquared <= 2) {
            if ( Logging.LOGGING ) {
                System.out.println("Right on top of enemy muckraker, attacking");
            }
            rc.empower(nearestMuckDistSquared);
            return;
        }

        // see if we can attack the muckraker effectively
        final int blastRadiusAllies = rc.senseNearbyRobots(nearestMuckDistSquared, enemy.opponent()).length;
        if (blastRadiusAllies <= 1 && rc.canEmpower(nearestMuckDistSquared)) {
            if ( Logging.LOGGING ) {
                System.out.println("Able to attack enemy muckraker");
            }
            rc.empower(nearestMuckDistSquared);
            return;
        }

        // if we couldn't attack the nearest muckraker, get closer
        if ( Logging.LOGGING ) {
            System.out.println("Moving in on enemy muckraker");
        }
        planner.move(planner.getNextDirection(nearestMuck.location));
    }

    private RobotInfo nearestEnemyMuckraker() {
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(
            RobotType.POLITICIAN.sensorRadiusSquared,
            enemy);

        RobotInfo[] enemyMuckrakers = Arrays.stream(enemyRobots)
            .filter(robot -> robot.type == RobotType.MUCKRAKER)
            .toArray(RobotInfo[]::new);

        if (enemyMuckrakers.length == 0) {
            return null;
        }

        final MapLocation myLocation = rc.getLocation();

        RobotInfo closest = enemyMuckrakers[0];
        int distanceSquared = myLocation.distanceSquaredTo(closest.location);
        for (final RobotInfo robot : enemyMuckrakers) {
            if (myLocation.distanceSquaredTo(robot.location) < distanceSquared) {
                closest = robot;
                distanceSquared = myLocation.distanceSquaredTo(robot.location);
            }
        }

        return closest;
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
