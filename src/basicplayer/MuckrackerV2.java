package basicplayer;

import battlecode.common.*;

/**
 * The return of the Muckracker, but this time with some improvements including:
 * - All method names are quotes from my minds enigma
 * - Muckernet // TODO
 * - BDSM EC Choking action // TODO
 * - Integrated Van-Helsing instincts when hunting slanderers // TODO
 * - Training from the nations top bloodhounds to find baddies // TODO
 */
public class MuckrackerV2 implements Robot{

    private static final int ACTION_R2 = 12;
    private static final int SENSOR_R2 = 40;
    private static final int DETECT_R2 = 30;

    private MuckMode mode;
    private final RobotController robotController;
    private final Team enemyTeam;
    private final RobotInfo parent;
    private final Direction scoutDir;
    private MapLocation enemyEC;
    private CoordinateSystem coordinateSystem;

    private Direction scanDir = null;
    private Direction lastVerticalScanDir = null;
    private Boolean goEast = true;
    private int scanDirCount = 0;

    private enum MuckMode {
        SCOUT,
        SCAN,
        CHOKE,
        HUNT
    }

    public MuckrackerV2(RobotController robotController, Team enemyTeam, RobotInfo parent) {
        this.mode = MuckMode.SCOUT;
        this.robotController = robotController;
        this.enemyTeam = enemyTeam;
        this.parent = parent;

        // Sneaky way to determine scout direction. If we are built on the same
        // round number, this should set us off in the same direction.
        scoutDir = Directions.directions[robotController.getRoundNum() % 8];

        if (parent != null)
            this.coordinateSystem = new CoordinateSystem(parent.location);
    }


    /**
     * Main execution loop. All methods should be designed to only
     * do one action, as this will Clock.yield()
     * @throws GameActionException If we r bad
     */
    @Override
    public void run() throws GameActionException {
        while(true) {
            switch (mode) {
                case SCOUT:
                    System.out.println("Case SCOUT\n");
                    scout();
                    break;
                case SCAN:
                    System.out.println("Case SCAN\n");
                    scan();
                    break;
                //TODO
                case CHOKE:
                    break;
                //TODO
                case HUNT:
                    break;
                //TODO (NE?)
                default:
                    break;
            }

            Clock.yield();
        }
    }

    private void scout() throws GameActionException {
        if (seesEnemyEC()) {
            mode = MuckMode.CHOKE;
        }

        if (tryKill()) {
            return;
        } else if (!robotController.onTheMap(robotController.getLocation().add(scoutDir))) {
            mode = MuckMode.SCAN;
        } else {
            Pathfinding.moveNoYield(scoutDir, robotController);
        }
    }

    private boolean tryKill() throws GameActionException {
        RobotInfo enemyLiar = getClosestSlanderer();
        if (enemyLiar != null && Directions.distanceTo(enemyLiar.location, robotController.getLocation()) <= ACTION_R2) {
            robotController.expose(enemyLiar.getLocation());
            return true;
        }
        return false;
    }

    /**
     * Returns the robot info of the closest slanderer.
     * If none are found returns null
     * @return A robot's info
     */
    private RobotInfo getClosestSlanderer() {
        RobotInfo[] infos = robotController.senseNearbyRobots(SENSOR_R2, enemyTeam);
        RobotInfo closest = null;

        for (RobotInfo info : infos) {
            if (info.type == RobotType.SLANDERER) {
                if (closest == null) {
                    closest = info;
                } else if (Directions.distanceTo(info.getLocation(), robotController.getLocation()) < Directions.distanceTo(closest.getLocation(), robotController.getLocation())) {
                    closest = info;
                }
            }
        }
        return closest;
    }

    /**
     * And enemy EC is a neutral or enemy team ec
     * @return true if we set a flag on an enemy EC, false otherwise
     */
    private boolean seesEnemyEC() throws GameActionException {
        RobotInfo[] robots = robotController.senseNearbyRobots(SENSOR_R2);
        for (RobotInfo robot : robots) {
            if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
                if (robot.getTeam() == enemyTeam) {
                    enemyEC = robot.location;
                    setEnemyHQFlag(enemyEC);
                    return true;
                } else if (robot.getTeam() != robotController.getTeam()){
                    setNeutralECFlag(robot.getLocation(), robot.getConviction());
                    return false;
                }
            }
        }
        return false;
    }

    private void setEnemyHQFlag(MapLocation location) throws GameActionException {
        final int[] coords = coordinateSystem.toRelative(location);
        if ( Logging.LOGGING ) {
            System.out.println(String.format("Found enemy HQ at relative coords: %s, %s", coords[0], coords[1]));
        }
        robotController.setFlag(Flags.encodeEnemyECFoundFlag(coords[0], coords[1]));
    }

    private void setNeutralECFlag(MapLocation location, int conviction) throws GameActionException {
        final int[] coords = coordinateSystem.toRelative(location);
        if ( Logging.LOGGING ) {
            System.out.println(String.format("Found enemy HQ at relative coords: %s, %s", coords[0], coords[1]));
        }
        robotController.setFlag(Flags.encodeEnemyECFoundFlag(coords[0], coords[1]));
    }

    /**
     * Go North/South, then East, and then back west
     */
    private void scan() throws GameActionException {
        if (scanDir == null) {
            // MAGIC DIRECTION
            lastVerticalScanDir = scanDir = Direction.NORTH;
        }

        if (seesEnemyEC()) {
            mode = MuckMode.CHOKE;
        }
        if (tryKill()) {
            return;
        }

        switch (scanDir) {
            case NORTH:
                System.out.println("case NORTH");
                if (!robotController.onTheMap(robotController.getLocation().add(scanDir))) {
                    scanDir = goEast ? Direction.EAST : Direction.WEST;
                    //We move here to not miss an action before yielding
                    Pathfinding.moveNoYield(scanDir, robotController);
                    scanDirCount += 1;
                } else {
                    Pathfinding.moveNoYield(scanDir, robotController);
                    lastVerticalScanDir = Direction.NORTH;
                }
                break;
            case SOUTH:
                System.out.println("case SOUTH");
                if (!robotController.onTheMap(robotController.getLocation().add(scanDir))) {
                    scanDir = goEast ? Direction.EAST : Direction.WEST;
                    //We move here to not miss an action before yielding
                    Pathfinding.moveNoYield(scanDir, robotController);
                    scanDirCount += 1;
                } else {
                    Pathfinding.moveNoYield(scanDir, robotController);
                    lastVerticalScanDir = Direction.SOUTH;
                }
                break;
            case EAST:
            case WEST:
                System.out.println("case " + scanDir);
                electricSlide();
                break;
            default:
                break;
        }
    }

    private void electricSlide() throws GameActionException {
        if (scanDirCount >=10) {
            switch (lastVerticalScanDir) {
                case NORTH:
                    System.out.println("last scan " + lastVerticalScanDir);
                    lastVerticalScanDir = scanDir = Direction.SOUTH;
                    break;
                case SOUTH:
                default:
                    System.out.println("last scan " + lastVerticalScanDir);
                    lastVerticalScanDir = scanDir = Direction.NORTH;
                    break;
            }
            //We move so we don't miss an action before the yield in run.
            Pathfinding.moveNoYield(scanDir, robotController);
            scanDirCount = 0;
        // if we hit the edge of the map, we need to swap the translation direction
        } else if (!robotController.onTheMap(robotController.getLocation().add(scanDir))) {
            goEast = !goEast;
            switch (lastVerticalScanDir) {
                case NORTH:
                    System.out.println("last scan " + lastVerticalScanDir);
                    lastVerticalScanDir = scanDir = Direction.SOUTH;
                    break;
                case SOUTH:
                default:
                    System.out.println("last scan " + lastVerticalScanDir);
                    lastVerticalScanDir = scanDir = Direction.NORTH;
                    break;
            }
            Pathfinding.moveNoYield(scanDir, robotController);
            scanDirCount = 0;
        } else {
            Pathfinding.moveNoYield(scanDir, robotController);
            System.out.println("Move count " + scanDirCount);
            scanDirCount += 1;
        }
    }

}
