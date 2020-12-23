package anonymous.parser.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ResultModel implements Serializable
{

    @SerializedName("identifier")
    @Expose
    private Long identifier;
    @SerializedName("query")
    @Expose
    private String query;
    @SerializedName("queryType")
    @Expose
    private String queryType;
    @SerializedName("answers")
    @Expose
    private List<String> answers = new ArrayList<String>();
    @SerializedName("queryId")
    @Expose
    private Long queryId;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("queryAnalyze")
    @Expose
    private QueryAnalyze queryAnalyze;
    @SerializedName("answersAnalyze")
    @Expose
    private List<AnswersAnalyze> answersAnalyze = new ArrayList<AnswersAnalyze>();
    private final static long serialVersionUID = -5297688524727371407L;

    public Long getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Long identifier) {
        this.identifier = identifier;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getQueryType() {
        return queryType;
    }

    public void setQueryType(String queryType) {
        this.queryType = queryType;
    }

    public List<String> getAnswers() {
        return answers;
    }

    public void setAnswers(List<String> answers) {
        this.answers = answers;
    }

    public Long getQueryId() {
        return queryId;
    }

    public void setQueryId(Long queryId) {
        this.queryId = queryId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public QueryAnalyze getQueryAnalyze() {
        return queryAnalyze;
    }

    public void setQueryAnalyze(QueryAnalyze queryAnalyze) {
        this.queryAnalyze = queryAnalyze;
    }

    public List<AnswersAnalyze> getAnswersAnalyze() {
        return answersAnalyze;
    }

    public void setAnswersAnalyze(List<AnswersAnalyze> answersAnalyze) {
        this.answersAnalyze = answersAnalyze;
    }
}
