package anonymous.gazetteers.geonames.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SummaryModel implements Serializable {
    private Map<String, Integer> placeNames = new HashMap<>();
    private Map<String, Integer> placeTypes = new HashMap<>();
    private Map<String, Integer> qualities = new HashMap<>();
    private Map<String, Integer> activities = new HashMap<>();
    private Map<String, Integer> spatialRelations = new HashMap<>();
    private Map<String, Integer> situations = new HashMap<>();

    public Map<String, Integer> getActivities() {
        return activities;
    }

    public void setActivities(Map<String, Integer> activities) {
        this.activities = activities;
    }

    public void addActivity(String activity) {
        add(activities, activity);
    }

    public void addAllActivities(List<String> acts) {
        addAll(activities, acts);
    }

    public Map<String, Integer> getSpatialRelations() {
        return spatialRelations;
    }

    public void setSpatialRelations(Map<String, Integer> spatialRelations) {
        this.spatialRelations = spatialRelations;
    }

    public void addSpatialRelation(String spatialRelation) {
        add(spatialRelations, spatialRelation);
    }

    public void addAllSpatialRelations(List<String> relations) {
        addAll(spatialRelations, relations, true);
    }

    public Map<String, Integer> getPlaceNames() {
        return placeNames;
    }

    public void setPlaceNames(Map<String, Integer> placeNames) {
        this.placeNames = placeNames;
    }

    public void addPlaceName(String placeName) {
        add(placeNames, placeName);
    }

    public void addAllPlaceName(List<String> pNs) {
        addAll(placeNames, pNs);
    }

    public Map<String, Integer> getPlaceTypes() {
        return placeTypes;
    }

    public void setPlaceTypes(Map<String, Integer> placeTypes) {
        this.placeTypes = placeTypes;
    }

    public void addPlaceType(String placeType) {
        add(placeTypes, placeType);
    }

    public void addAllPlaceType(List<String> pTs) {
        addAll(placeTypes, pTs);
    }

    public Map<String, Integer> getQualities() {
        return qualities;
    }

    public void setQualities(Map<String, Integer> qualities) {
        this.qualities = qualities;
    }

    public void addQuality(String quality) {
        add(qualities, quality);
    }

    public void addAllQuality(List<String> qs) {
        addAll(qualities, qs);
    }

    private void add(Map<String, Integer> map, String value) {
        if (!map.containsKey(value))
            map.put(value, 0);
        map.put(value, map.get(value) + 1);
    }

    private void addAll(Map<String, Integer> map, List<String> values) {
        for (String value : values)
            add(map, value);
    }

    private void addAll(Map<String, Integer> map, List<String> values, boolean split) {
        for (String value : values) {
            if (split)
                value = value.split(" ")[0].trim();
            add(map, value);
        }
    }

    public Map<String, Integer> getSituations() {
        return situations;
    }

    public void setSituations(Map<String, Integer> situations) {
        this.situations = situations;
    }

    public void addAllSituations(List<String> sits) {
        addAll(situations, sits);
    }

    public void addSituation(String situation) {
        add(situations, situation);
    }
}
