package common;

import java.util.Comparator;

public class LocationComparator implements Comparator<Location> {
    public int compare( Location l1, Location l2 ) {
        double l1Cost = l1.getDistance() / l1.getPassability();
        double l2Cost = l2.getDistance() / l2.getPassability();

        return ( (int) ( l2Cost - l1Cost ) );
    }
}
