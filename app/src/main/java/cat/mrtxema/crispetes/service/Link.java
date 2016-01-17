package cat.mrtxema.crispetes.service;


public class Link {
    private final String id;
    private final Language language;
    private final String server;
    private final String videoQuality;
    private final String audioQuality;

    public Link(String id, String server, Language language, String videoQuality, String audioQuality) {
        this.id = id;
        this.server = server;
        this.language = language;
        this.videoQuality = videoQuality;
        this.audioQuality = audioQuality;
    }

    public String getId() {
        return id;
    }

    public String getServer() {
        return server;
    }

    public Language getLanguage() {
        return language;
    }

    public String getVideoQuality() {
        return videoQuality;
    }

    public String getAudioQuality() {
        return audioQuality;
    }

    public String toString() {
        return String.format("%s (%s)", server, language.getName());
    }
}
