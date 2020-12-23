package anonymous.dataset.model.basic;

/**
 * Created by [anonymous] [anonymous] on 5/7/2017.
 */
public class BriefDataModel {
    private String id, query, queryType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    @Override
    public String toString() {
        return "id: " + id + "\tquery: " + query + "\tquery_type: " + queryType;
    }
}
