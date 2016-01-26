package cat.mrtxema.crispetes.model;


public class Episode {
    private final int number;
    private final String title;

    public Episode(int number, String title) {
        this.number = number;
        this.title = title;
    }

    public int getNumber() {
        return number;
    }

    public String getTitle() {
        return title;
    }

    public String toString() {
        return String.format("%d - %s", number, title);
    }
}
