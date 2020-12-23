package anonymous.dataset.model.basic;

import java.util.List;

/**
 * Created by [anonymous] [anonymous] on 5/10/2017.
 */
public class NLPDataModel {
    private List<String> names;
    private List<String> locations;
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getLocations() {
        return locations;
    }

    public void setLocations(List<String> locations) {
        this.locations = locations;
    }

    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }
}
