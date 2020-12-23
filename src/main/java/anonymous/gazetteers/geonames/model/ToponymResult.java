package anonymous.gazetteers.geonames.model;

import org.geonames.ToponymSearchResult;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ToponymResult implements Serializable {
    public static Map<String, ToponymSearchResult> getToponyms() {
        return ToponymResult.toponyms;
    }

    public static void setToponyms(Map<String, ToponymSearchResult> toponyms) {
        ToponymResult.toponyms = toponyms;
    }

    public void add (String name, ToponymSearchResult result) {
        ToponymResult.toponyms.put(name, result);
    }

    private static Map<String, ToponymSearchResult> toponyms = new HashMap<>();
}
