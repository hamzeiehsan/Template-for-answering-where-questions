package anonymous.predict;

import anonymous.predict.ipredict.database.SequenceDatabase;
import anonymous.predict.ipredict.predictor.DG.DGArc;
import anonymous.predict.ipredict.predictor.Predictor;
import anonymous.predict.ipredict.predictor.TDAG.TDAGNode;

import java.util.*;

public class MapUtil {
        public static <K, V extends Comparable<? super V>> LinkedHashMap<K, V> sortByValue(Map<K, V> map) {
            List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
            list.sort(Map.Entry.comparingByValue());

            LinkedHashMap<K, V> result = new LinkedHashMap<>();
            for (Map.Entry<K, V> entry : list) {
                result.put(entry.getKey(), entry.getValue());
            }
            return result;
        }

        public static <K, V extends Comparable<? super V>> Map<K, V> getKthValues (Map<K, V> map, int k) {
            LinkedHashMap<K, V> sortedMap = sortByValue(map);
            if (k >= map.size())
                return sortedMap;
            LinkedHashMap<K, V> result = new LinkedHashMap<>();
            int counter = 0;
            if(Predictor.isTSP) {
                Map<Integer, K> keys = new HashMap<>();
                for (K key : sortedMap.keySet()) {
                    //if (counter >= map.size() - k)
                     //   result.put(key, sortedMap.get(key));
                    keys.put(counter, key);
                    counter++;
                }
                Set<String> projecteds = new HashSet<>();
                while (counter >= 0) {
                    counter--;
                    K key = keys.get(counter);
                    Integer realKey = null;
                    if (key instanceof Integer)
                        realKey = (Integer) key;
                    else if (key instanceof TDAGNode)
                        realKey = ((TDAGNode) key).symbol;
                    else if (key instanceof DGArc)
                        realKey = ((DGArc) key).dest;
                    else
                        break;
                    String val = SequenceDatabase.reverseResults.get(realKey);
                    if (!projecteds.contains(val)) {
                        projecteds.add(val);
                        result.put(key, sortedMap.get(key));
                    }
                    if (projecteds.size() == k)
                        return result;
                }
                if (counter == 0 && result.size() > 0)
                    return result;

            }
                for (K key : sortedMap.keySet()) {
                    if (counter >= map.size() - k)
                        result.put(key, sortedMap.get(key));
                    counter++;
                }

            return result;
        }

        public static void main(String[] args) {
            Map<String, Double> vals = new HashMap<>();
            vals.put("[anonymous]1", 5.1);
            vals.put("[anonymous]2", 15.1);
            vals.put("[anonymous]3", 5.12);
            vals.put("[anonymous]4", 5.31);
            vals.put("[anonymous]5", 45.1);
            vals.put("[anonymous]6", 5.61);
            vals.put("[anonymous]7", 5.81);
            vals.put("[anonymous]8", 95.1);

            Map<String, Double> result1 = getKthValues(vals, 1);
            Map<String, Double> result2 = getKthValues(vals, 2);
            Map<String, Double> result3 = getKthValues(vals, 3);
            Map<String, Double> result4 = getKthValues(vals, 4);
            Map<String, Double> result5 = getKthValues(vals, 5);
            Map<String, Double> result6 = getKthValues(vals, 6);
            Map<String, Double> result7 = getKthValues(vals, 7);
            Map<String, Double> result8 = getKthValues(vals, 8);
            Map<String, Double> result9 = getKthValues(vals, 8);
        }
}
