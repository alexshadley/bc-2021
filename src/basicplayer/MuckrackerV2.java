package basicplayer;

import basicplayer.Flags.Type;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The return of the Muckracker, but this time with some improvements including:
 * - All method names are quotes from my minds enigma
 * - Muckernet // TODO
 * - BDSM EC Choking action // TODO
 * - Integrated Van-Helsing instincts when hunting slanderers // TODO
 * - Training from the nations top bloodhounds to find baddies // TODO
 */
public class MuckrackerV2 implements Robot {

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
        HUNT,
        CLEAREC
    }

    public MuckrackerV2(RobotController robotController, Team enemyTeam, RobotInfo parent) {
        this.mode = MuckMode.SCOUT;
        this.robotController = robotController;
        this.enemyTeam = enemyTeam;
        this.parent = parent;

        // Sneaky way to determine scout direction. If we are built on the same
        // round number, this should set us off in the same direction.
        scoutDir = getScoutDir();

        if (parent != null) {
            this.coordinateSystem = new CoordinateSystem(parent.location);
        }
    }

    private Direction getScoutDir() {
        RobotInfo robotInfo;
        try {
            robotInfo = robotController.senseRobotAtLocation(robotController.getLocation().add(Direction.NORTH));
            if (robotInfo != null && robotInfo.getTeam() == robotController.getTeam() && robotInfo.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                return Direction.SOUTH;
            }
        } catch (GameActionException e) {
            //noop
        }
        try {
            robotInfo = robotController.senseRobotAtLocation(robotController.getLocation().add(Direction.SOUTH));
            if (robotInfo != null && robotInfo.getTeam() == robotController.getTeam() && robotInfo.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                goEast = false;
                return Direction.NORTH;
            }
        } catch (GameActionException e) {
            //noop
        }
        try {
            robotInfo = robotController.senseRobotAtLocation(robotController.getLocation().add(Direction.EAST));
            if (robotInfo != null && robotInfo.getTeam() == robotController.getTeam() && robotInfo.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                goEast = false;
                return Direction.WEST;
            }
        } catch (GameActionException e) {
            //noop
        }
        try {
            robotInfo = robotController.senseRobotAtLocation(robotController.getLocation().add(Direction.WEST));
            if (robotInfo != null && robotInfo.getTeam() == robotController.getTeam() && robotInfo.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                return Direction.EAST;
            }
        } catch (GameActionException e) {
            //noop
        }
        try {
            robotInfo = robotController.senseRobotAtLocation(robotController.getLocation().add(Direction.NORTHWEST));
            if (robotInfo != null && robotInfo.getTeam() == robotController.getTeam() && robotInfo.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                return Direction.SOUTHEAST;
            }
        } catch (GameActionException e) {
            //noop
        }
        try {
            robotInfo = robotController.senseRobotAtLocation(robotController.getLocation().add(Direction.NORTHEAST));
            if (robotInfo != null && robotInfo.getTeam() == robotController.getTeam() && robotInfo.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                goEast = false;
                return Direction.SOUTHWEST;
            }
        } catch (GameActionException e) {
            //noop
        }
        try {
            robotInfo = robotController.senseRobotAtLocation(robotController.getLocation().add(Direction.SOUTHWEST));
            if (robotInfo != null && robotInfo.getTeam() == robotController.getTeam() && robotInfo.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                return Direction.NORTHEAST;
            }
        } catch (GameActionException e) {
            //noop
        }
        try {
            robotInfo = robotController.senseRobotAtLocation(robotController.getLocation().add(Direction.SOUTHEAST));
            if (robotInfo != null && robotInfo.getTeam() == robotController.getTeam() && robotInfo.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                goEast = false;
                return Direction.NORTHWEST;
            }
        } catch (GameActionException e) {
            //noop
        }
        return Direction.SOUTH;
    }

    /**
     * Main execution loop. All methods should be designed to only
     * do one action, as this will Clock.yield()
     * @throws GameActionException If we r bad
     */
    @Override
    public void run() throws GameActionException {
        while (true) {
            switch (mode) {
                case SCOUT:
                    System.out.println("Case SCOUT\n");
                    scout();
                    break;
                case SCAN:
                    System.out.println("Case SCAN\n");
                    scan();
                    break;
                case CHOKE:
                    choke();
                    break;
                //TODO
                case HUNT:
                    hunt();
                    break;
                case CLEAREC:
                    clearEC();
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
        } else if (!robotController.onTheMap(robotController.getLocation().translate(scoutDir.dx * 3, scoutDir.dy * 3))) {
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
                } else if (robot.getTeam() != robotController.getTeam()) {
                    setNeutralECFlag(robot.getLocation(), robot.getConviction());
                    return false;
                }
            }
        }

        final Optional<MapLocation> maybeEnemyEC = checkCommsForEnemyEC();
        if (maybeEnemyEC.isPresent() && robotController.getID() % 2 == 0) {
            enemyEC = maybeEnemyEC.get();
            setEnemyHQFlag(enemyEC);
            return true;
        }

        return false;
    }

    // TODO: Noah, refactor as you see fit; I'm just guessing here
    private Optional<MapLocation> checkCommsForEnemyEC() throws GameActionException {
        if (robotController.canGetFlag(parent.ID)) {
            final int parentFlag = robotController.getFlag(parent.ID);
            if (Flags.getFlagType(parentFlag) == Type.ENEMY_EC_FOUND) {
                Logging.log("Discovered enemy EC location from home base");

                final int[] coords = Flags.getAttackEnemyECInfo(parentFlag);
                if (Logging.LOGGING) {
                    System.out.println("X: " + coords[0]);
                    System.out.println("Y: " + coords[1]);
                }
                return Optional.of(coordinateSystem.toAbsolute(coords[0], coords[1]));
            }
        }

        return Optional.empty();
    }


    private void setEnemyHQFlag(MapLocation location) throws GameActionException {
        final int[] coords = coordinateSystem.toRelative(location);
        if (Logging.LOGGING) {
            System.out.println(String.format("Found enemy HQ at relative coords: %s, %s", coords[0], coords[1]));
        }
        robotController.setFlag(Flags.encodeEnemyECFoundFlag(coords[0], coords[1]));
    }

    private void setNeutralECFlag(MapLocation location, int conviction) throws GameActionException {
        final int[] coords = coordinateSystem.toRelative(location);
        if (Logging.LOGGING) {
            System.out.println(String.format("Found enemy HQ at relative coords: %s, %s", coords[0], coords[1]));
        }

        robotController.setFlag(Flags.encodeNeturalECFoundFlag(coords[0], coords[1]));
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
            tryKill();
            return;
        }
        if (tryKill()) {
            return;
        }

        switch (scanDir) {
            case NORTH:
                System.out.println("case NORTH");
                if (!robotController.onTheMap(robotController.getLocation().translate(0, 3))) {
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
                if (!robotController.onTheMap(robotController.getLocation().translate(0, -3))) {
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
        if (scanDirCount >= 20) {
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
        } else if (!robotController.onTheMap(robotController.getLocation().translate(scanDir.dx * 3, scanDir.dy * 3))) {
            goEast = !goEast;
            scanDir = goEast ? Direction.EAST : Direction.WEST;
            scanDirCount = -10;

            Pathfinding.moveNoYield(scanDir, robotController);
        } else {
            Pathfinding.moveNoYield(scanDir, robotController);
            System.out.println("Move count " + scanDirCount);
            scanDirCount += 1;
        }
    }

    private void choke() throws GameActionException {
        //If we are too far away move closer
        if (Directions.distanceTo(enemyEC, robotController.getLocation()) > 12) {
            System.out.println("Moving closer to enemy EC\n");
            Pathfinding.moveNoYield(Pathfinding.findPath(enemyEC, robotController), robotController);
        } else if (!standingByEnemyEC() && !tryKill()) {
            List<MapLocation> chokeSpots = openSpotsByEnemyEC();
            if (chokeSpots.isEmpty()) {
                System.out.println("No choke spots, hunting\n");
                tryKill();
                mode = MuckMode.CLEAREC;
            } else {
                System.out.println("Found choke spot, moving to choke\n");
                Pathfinding.moveNoYield(Pathfinding.findPath(chokeSpots.get(0), robotController), robotController);
            }
        } else {
            //We should be standing by an enemy EC, we should try to kill.
            tryKill();
        }
    }

    private boolean standingByEnemyEC() throws GameActionException {
        if (robotController.canSenseLocation(enemyEC)) {
            for (Direction direction : Directions.directions) {
                MapLocation location = robotController.getLocation().add(direction);
                if (robotController.onTheMap(location)) {
                    RobotInfo ec = robotController.senseRobotAtLocation(location);
                    if (ec != null && ec.getTeam() == enemyTeam && ec.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private List<MapLocation> openSpotsByEnemyEC() throws GameActionException {
        List<MapLocation> openSpots = new ArrayList<>();

        for (Direction direction : Directions.directions) {
            if (robotController.onTheMap(enemyEC.add(direction))) {
                RobotInfo robotInfo = robotController.senseRobotAtLocation(enemyEC.add(direction));
                if (robotInfo != null && robotInfo.getTeam() != robotController.getTeam()) {
                    openSpots.add(enemyEC.add(direction));
                } else if (robotInfo == null) {
                    openSpots.add(enemyEC.add(direction));
                }
            }
        }

        return openSpots;
    }

    /**
     * really borky right now.
     * @throws GameActionException
     */
    private void hunt() throws GameActionException {
        MapLocation closestSland = closestSlandererToHunt();
        if (closestSland != null && Directions.distanceTo(closestSland, robotController.getLocation()) <= ACTION_R2) {
            if (robotController.canExpose(closestSland)) {
                robotController.expose(closestSland);
            } else {
                Pathfinding.moveNoYield(Directions.getRandomDirection(), robotController);
                mode = MuckMode.SCAN;
            }
        } else {
            if (closestSland == null || robotController.getLocation().directionTo(closestSland) == Direction.CENTER) {
                //mode = MuckMode.SCAN;
                Pathfinding.moveNoYield(Directions.getRandomDirection(), robotController);
                robotController.setFlag(0);
            } else {
                Pathfinding.moveNoYield(Pathfinding.findPath(closestSland, robotController), robotController);
            }
        }
    }

    private MapLocation closestSlandererToHunt() throws GameActionException {
        RobotInfo[] infos = robotController.senseNearbyRobots(SENSOR_R2);
        RobotInfo closest = null;
        MapLocation closestOutOfRange = null;

        for (RobotInfo info : infos) {
            // Check in vision
            if (info.type == RobotType.SLANDERER && info.getTeam() == enemyTeam) {
                if (closest == null) {
                    closest = info;
                    setEnemySlandFlag(closest.getLocation());
                } else if (Directions.distanceTo(info.getLocation(), robotController.getLocation()) < Directions.distanceTo(closest.getLocation(), robotController.getLocation())) {
                    closest = info;
                    setEnemySlandFlag(closest.getLocation());
                }
            } else if (info.getType() == RobotType.MUCKRAKER && info.getTeam() == robotController.getTeam()) {
                int flag = robotController.getFlag(info.getID());
                if (Flags.getFlagType(flag) == Flags.Type.ENEMY_SLANDERER) {
                    final int[] coords = Flags.getEnemySlandererFlag(flag);
                    MapLocation enemySland = coordinateSystem.toAbsolute(coords[0], coords[1]);
                    setEnemySlandFlag(enemySland);
                    closestOutOfRange = enemySland;
                }
            }
        }
        return (closest != null ? closest.getLocation() : closestOutOfRange);
    }

    private void setEnemySlandFlag(MapLocation location) throws GameActionException {
        final int[] coords = coordinateSystem.toRelative(location);
        if (Logging.LOGGING) {
            System.out.println(String.format("Found enemy HQ at relative coords: %s, %s", coords[0], coords[1]));
        }
        robotController.setFlag(Flags.encodeEnemySladererFoundFlag(coords[0], coords[1]));
    }

    //GET AWAY YOU BASTARDS
    private void clearEC() throws GameActionException {
        Direction clearDir = Directions.getRandomDirection();
        for (int i = 0; i < 10; i++) {
            Pathfinding.moveNoYield(clearDir, robotController);
            for (int j = 0; j < robotController.getCooldownTurns(); j++) {
                Clock.yield();
            }
        }
        mode = MuckMode.HUNT;
    }
}
