package anonymous.gazetteers.geonames.model;

import java.io.Serializable;
import java.util.*;

public class QuestionAnswerScale implements Serializable {
    private Long identifier;
    private Map<Integer, Integer> scalesInQuestion = new HashMap<>();
    private LinkedList<Map<Integer, Integer>> scalesInAnswers = new LinkedList<>();

    public Long getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Long identifier) {
        this.identifier = identifier;
    }

    public Map<Integer, Integer> getScalesInQuestion() {
        return scalesInQuestion;
    }

    public void setScalesInQuestion(Map<Integer, Integer> scalesInQuestion) {
        this.scalesInQuestion = scalesInQuestion;
    }

    public LinkedList<Map<Integer, Integer>> getScalesInAnswers() {
        return scalesInAnswers;
    }

    public void setScalesInAnswers(LinkedList<Map<Integer, Integer>> scalesInAnswers) {
        this.scalesInAnswers = scalesInAnswers;
    }

    public Set<Integer> getScales() {
        return scales;
    }

    public void setScales(Set<Integer> scales) {
        this.scales = scales;
    }

    private Set<Integer> scales = new HashSet<>();

    public void addScale (int scale) {
        this.scales.add(scale);
    }

    public void addQuestionScale (int location, int scale) {
        this.scalesInQuestion.put(location, scale);
        this.addScale(scale);
    }


    public void addAnswerScale(Integer scale, Integer location, Integer level) {
        scalesInAnswers.get(level).put(location, scale);
    }


    public Integer getAScaleUnique () {
        Set<Integer> temp = new HashSet<>();
        for (Map<Integer, Integer> scalesInAnswer : scalesInAnswers)
            temp.addAll(scalesInAnswer.values());
        return temp.size();
    }

    public Integer getQScaleUnique () {
        Set<Integer> temp = new HashSet<>();
        temp.addAll(scalesInQuestion.values());
        return temp.size();
    }

    public void createAnswerLevel(Integer level) {
        scalesInAnswers.add(level, new HashMap<Integer, Integer>());
    }


}
