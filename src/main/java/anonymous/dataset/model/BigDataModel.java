package anonymous.dataset.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BigDataModel {
    Map<String, List<String>> answers = new HashMap<>();
    Map<String, String> query =new HashMap<>();
    Map<String, Long> query_id = new HashMap<>();
    Map<String, String> query_type = new HashMap<>();

    public Map<String, List<String>> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<String, List<String>> answers) {
        this.answers = answers;
    }


    public Map<String, String> getQuery() {
        return query;
    }

    public void setQuery(Map<String, String> query) {
        this.query = query;
    }

    public Map<String, Long> getQuery_id() {
        return query_id;
    }

    public void setQuery_id(Map<String, Long> query_id) {
        this.query_id = query_id;
    }

    public Map<String, String> getQuery_type() {
        return query_type;
    }

    public void setQuery_type(Map<String, String> query_type) {
        this.query_type = query_type;
    }
}
