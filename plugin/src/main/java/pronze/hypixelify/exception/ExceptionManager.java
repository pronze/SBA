package pronze.hypixelify.exception;

import org.jetbrains.annotations.NotNull;
import pronze.hypixelify.api.exception.ExceptionHandler;

public class ExceptionManager {
    private volatile ExceptionHandler handler;

    public ExceptionManager() {
        handler = (Throwable::printStackTrace);
    }

    public synchronized void setExceptionHandler(@NotNull ExceptionHandler handler) {
        this.handler = handler;
    }

    public synchronized void handleException(Exception ex) {
        handler.handleException(ex);
    }
}
