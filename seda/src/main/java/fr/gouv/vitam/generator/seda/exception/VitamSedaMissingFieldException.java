package fr.gouv.vitam.generator.seda.exception;
/**
 * Exception when a mandatory field is missing during the SEDA generation
 */
public class VitamSedaMissingFieldException extends RuntimeException {
    /**
     * 
     * @param message
     */
    public VitamSedaMissingFieldException(String message) {
        super(message);
    }
    /**
     * 
     * @param cause
     */
    public VitamSedaMissingFieldException(Throwable cause) {
        super(cause);
    }
    /**
     * 
     * @param message
     * @param cause
     */
    public VitamSedaMissingFieldException(String message, Throwable cause) {
        super(message, cause);
    }
    /**
     * 
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public VitamSedaMissingFieldException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
