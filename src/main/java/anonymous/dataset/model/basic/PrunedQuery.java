package anonymous.dataset.model.basic;

public class PrunedQuery {
    private String query, prunedQuery;

    public PrunedQuery(String query, String prunedQuery) {
        this.query = query;
        this.prunedQuery = prunedQuery;
    }

    public String getPrunedQuery() {
        return prunedQuery;
    }

    public void setPrunedQuery(String prunedQuery) {
        this.prunedQuery = prunedQuery;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
