package nl.nlnetlabs.bgpsym01.primitives.types;

import nl.nlnetlabs.bgpsym01.primitives.BGPSymException;

public class NotImplementedException extends BGPSymException {

    /**
     * 
     */
    private static final long serialVersionUID = -7694387379510569081L;

    public NotImplementedException() {
        super();
    }

    public NotImplementedException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotImplementedException(String message) {
        super(message);
    }

    public NotImplementedException(Throwable cause) {
        super(cause);
    }

}
