package anonymous.dataset.model.basic;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by [anonymous] [anonymous] on 5/4/2017.
 */
public class DataModel {
    private static int counter = 0;
    private int identifier;
    private String query, queryType;
    private List<String> passages;
    private List<String> answers = new ArrayList<>();
    private Long queryId;
    private String id;

    public DataModel() {
        this.id = UUID.randomUUID().toString();
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

    public List<String> getPassages() {
        return passages;
    }

    public void setPassages(List<String> passages) {
        this.passages = passages;
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

    @Override
    public String toString() {
        return "id: " + id + "\tquery: " + query + "\tquery_type: " + queryType;
    }

    public List<String> getAnswers() {
        return answers;
    }

    public void setAnswers(List<String> answers) {
        this.answers = answers;
    }
}
