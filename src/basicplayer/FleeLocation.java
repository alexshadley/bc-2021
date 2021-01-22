package basicplayer;

import java.util.List;

import battlecode.common.MapLocation;

public class FleeLocation extends Location {
    private final List<MapLocation> enemies;
    private final List<MapLocation> enlightenmentCenters;

    public FleeLocation( MapLocation location, double passability, List<MapLocation> enemies, List<MapLocation> enlightenmentCenters ) {
        super( location, passability );

        this.enemies = enemies;
        this.enlightenmentCenters = enlightenmentCenters;
    }

    public List<MapLocation> getEnemies() {
        return ( enemies );
    }

    public List<MapLocation> getEnlightenmentCenters() {
        return ( enlightenmentCenters );
    }
}
