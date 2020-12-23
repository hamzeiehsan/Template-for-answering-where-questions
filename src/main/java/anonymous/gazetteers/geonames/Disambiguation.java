package anonymous.gazetteers.geonames;

import anonymous.gazetteers.geonames.model.ToponymExportModel;
import org.geonames.Toponym;
import org.geonames.ToponymSearchResult;
import org.geonames.WebService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Disambiguation {
    private static final Logger LOG = LoggerFactory.getLogger(Disambiguation.class);
    private static List<String> invalidFeatureCode = new ArrayList<>();

    static {
        invalidFeatureCode.add("PPL");
        invalidFeatureCode.add("PPLA");
        invalidFeatureCode.add("PPLA2");
        invalidFeatureCode.add("PPLA3");
        invalidFeatureCode.add("PPLA4");
        invalidFeatureCode.add("PPLC");
        invalidFeatureCode.add("PPLCH");
        invalidFeatureCode.add("PPLF");
        invalidFeatureCode.add("PPLG");
        invalidFeatureCode.add("PPLH");
        invalidFeatureCode.add("PPLL");
        invalidFeatureCode.add("PPLQ");
        invalidFeatureCode.add("PPLR");
        invalidFeatureCode.add("PPLS");
        invalidFeatureCode.add("PPLW");
        invalidFeatureCode.add("PPLX");
        invalidFeatureCode.add("STLMT");
    }

    public static Map<String, Toponym> disambiguate(List<String> names, Map<String, ToponymExportModel> database) {

        Map<String, Toponym> finalResults = new HashMap<>();
        Map<String, List<Toponym>> results = new HashMap<>();
        Integer testThem = 0;
        for (String name : names) {
            if (name.length() > 1) {
                results.put(name, validate(database.get(name)).getResult().getToponyms());
                testThem += database.get(name).getResult().getToponyms().size();
            }
        }
        if (testThem > 75)
            return disambiguate2(names, database);
        List<List<Toponym>> lists = new ArrayList<>();
        for (List<Toponym> t : results.values())
            lists.add(t);
        List<Toponyms> toponyms = new ArrayList<>();
        GeneratePermutations(lists, toponyms, 0, "");
        Double minDistance = 1000000d;
        Toponyms best = null;
        for (Toponyms t : toponyms) {
            t.populateDistance();
            if (minDistance > t.getDistance()) {
                minDistance = t.getDistance();
                best = t;
            }
        }
        for (String name : names) {
            for (Toponym t : best.getToponyms()) {
                if (name.toLowerCase().trim().equals(t.getName().trim().toLowerCase())) {
                    finalResults.put(name, t);
                }
            }
        }
        if (finalResults.size() == best.getToponyms().size())
            return finalResults;
        for (String name : names) {
            if (!finalResults.containsKey(name)) {
                List<Toponym> tS = results.get(name);
                for (Toponym t : tS) {
                    if (best.find(t.getGeoNameId()))
                        finalResults.put(name, t);
                }
            }
        }
        return finalResults;
    }

    public static Map<String, Toponym> disambiguate2(List<String> names, Map<String, ToponymExportModel> database) {
        Map<String, Toponym> finalResults = new HashMap<>();
        Map<String, ToponymSearchResult> results = new HashMap<>();
        Map<Integer, Map<String, Toponym>> minDistanceToponym = new HashMap<>();
        Map<Integer, Map<String, Double>> minDistances = new HashMap<>();
        Map<Integer, Double> sumDistances = new HashMap<>();

        for (String name : names) {
            if (name.length() > 1) {
                results.put(name, validate(database.get(name)).getResult());
            }
        }

        ToponymSearchResult reference = results.get(names.get(0));
        for (Toponym toponym : reference.getToponyms()) {
            minDistances.put(toponym.getGeoNameId(), new HashMap<String, Double>());
            minDistanceToponym.put(toponym.getGeoNameId(), new HashMap<String, Toponym>());
            for (int i = 1; i < names.size(); i++) {
                Toponym tempToponym = null;
                Double tempDistance = 100000d;
                ToponymSearchResult otherResult = results.get(names.get(i));
                for (Toponym otherToponym : otherResult.getToponyms()) {
                    try {
                        Double dist = distance(toponym.getLatitude(), toponym.getLongitude(), otherToponym.getLatitude(), otherToponym.getLongitude());
                        if (otherToponym.getFeatureCode() != null && !otherToponym.getFeatureCode().trim().equals("") && Analyze.scalesGeonames.containsKey(otherToponym.getFeatureCode()))
                            dist = dist / (Analyze.scalesGeonames.get(otherToponym.getFeatureCode()));

                        if (tempDistance > dist) {
                            tempDistance = dist;
                            tempToponym = otherToponym;
                        }
                    } catch (Throwable t) {
                        System.out.println("Error!");
                    }
                }
                if (toponym.getFeatureCode() != null && !toponym.getFeatureCode().trim().equals("") && Analyze.scalesGeonames.containsKey(toponym.getFeatureCode()))
                    tempDistance = tempDistance / (Analyze.scalesGeonames.get(toponym.getFeatureCode()));
                minDistances.get(toponym.getGeoNameId()).put(names.get(i), tempDistance);
                minDistanceToponym.get(toponym.getGeoNameId()).put(names.get(i), tempToponym);
            }
        }
        //sum of all distances
        for (Integer id : minDistances.keySet()) {
            Double sumDists = 0d;
            for (Double d : minDistances.get(id).values()) {
                sumDists += d;
            }
            sumDistances.put(id, sumDists);
        }

        //choosing best reference
        Double minDistance = null;
        Integer id = null;
        for (Integer identity : sumDistances.keySet()) {
            if (minDistance == null || minDistance > sumDistances.get(identity)) {
                id = identity;
                minDistance = sumDistances.get(id);
            }
        }
//        LOG.info("Best candidate ID: " + id + "\tSum Distances: " + minDistance);
//        if (minDistance/names.size() > 40)
//            throw new Exception("Not valid!");
        Analyze.distances.add(minDistance);
        for (Toponym t : reference.getToponyms()) {
            if (id == t.getGeoNameId()) {
                finalResults.put(names.get(0), t);
                break;
            }
        }
        for (String name : minDistanceToponym.get(id).keySet()) {
            finalResults.put(name, minDistanceToponym.get(id).get(name));
        }
        return finalResults;
    }

    private static double distance(double refLat, double refLng, double otherLat, double otherLng) {
        return Math.sqrt(Math.pow(refLat - otherLat, 2) + Math.pow(refLng - otherLng, 2));
    }

    public static void main(String[] args) throws Throwable {
//        String ids = "1231-21312-3423423-2345-23213-43535-";
//        String[] idSplit = ids.replaceAll("-", " ").trim().split(" ");
        WebService.setUserName("e_[anonymous]"); //setting for geonames account!

        ToponymSearchResult T1 = Search.simpleSearch("England");
        ToponymSearchResult T2 = Search.simpleSearch("Warren Avenue");
        ToponymSearchResult T3 = Search.simpleSearch("Dover Pennsylvania");
        ToponymSearchResult T4 = Search.simpleSearch("Guguh Mountain");
        ToponymSearchResult T5 = Search.simpleSearch("Alamo Plaza District");
        ToponymSearchResult T6 = Search.simpleSearch("Harrsiburg Pike");


        ToponymSearchResult BT = Search.searchNamesEqual("South China");
        ToponymExportModel bt = new ToponymExportModel();
        bt.setResult(BT);
        bt.setName("South China");
        ToponymSearchResult TX = Search.searchNamesEqual("Guangdong");
        ToponymExportModel tx = new ToponymExportModel();
        tx.setResult(TX);
        tx.setName("Guangdong");
//        ToponymSearchResult AM = Search.search("America");
//        ToponymExportModel am = new ToponymExportModel();
//        am.setName("America");
//        am.setResult(AM);
        List<String> names = new ArrayList<>();
        names.add("South China");
        names.add("Guangdong");
        //names.add("America");

        Map<String, ToponymExportModel> results = new HashMap<>();
        results.put("South China", bt);
        results.put("Guangdong", tx);
        //results.put("America", am);

        Map<String, Toponym> res = Disambiguation.disambiguate(names, results);

        System.out.println("Boom!");
    }

    public static void GeneratePermutations(List<List<Toponym>> lists, List<Toponyms> result, int depth, String current) {
        if (depth == lists.size()) {
            Toponyms t = new Toponyms();
            t.construct(current, lists);
            result.add(t);
            return;
        }

        for (int i = 0; i < lists.get(depth).size(); ++i) {
            GeneratePermutations(lists, result, depth + 1, current + lists.get(depth).get(i).getGeoNameId() + "-");
        }
    }

    private static ToponymExportModel validate(ToponymExportModel e) {
        ToponymExportModel result = new ToponymExportModel();
        String name = e.getName();
        result.setName(name);
        ToponymSearchResult searchResult = e.getResult();
        SortedMap<Integer, List<Toponym>> relevant = new TreeMap<>(Collections.reverseOrder());
        for (Toponym t : searchResult.getToponyms()) {
            Integer value = isNameOK(name, t.getName());
            if (!relevant.keySet().contains(value))
                relevant.put(value, new ArrayList<Toponym>());
            List<Toponym> list = relevant.get(value);
            list.add(t);
            relevant.put(value, list);
        }
        if (relevant.size() == 0)
            return e;
        List<Toponym> searchData = new ArrayList<>();
        for (Toponym t : relevant.get(relevant.firstKey()))
            if (isValid(t))
                searchData.add(t);
        if (searchData.size() != 0)
            searchResult.setToponyms(searchData);
        else
            searchResult.setToponyms(relevant.get(relevant.firstKey()));
        searchResult.setTotalResultsCount(searchResult.getToponyms().size());
        result.setResult(searchResult);
        return result;
    }

    private static Integer isNameOK(String refName, String name) {
        if (refName.trim().toLowerCase().equals(name.trim().toLowerCase()))
            return 100;
        if (refName.trim().toLowerCase().contains(name.trim().toLowerCase()))
            return 50;
        if (refName.trim().contains(" ") || name.trim().contains(" ")) {
            String[] refSplit = refName.split(" ");
            String[] nameSplit = name.split(" ");
            int value = 0;
            for (String r : refSplit)
                for (String n : nameSplit)
                    if (r.toLowerCase().trim().equals(n.toLowerCase().trim()))
                        value += 5;
            return value - (Math.abs(refName.length() - name.length()));
        }
        return 0;
    }

    private static boolean isValid(Toponym t) {
        return !invalidFeatureCode.contains(t.getFeatureCode());
    }
}
