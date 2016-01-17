package cat.mrtxema.crispetes.service;


public class MovieServiceException extends Exception {

    public MovieServiceException(String message) {
        super(message);
    }

    public MovieServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
