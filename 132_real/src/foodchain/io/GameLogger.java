package foodchain.io;

import java.io.IOException;
import java.nio.file.*;

/**
 * Handles writing game events to a log file.
 * Appends new entries to the end of the file.
 */
public class GameLogger {
    private final Path file;

    /**
     * Creates a logger for the specified file path.
     * @param file The path to the log file.
     */
    public GameLogger(Path file) {
        this.file=file;
    }

    /**
     * Writes a message to the log file followed by a new line.
     * @param msg The message to record.
     */
    public void log(String msg) {
                try {
            Files.writeString(file,msg+System.lineSeparator(),StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}