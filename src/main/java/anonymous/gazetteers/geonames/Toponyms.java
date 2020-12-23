package anonymous.gazetteers.geonames;

import org.geonames.Toponym;

import java.util.ArrayList;
import java.util.List;

public class Toponyms implements Comparable<Toponyms> {
    public List<Toponym> getToponyms() {
        return toponyms;
    }

    private Double distance = 0d;

    public void setToponyms(List<Toponym> toponyms) {
        this.toponyms = toponyms;
    }

    public void construct (String ids, List<List<Toponym>> topos ) {
        String[] idSplit = ids.replaceAll("-", " ").trim().split(" ");
        for (String idStr : idSplit) {
            Integer id = Integer.parseInt(idStr);
            toposLabel:
            for (List<Toponym> ts : topos) {
                tsLabel:
                for (Toponym t : ts)
                    if (t.getGeoNameId() == id) {
                        this.add(t);
                        break toposLabel;
                    }
            }
        }
    }

    public void populateDistance () {
        Double d = 0d;
        try {
            for (int i = 0; i < this.toponyms.size(); i++) {
                for (int j = i; j < this.toponyms.size(); j++) {
                    Toponym t1 = this.toponyms.get(i);
                    Toponym t2 = this.toponyms.get(j);
                    double dist = distance(t1.getLatitude(), t1.getLongitude(), t2.getLatitude(), t2.getLongitude());
                    if (t1.getFeatureCode() != null && t2.getFeatureCode()!=null && !t1.getFeatureCode().equals("") && !t2.getFeatureCode().equals("") && Analyze.scalesGeonames.containsKey(t1.getFeatureCode()) && Analyze.scalesGeonames.containsKey(t2.getFeatureCode()))
                        d += dist / (Math.abs(Analyze.scalesGeonames.get(t1.getFeatureCode()) + Analyze.scalesGeonames.get(t2.getFeatureCode())));
                    else
                        d += dist;
                }
            }
        } catch (Throwable t) {
            System.out.println("Error!!");
        }
        this.distance = d;
    }

    private List<Toponym> toponyms = new ArrayList<>();

    public void add(Toponym t){
        toponyms.add(t);
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public boolean find (Integer geonameID) {
        for (Toponym t : toponyms)
            if (t.getGeoNameId() == geonameID)
                return true;
        return false;
    }

    @Override
    public int hashCode() {
        int code = this.toponyms.size();
        for (Toponym t : toponyms)
            code += t.getGeoNameId();
        return code;
    }

    @Override
    public int compareTo(Toponyms toponyms) {
        if (this.getToponyms().size() == toponyms.getToponyms().size() && Math.abs(this.getDistance()-toponyms.getDistance()) < 0.0000001) {
            for (Toponym t : toponyms.getToponyms())
                if (this.find(t.getGeoNameId()) == false)
                    return -1;
            return 0;
        }
        if (this.getDistance() < toponyms.getDistance())
            return -1;
        else
            return 1;
    }

    private double distance(double refLat, double refLng, double otherLat, double otherLng) {
        return Math.sqrt(Math.pow(refLat - otherLat, 2) + Math.pow(refLng - otherLng, 2));
    }
}
