package foodchain.io;

import java.io.IOException;

/**
 * Exception thrown when the save file is corrupted or invalid.
 */
public class InvalidSaveFormatException extends IOException {
    public InvalidSaveFormatException(String message) { super(message); }
    public InvalidSaveFormatException(String message, Throwable cause) { super(message, cause); }
}