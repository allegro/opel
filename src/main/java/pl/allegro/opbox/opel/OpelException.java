package pl.allegro.opbox.opel;

public class OpelException extends RuntimeException {
    public OpelException() {
        super();
    }

    public OpelException(String msg) {
        super(msg);
    }

    public OpelException(Exception e) {
        super(e);
    }

    public OpelException(String msg, Exception e) {
        super(msg, e);
    }
}
