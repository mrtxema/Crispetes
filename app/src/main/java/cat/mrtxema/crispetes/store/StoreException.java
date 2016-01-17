package cat.mrtxema.crispetes.store;

public class StoreException extends Exception {

    public StoreException(String message, Throwable t) {
        super(message, t);
    }

    public StoreException(String message) {
        super(message);
    }
}
