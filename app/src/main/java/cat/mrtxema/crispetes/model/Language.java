package cat.mrtxema.crispetes.model;


public class Language {
    private final String code;
    private final String name;

    public Language(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
