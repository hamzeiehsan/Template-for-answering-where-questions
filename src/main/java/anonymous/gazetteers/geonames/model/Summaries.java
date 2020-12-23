package anonymous.gazetteers.geonames.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class Summaries implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(Summaries.class);
    private SummaryModel all;
    private SummaryModel questions;

    public SummaryModel getAll() {
        return all;
    }

    public void setAll(SummaryModel all) {
        this.all = all;
        LOG.info("All summaries: \nPlace names: "+this.all.getPlaceNames().size()+"\tPlaceType: "+this.all.getPlaceTypes().size()+"\tQualities: "+this.all.getQualities().size());
    }

    public SummaryModel getQuestions() {
        return questions;
    }

    public void setQuestions(SummaryModel questions) {
        this.questions = questions;
        LOG.info("Question summaries: \nPlace names: "+this.questions.getPlaceNames().size()+"\tPlaceType: "+this.questions.getPlaceTypes().size()+"\tQualities: "+this.questions.getQualities().size());
    }

    public SummaryModel getAnswers() {
        return answers;
    }

    public void setAnswers(SummaryModel answers) {
        this.answers = answers;
        LOG.info("Answer summaries: \nPlace names: "+this.answers.getPlaceNames().size()+"\tPlaceType: "+this.answers.getPlaceTypes().size()+"\tQualities: "+this.answers.getQualities().size());
    }

    private SummaryModel answers;
}
