package anonymous.gazetteers.geonames.model;

import java.io.Serializable;

public class CodeModel implements Serializable {
    private String value;
    private Long identifier;
    private String code;

    public CodeModel() {
        //Default constructor!
    }

    public CodeModel(Long identifier, String value, String code) {
        this.identifier = identifier;
        this.value = value;
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Long identifier) {
        this.identifier = identifier;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

}
