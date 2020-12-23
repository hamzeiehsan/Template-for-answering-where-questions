package anonymous.predict.scenario;

import java.util.*;

public class Scenario {
    private boolean onlyContent, onlySameClassPrediction;
    private String predictionClass;
    private String questionType;

    private Map<String, Map<Integer, Double>> methodPerformances = new HashMap<>();

    public static List<Scenario> generateAllScenarios() {
        List<Scenario> scenarios = new LinkedList<>();

        String[] predictionClassses = {"type", "scale", "prominence"};
        String[] questionTypes = {"all", "SWQs", "DWQs"};
        Boolean[] onlyContent = {true, false};
        Boolean[] onlySameClass = {true, false};

        for (String cl : predictionClassses) {
            for (Boolean content : onlyContent) {
                for (Boolean sameCl : onlySameClass) {
                    for (String qType : questionTypes) {
                        Scenario s = new Scenario();
                        s.setOnlyContent(content);
                        s.setOnlySameClassPrediction(sameCl);
                        s.setPredictionClass(cl);
                        s.setQuestionType(qType);
                        scenarios.add(s);
                    }
                }
            }
        }
        return scenarios;
    }

    public boolean isOnlyContent() {
        return onlyContent;
    }

    public void setOnlyContent(boolean onlyContent) {
        this.onlyContent = onlyContent;
    }

    public boolean isOnlySameClassPrediction() {
        return onlySameClassPrediction;
    }

    public void setOnlySameClassPrediction(boolean onlySameClassPrediction) {
        this.onlySameClassPrediction = onlySameClassPrediction;
    }

    public String getPredictionClass() {
        return predictionClass;
    }

    public void setPredictionClass(String predictionClass) {
        this.predictionClass = predictionClass;
    }

    @Override
    public String toString() {
        return "Scenario: \nPrediction class: {" + this.predictionClass +
                "}\nOnly content: {" + this.onlyContent +
                "}\nOnly same class prediction: {" + this.onlySameClassPrediction +
                "}\nQuestion Type: {" + this.questionType +
                "}";
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public void addMethodPerformance(String method, Integer numberOfGuesses, Double accuracy) {
        if (!this.methodPerformances.containsKey(method))
            this.methodPerformances.put(method, new HashMap<>());
        this.methodPerformances.get(method).put(numberOfGuesses, accuracy);
    }

    public void addMethodPerformance(String method, SortedMap<Integer, Double> accuracies) {
        for (int key : accuracies.keySet())
            addMethodPerformance(method, key, accuracies.get(key));
    }

    public Map<String, Map<Integer, Double>> getMethodPerformances() {
        return methodPerformances;
    }

    public String getCSVResults(Integer id) {
        StringBuilder builder = new StringBuilder();
        for (String method : getMethodPerformances().keySet()) {
            for (Integer numOfGuesses : getMethodPerformances().get(method).keySet()) {
                builder.append(id);
                builder.append(',');
                builder.append(this.predictionClass);
                builder.append(',');
                builder.append(this.questionType);
                builder.append(',');
                if (isOnlySameClassPrediction())
                    builder.append(this.questionType);
                else
                    builder.append("TSP");
                builder.append(',');
                if (isOnlyContent())
                    builder.append("Content");
                else
                    builder.append("Content+Style");
                builder.append(',');
                builder.append(numOfGuesses);
                builder.append(',');
                builder.append(method);
                builder.append(',');
                builder.append(this.getMethodPerformances().get(method).get(numOfGuesses));
                builder.append(',');
                Double best = this.getBestPerformance(numOfGuesses);
                builder.append(best);
                builder.append(',');
                builder.append(this.getMethodPerformances().get(method).get(numOfGuesses) - best);
                builder.append('\n');
            }
        }
        return builder.toString();
    }

    private Double getBestPerformance(int numOfGuesses) {
        Double best = 0d;
        for (String method : getMethodPerformances().keySet()) {
            if (!method.equals("Random") && !method.equals("MFP")) {
                Double performance = getMethodPerformances().get(method).get(numOfGuesses);
                if (performance > best)
                    best = performance;
            }
        }
        return best*100;
    }

    public Map<String, Double> getMethodSEs() {
        Map<String, Double> squaredErrors = new HashMap<>();
        for (String method : getMethodPerformances().keySet()) {
            if (!method.equals("Random") && !method.equals("MFP")) {
                Double squaredError = 0d;
                for (Integer numOfGuesses : getMethodPerformances().get(method).keySet()) {
                    Double best = getBestPerformance(numOfGuesses);
                    squaredError += Math.pow(getMethodPerformances().get(method).get(numOfGuesses)*100-best, 2);
                }
                squaredErrors.put(method, squaredError);
            }
        }
        return squaredErrors;
    }
}
