package cat.mrtxema.crispetes.service;

public class NavigationException extends Exception {

    public NavigationException(String message) {
        super(message);
    }

    public NavigationException(String message, Throwable cause) {
        super(message, cause);
    }

}
