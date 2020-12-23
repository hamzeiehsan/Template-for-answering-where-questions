package anonymous.dataset.model.basic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TripletAnswer {
    private String question;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public Map<String, List<String>> getAnalysedAnswers() {
        return AnalysedAnswers;
    }

    public void setAnalysedAnswers(Map<String, List<String>> analysedAnswers) {
        AnalysedAnswers = analysedAnswers;
    }

    private Map<String, List<String>> AnalysedAnswers = new HashMap<>();

    @Override
    public String toString() {
        String str = "-------------------\nQuestion:\t"+question;
        for (String answer : getAnalysedAnswers().keySet()) {
            str += "\nAnswer: " + answer + "\tTriplets: [";
            for (String t : getAnalysedAnswers().get(answer))
                str+= t+", ";
            str += "]";
        }
        return str;
    }
}
