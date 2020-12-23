package anonymous.gazetteers.osm.model;

import java.util.ArrayList;
import java.util.List;

public class OSMModel {
    private String name;
    private List<Address> addresses = new ArrayList<>();

    public OSMModel(String name, List<fr.dudie.nominatim.model.Address> addresses) {
        this.name = name;
        if (addresses != null)
            this.addresses = transform(addresses);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }


    public List<Address> transform(List<fr.dudie.nominatim.model.Address> addresses) {
        List<Address> result = new ArrayList<>();
        for (fr.dudie.nominatim.model.Address a : addresses) {
            Address addr = new Address();
            addr.setAddressElements(a.getAddressElements());
            addr.setBoundingBox(a.getBoundingBox());
            addr.setDisplayName(a.getDisplayName());
            addr.setElementClass(a.getElementClass());
            addr.setElementType(a.getElementType());
            addr.setLatitude(a.getLatitude());
            addr.setLatitudeE6(a.getLatitudeE6());
            addr.setLongitude(a.getLongitude());
            addr.setLongitudeE6(a.getLongitudeE6());
            addr.setImportance(a.getImportance());
            addr.setPlaceId(a.getPlaceId());
            addr.setPlaceRank(a.getPlaceRank());
            addr.setOsmId(a.getOsmId());
            addr.setOsmType(a.getOsmType());
            addr.setWkt(a.getWkt());
            addr.setNameDetails(a.getNameDetails());
            result.add(addr);
        }
        return result;
    }
}
