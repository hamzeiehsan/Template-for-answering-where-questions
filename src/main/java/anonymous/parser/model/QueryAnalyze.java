package anonymous.parser.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class QueryAnalyze implements Serializable
{

    @SerializedName("placeName")
    @Expose
    private List<String> placeName = new ArrayList<String>();
    @SerializedName("placeType")
    @Expose
    private List<String> placeType = new ArrayList<String>();
    @SerializedName("code")
    @Expose
    private String code;
    @SerializedName("sentence")
    @Expose
    private String sentence;
    @SerializedName("quality")
    @Expose
    private List<String> quality = new ArrayList<String>();
    private final static long serialVersionUID = 5520639919005716129L;

    public List<String> getActivity() {
        return activity;
    }

    public void setActivity(List<String> activity) {
        this.activity = activity;
    }

    public List<String> getSpatialRelation() {
        return spatialRelation;
    }

    public void setSpatialRelation(List<String> spatialRelation) {
        this.spatialRelation = spatialRelation;
    }

    @SerializedName("activity")
    @Expose
    private List<String> activity = new ArrayList<String>();
    @SerializedName("sp_relation")
    @Expose
    private List<String> spatialRelation = new ArrayList<String>();

    @SerializedName("stative")
    @Expose
    private List<String> situations = new ArrayList<String>();

    public List<String> getPlaceName() {
        return placeName;
    }

    public void setPlaceName(List<String> placeName) {
        this.placeName = placeName;
    }

    public List<String> getPlaceType() {
        return placeType;
    }

    public void setPlaceType(List<String> placeType) {
        this.placeType = placeType;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSentence() {
        return sentence;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    public List<String> getQuality() {
        return quality;
    }

    public void setQuality(List<String> quality) {
        this.quality = quality;
    }

    public List<String> getSituations() {
        return situations;
    }

    public void setSituations(List<String> situations) {
        this.situations = situations;
    }
}