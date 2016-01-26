package cat.mrtxema.crispetes.model;


public class Season {
    private final int number;

    public Season(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public String toString() {
        return "Temporada " + number;
    }
}
