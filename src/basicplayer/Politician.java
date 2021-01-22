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

public class Politician extends Robot implements RobotInterface {
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

    public Politician(final RobotController robotController, final RobotInfo parent) {
        super( robotController );
        this.parent = parent;

        if (null == parent) {
            this.coordinateSystem = null;
        } else {
            this.coordinateSystem = new CoordinateSystem(parent.location);
        }

        this.mode = PoliticanMode.ROAMING;
        this.enemy = robotController.getTeam().opponent();

        this.type = robotController.getInfluence() == GUARD_POLITICAN_SIZE
            ? PoliticanType.GUARD
            : PoliticanType.GENERAL;

        planner = new Planner( this );
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
                        Logging.info( "EC won!" );
                        robotController.setFlag(Flags.encodeECTakenFlag());
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
        final RobotInfo[] friendly = robotController.senseNearbyRobots(SENSOR_R2, enemy.opponent());

        return Arrays.stream(friendly).anyMatch(robotInfo ->
            robotInfo.type == RobotType.ENLIGHTENMENT_CENTER && robotInfo.location.equals(rushCoords));
    }

    private void guard() throws GameActionException {
        // can't take any actions if cooldown too high
        if (robotController.getCooldownTurns() >= 1) {
            return;
        }
        final RobotInfo furthestMuck = furthestEnemyMuckrakerInRange();

        // if we don't see any enemy muckrakers, just amble about
        if (furthestMuck == null) {
            // see if there's a muckraker in sensors range at all
            final RobotInfo anyMuck = anyMuckraker();
            if (anyMuck != null) {
                Logging.info( "Found muckraker out of empower range, moving in" );
                planner.move(planner.getNextDirection(anyMuck.location));
                return;
            }

            Logging.info( "No nearby muckraker" );
            planner.move(Directions.getRandomDirection());
            return;
        }

        final int furthestMuckDistSquared = robotController.getLocation().distanceSquaredTo(furthestMuck.location);

        // see if we can attack the muckraker effectively
        final int blastRadiusAllies = robotController.senseNearbyRobots(furthestMuckDistSquared, enemy.opponent()).length;
        if (blastRadiusAllies <= 3 && timeWaited >= TIME_TO_WAIT) {
            Logging.info( "Able to attack enemy muckraker" );
            robotController.empower(furthestMuckDistSquared);
            return;
        } else {
            timeWaited++;
        }

        // if we couldn't attack muckraker, get closer
        Logging.info( "Moving in on enemy muckraker" );
        planner.move(planner.getNextDirection(furthestMuck.location));
    }

    private RobotInfo anyMuckraker() {
        RobotInfo[] enemyRobots = robotController.senseNearbyRobots(
            RobotType.POLITICIAN.sensorRadiusSquared,
            enemy);

        return Arrays.stream(enemyRobots)
            .filter(robot -> robot.type == RobotType.MUCKRAKER)
            .findAny().orElse(null);
    }

    private RobotInfo furthestEnemyMuckrakerInRange() {
        RobotInfo[] enemyRobots = robotController.senseNearbyRobots(
            RobotType.POLITICIAN.sensorRadiusSquared,
            enemy);

        final MapLocation myLocation = robotController.getLocation();
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
            if (robotController.canGetFlag(parent.ID)) {
                final int parentFlag = robotController.getFlag(parent.ID);
                if (Flags.getFlagType(parentFlag) == Type.ATTACK_ENEMY_EC) {
                    Logging.info( "Recieved attack orders from EC" );
                    this.mode = PoliticanMode.RUSHING;

                    final int[] coords = Flags.getAttackEnemyECInfo(parentFlag);
                    Logging.info( String.format( "Enemy HQ at coords: X %s, Y %s", coords[0], coords[1] ) );
                    this.rushCoords = coordinateSystem.toAbsolute(coords[0], coords[1]);
                }
            }
        }
    }

    private void attackIfPossible() throws GameActionException {
        int actionRadius = robotController.getType().actionRadiusSquared;
        final RobotInfo[] attackable = robotController.senseNearbyRobots(actionRadius, enemy);
        if (attackable.length != 0) {
            attemptAttack(actionRadius);
            return;
        }

        final RobotInfo[] neutral = robotController.senseNearbyRobots(actionRadius, Team.NEUTRAL);
        if (neutral.length != 0) {
            attemptAttack(actionRadius);
        }
    }

    /**
     * Try to avoid attacking units and focus on ECs
     */
    private void rushAttack() throws GameActionException {
        final int actionRadius = robotController.getType().actionRadiusSquared;

        // the only neutral units are ECs, so explode if we find one
        final RobotInfo[] neutral = robotController.senseNearbyRobots(actionRadius, Team.NEUTRAL);
        if (neutral.length > 0) {
            attemptAttack(actionRadius);
            return;
        }

        final RobotInfo[] attackable = robotController.senseNearbyRobots(actionRadius, enemy);
        for (final RobotInfo target : attackable) {
            if (target.type == RobotType.ENLIGHTENMENT_CENTER) {
                Logging.info( "Enemy EC found, attacking" );
                attemptAttack(actionRadius);
                return;
            }
        }

        if (attackable.length >= 5) {
            Logging.info( "Overwhelmed, attacking" );
            attemptAttack(actionRadius);
        }
    }

    private void attemptAttack(final int actionRadius) throws GameActionException {
        if (robotController.canEmpower(actionRadius)) {
            robotController.empower(actionRadius);
        }
    }
}
