package anonymous.dataset.model.basic;

import java.util.*;

public class SemanticCategoriesInstances {
    private static SemanticCategoriesInstances instance;

    public static void setInstance(SemanticCategoriesInstances instance) {
        SemanticCategoriesInstances.instance = instance;
    }

    public SortedMap<String, Map<String, Integer>> getData() {
        return data;
    }

    private Integer getThreshold (Map<String, Integer> value) {
        if (value.size() <= 50)
            return -1;
        SortedMap<Integer, String> temp = new TreeMap<>(Collections.reverseOrder());
        for (String str : value.keySet()) {
            temp.put(value.get(str), str);
        }
        Object[] keys = temp.keySet().toArray();
        Integer result = 1;
        if (keys.length > 60)
            result = (Integer) keys[55];
        else if (keys.length > 45)
            result = (Integer) keys[35];
        else if (keys.length > 35)
            result = (Integer) keys[25];
        else if (keys.length > 25)
            result = (Integer) keys[15];
        else if (keys.length > 15)
            result = (Integer) keys[5];
        return result;
    }

    public SortedMap<String, Map<String, Integer>> getFrequentData() {
        SortedMap<String, Map<String, Integer>> frequentData = new TreeMap<>();
        for (String key : data.keySet()) {
            Map<String, Integer> value = data.get(key);
            Integer threshold = getThreshold(value);
            frequentData.put(key, new HashMap<String, Integer>());
            for (String k : value.keySet()) {
                if (value.get(k) >= threshold)
                    frequentData.get(key).put(k, value.get(k));
            }
        }
        return frequentData;
    }

    private SortedMap<String, Map<String, Integer>> data;

    private SemanticCategoriesInstances() {
        data = new TreeMap<>();
    }

    public static SemanticCategoriesInstances getInstance() {
        if (instance == null)
            instance = new SemanticCategoriesInstances();
        return instance;
    }

    public void addData(String type, String value) {
        if (!this.data.containsKey(type))
            this.data.put(type, new HashMap<String, Integer>());
        if (this.data.get(type).containsKey(value))
            this.data.get(type).put(value, this.data.get(type).get(value) + 1);
        else
            this.data.get(type).put(value, 1);
    }

    public void updatePQA() {
        for (String key : data.keySet()) {
            if(key.startsWith("P")) {
                String equivalentNP = "NP-"+key.charAt(2);
                for (String word : data.get(key).keySet()) {
                    if (data.get(equivalentNP).containsKey(word)) {
                        Integer difference = data.get(equivalentNP).get(word)*5068/96955;
                        data.get(key).put(word, data.get(key).get(word)-difference);
                    }
                }
            }
        }
    }
}