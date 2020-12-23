package anonymous.gazetteers.geonames.model;

import org.geonames.ToponymSearchResult;

import java.io.Serializable;

public class ToponymExportModel implements Serializable {
    private String name;
    private ToponymSearchResult result;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ToponymSearchResult getResult() {
        return result;
    }

    public void setResult(ToponymSearchResult result) {
        this.result = result;
    }
}
