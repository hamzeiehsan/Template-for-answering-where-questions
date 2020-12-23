package anonymous.gazetteers.geonames.model;

import java.io.Serializable;

public class PlaceLocation implements Serializable, Comparable<PlaceLocation> {
    private String name;
    private Double latitude, longitude;
    private int count = 1;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @Override
    public int hashCode() {
        return name.hashCode()+latitude.hashCode()+longitude.hashCode();
    }

    @Override
    public int compareTo(PlaceLocation placeLocation) {
        if (this.name.equals(placeLocation.name))
            if (Math.abs(this.getLatitude()-placeLocation.getLatitude()) < 0.00001 && Math.abs(this.getLongitude()-placeLocation.getLongitude()) < 0.00001) {
                this.count++;
                placeLocation.count++;
                return 0;
            }
        return 1;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof PlaceLocation) {
            PlaceLocation placeLocation = (PlaceLocation) o;
            if (compareTo(placeLocation) == 0) {
                return true;
            }
        }
        return false;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
