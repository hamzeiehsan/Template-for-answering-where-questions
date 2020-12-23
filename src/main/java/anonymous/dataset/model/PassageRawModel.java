package anonymous.dataset.model;

public class PassageRawModel {
    int is_selected;
    String passage_text;

    public int getIs_selected() {
        return is_selected;
    }

    public void setIs_selected(int is_selected) {
        this.is_selected = is_selected;
    }

    public String getPassage_text() {
        return passage_text;
    }

    public void setPassage_text(String passage_text) {
        this.passage_text = passage_text;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    String url;
}
