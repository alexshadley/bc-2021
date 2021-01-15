package basicplayer;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;
import common.CoordinateSystem;
import common.Directions;
import common.Flags;
import common.Flags.Type;
import common.Pathfinding;
import common.Robot;
import common.Planner;
import common.Logging;

public class Politician implements Robot {
    private static final int ACTION_R2 = 9;
    private static final int SENSOR_R2 = 25;
    private static final int DETECT_R2 = 25;

    private static enum PoliticanMode {
        ROAMING,
        RUSHING
    }

    private final RobotController rc;
    private final CoordinateSystem coordinateSystem;
    private final RobotInfo parent;
    private PoliticanMode mode;
    // only valid for RUSHING mode, indicates the coords of the base to rush
    private MapLocation rushCoords;

    private Planner planner;

    private final Team enemy;

    public Politician(final RobotController rc, final RobotInfo parent) {
        this.rc = rc;
        this.parent = parent;

        if ( null == parent ) {
            this.coordinateSystem = null;
        } else {
            this.coordinateSystem = new CoordinateSystem(parent.location);
        }

        this.mode = PoliticanMode.ROAMING;
        this.enemy = rc.getTeam().opponent();

        planner = new Planner( rc );
    }

    /**
     * Main execution loop
     **/
    public void run() throws GameActionException {
        while (true) {
            if ( null != parent ) {
                checkCommunications();
            }

            if (mode == PoliticanMode.ROAMING) {
                attackIfPossible();
                planner.move(Directions.getRandomDirection());
            } else {
                rushAttack();
                planner.move(planner.getNextDirection(rushCoords));
            }

            Clock.yield();
        }
    }

    private void checkCommunications() throws GameActionException {
        // don't check while rushing to save bytecode
        if (mode != PoliticanMode.RUSHING) {
            if ( rc.canGetFlag( parent.ID ) ) {
                final int parentFlag = rc.getFlag(parent.ID);
                if (Flags.getFlagType(parentFlag) == Type.ATTACK_ENEMY_EC) {
                    if ( Logging.LOGGING ) {
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
                if ( Logging.LOGGING ) {
                    System.out.println("Enemy EC found, attacking");
                }
                attemptAttack(actionRadius);
                return;
            }
        }

        if (attackable.length >= 5) {
            if ( Logging.LOGGING ) {
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
