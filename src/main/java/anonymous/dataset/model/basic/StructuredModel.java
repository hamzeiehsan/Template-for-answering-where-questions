package anonymous.dataset.model.basic;

import java.util.List;

public class StructuredModel {
    private List<String> pos, generalPOS;
    private int numberOfSentences = 0;

    public StructuredModel(String text) {

    }

    public List<String> getGeneralPOS() {
        return generalPOS;
    }

    public void setGeneralPOS(List<String> generalPOS) {
        this.generalPOS = generalPOS;
    }

    public List<String> getPos() {
        return pos;
    }

    public void setPos(List<String> pos) {
        this.pos = pos;
    }
}
