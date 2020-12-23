package anonymous.dataset.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class AnalysisModel implements Serializable {
    List<Nodes> nodes = new LinkedList<>();
    List<Links> links = new LinkedList<>();

    public List<Nodes> getNodes() {
        return nodes;
    }

    public void setNodes(List<Nodes> nodes) {
        this.nodes = nodes;
    }

    public List<Links> getLinks() {
        return links;
    }

    public void setLinks(List<Links> links) {
        this.links = links;
    }
}
