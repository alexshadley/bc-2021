package basicplayer;

import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.Team;

/**
 * This is just examplefuncsplayer except ECs bid 1 each turn
 */
public strictfp class RobotPlayer {
    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) {
        try {
            final Team enemyTeam = rc.getTeam().opponent();
            final RobotInfo parent = Startup.getParent(rc);

            RobotInterface robotCode;
            switch (rc.getType()) {
                case ENLIGHTENMENT_CENTER:
                    robotCode = new EnlightenmentCenter(rc);
                    break;

                case POLITICIAN:
                    robotCode = new Politician(rc, parent);
                    break;

                case SLANDERER:
                    robotCode = new Slanderer(rc, parent);
                    break;

                case MUCKRAKER:
                    final boolean isScout = rc.getRoundNum() <= 100;
                    robotCode = new MuckrackerV2(rc, enemyTeam, parent);
                    break;

                default:
                    throw new RuntimeException();
            }

            while (true) {
                try {
                    robotCode.run();
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (
            final Exception e) {
            e.printStackTrace();
        }
    }
}
