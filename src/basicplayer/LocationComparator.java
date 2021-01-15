package basicplayer;

import java.util.Comparator;

public class LocationComparator implements Comparator<Location> {
    public int compare( Location l1, Location l2 ) {
        double l1Cost = l1.getDistance() + 0.5 / l1.getPassability();
        double l2Cost = l2.getDistance() + 0.5 / l2.getPassability();

        return ( (int) ( l2Cost - l1Cost ) );
    }
}
