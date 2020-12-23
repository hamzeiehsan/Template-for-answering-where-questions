package anonymous.gazetteers.geonames.model;

import java.io.Serializable;

public class CSModel implements Serializable {
    private String name, code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
