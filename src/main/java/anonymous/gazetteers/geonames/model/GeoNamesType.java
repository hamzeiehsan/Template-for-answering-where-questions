package anonymous.gazetteers.geonames.model;

public class GeoNamesType {
    private String CODE, NAME, DEF, GENERAL_CODE;

    public String getCODE() {
        return CODE;
    }

    public void setCODE(String CODE) {
        this.CODE = CODE;
    }

    public String getNAME() {
        return NAME;
    }

    public void setNAME(String NAME) {
        this.NAME = NAME;
    }

    public String getDEF() {
        return DEF;
    }

    public void setDEF(String DEF) {
        this.DEF = DEF;
    }

    public String getGENERAL_CODE() {
        return GENERAL_CODE;
    }

    public void setGENERAL_CODE(String GENERAL_CODE) {
        this.GENERAL_CODE = GENERAL_CODE;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    private int level;
}
