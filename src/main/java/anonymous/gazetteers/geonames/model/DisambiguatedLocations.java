package anonymous.gazetteers.geonames.model;

import org.geonames.Toponym;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DisambiguatedLocations implements Serializable {
    private long recordIdentifier;
    private Map<String, Toponym> places = new HashMap<>();

    public long getRecordIdentifier() {
        return recordIdentifier;
    }

    public void setRecordIdentifier(long recordIdentifier) {
        this.recordIdentifier = recordIdentifier;
    }

    public Map<String, Toponym> getPlaces() {
        return places;
    }

    public void setPlaces(Map<String, Toponym> places) {
        this.places = places;
    }
}
