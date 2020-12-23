package anonymous.predict.performance;

import anonymous.predict.scenario.Scenario;

import java.util.*;

public class Stat {
    private Scenario scenario;
    private Map<String, SortedMap<Integer, Integer>> success = new HashMap<>();
    private Map<String, SortedMap<Integer, Integer>> fail = new HashMap<>();
    private Map<String, SortedMap<Integer, Double>> accuracy = new HashMap<>();

    public Scenario getScenario() {
        return scenario;
    }

    public void setScenario(Scenario scenario) {
        this.scenario = scenario;
    }

    public Map<String, SortedMap<Integer, Integer>> getSuccess() {
        return success;
    }

    public void setSuccess(Map<String, SortedMap<Integer, Integer>> success) {
        this.success = success;
    }

    public Map<String, SortedMap<Integer, Double>> getAccuracy() {
        if (accuracy == null || accuracy.size() == 0) {
            for (String method : success.keySet()) {
                for (Integer idx : success.get(method).keySet()) {
                    Integer fails = 0;
                    if (fail != null && fail.containsKey(method) && fail.get(method).containsKey(idx)) {
                        fails = this.fail.get(method).get(idx);
                    }
                    if (!accuracy.containsKey(method))
                        accuracy.put(method, new TreeMap<>());
                    Integer successes = success.get(method).get(idx);
                    accuracy.get(method).put(idx, ((double) successes)/((double) (successes + fails)));
                }
            }
        }
        return accuracy;
    }

    public void addSuccess(String method, Integer numOfGuesses) {
        if (!this.success.keySet().contains(method))
            this.success.put(method, new TreeMap<>());
        if (!this.success.get(method).keySet().contains(numOfGuesses))
            this.success.get(method).put(numOfGuesses, 0);
        this.success.get(method).put(numOfGuesses, this.success.get(method).get(numOfGuesses)+1);
    }

    public Map<String, SortedMap<Integer, Integer>> getFail() {
        return fail;
    }

    public void setFail(Map<String, SortedMap<Integer, Integer>> fail) {
        this.fail = fail;
    }

    public void addFail(String method, Integer numOfGuesses) {
        if (!this.fail.keySet().contains(method))
            this.fail.put(method, new TreeMap<>());
        if (!this.fail.get(method).keySet().contains(numOfGuesses))
            this.fail.get(method).put(numOfGuesses, 0);
        this.fail.get(method).put(numOfGuesses, this.fail.get(method).get(numOfGuesses)+1);
    }

    @Override
    public String toString() {
        getAccuracy();
        StringBuilder builder = new StringBuilder();
        builder.append('\n');
        builder.append("#guesses\t");
        SortedSet<String> methods = new TreeSet<>();
        methods.addAll(accuracy.keySet());
        for (String method : methods) {
//            if (!method.equals("MFP") && !method.equals("Random")) {//baselines...
                if (method.length() < 4)
                    method += "\t";
                builder.append(method + "\t");
//            }
        }
        builder.append('\n');
        for (int i=1; i < 6; i++) {
            builder.append(i+"\t\t\t");
            for (String method : methods) {
                Double acc = accuracy.get(method).get(i)*100;
                builder.append(String.format("%.2f", acc)+"\t");
            }
            builder.append('\n');
        }
        return builder.toString();
    }
}
