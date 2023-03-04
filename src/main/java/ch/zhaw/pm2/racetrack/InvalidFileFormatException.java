package ch.zhaw.pm2.racetrack;

/**
 * Used for invalid formatted Move-List files.
 */
public class InvalidFileFormatException extends Exception {

    /**
     * Constructs an InvalidFileFormatException with the specified error message.
     *
     * @param message The error message that describes the reason for the exception.
     */
    public InvalidFileFormatException(String message) {
        super(message);
    }

}
