package pronze.hypixelify.api.exception;

@FunctionalInterface
public interface ExceptionHandler {
    /**
     *
     * @param exception
     */
    void handleException(Exception exception);
}

