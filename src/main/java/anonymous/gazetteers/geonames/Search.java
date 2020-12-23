package anonymous.gazetteers.geonames;

import anonymous.gazetteers.geonames.model.ToponymExportModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import org.geonames.Toponym;
import org.geonames.ToponymSearchCriteria;
import org.geonames.ToponymSearchResult;
import org.geonames.WebService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Search {
    private static final Logger LOG = LoggerFactory.getLogger(Search.class);
    public static ToponymSearchResult simpleSearch (String query) throws Exception {
        ToponymSearchCriteria criteria = new ToponymSearchCriteria();
        criteria.setQ(query);
         return WebService.search(criteria);
    }

    public static ToponymSearchResult searchNamesEqual (String name) throws Exception {
        ToponymSearchCriteria criteria = new ToponymSearchCriteria();
        criteria.setQ(name);
        criteria.setNameEquals(name);
        return WebService.search(criteria);
    }

    public static ToponymSearchResult search (String name) throws Exception {
        ToponymSearchResult result = searchNamesEqual(name);
        if (result == null && result.getTotalResultsCount() == 0) {
            result = simpleSearch(name);
        }
        return result;
    }

    public static void main (String [] args) throws Throwable {
        WebService.setUserName("e_[anonymous]"); //setting for geonames account!
        ToponymSearchResult t1 = search("Belgrade");
        ToponymSearchResult t2 = search("England");
        ToponymSearchResult t3 = search("United State");
        ToponymExportModel nsw = new ToponymExportModel();
        nsw.setResult(t1);
        nsw.setName("New South Wales");
        ToponymSearchResult AU = search("Australia");
        ToponymExportModel au = new ToponymExportModel();
        au.setResult(AU);
        au.setName("Australia");
        ToponymSearchResult LIS = search("Lismore");
        ToponymExportModel lis = new ToponymExportModel();
        lis.setName("Lismore");
        lis.setResult(LIS);
        List<String> names = new ArrayList<>();
        names.add("New South Wales");
        names.add("Australia");
        names.add("Lismore");

        Map<String, ToponymExportModel> results = new HashMap<>();
        results.put("New South Wales", nsw);
        results.put("Australia", au);
        results.put("Lismore", lis);
        Map<String, Toponym> disambiguatedOnes = Disambiguation.disambiguate(names, results);

        Gson gson = new Gson();
        Type mapType = new TypeToken<Map<String, Integer>>() {
        }.getType();
        JsonReader scaleGeonamesReader = new JsonReader(new FileReader("src/main/resources/scales_geonames.json"));
        JsonReader scalePlaceTypes = new JsonReader(new FileReader("src/main/resources/scales_place_type.json"));
        Map<String, Integer> scalesGeonames = gson.fromJson(scaleGeonamesReader, mapType);
        Map<String, Integer> scalesPlaces = gson.fromJson(scalePlaceTypes, mapType);
        for (Toponym t : disambiguatedOnes.values())
            System.out.println(t.getFeatureCode()+"--"+scalesGeonames.get(t.getFeatureCode()));
        System.out.println("Found!");
    }
}
