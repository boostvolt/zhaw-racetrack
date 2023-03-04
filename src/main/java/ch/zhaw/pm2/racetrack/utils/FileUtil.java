package ch.zhaw.pm2.racetrack.utils;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * A utility class containing static methods for performing actions on files.
 */
public final class FileUtil {

    private static final String TXT_FILE_ENDING = "txt";

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private FileUtil() {
    }

    /**
     * Evaluates if the given File object is indeed a file having the file type TXT.
     *
     * @param file the file to evaluate
     * @return true if the given File is a file and ends with TXT. False otherwise.
     * @throws NullPointerException if the provided {@link File} to be checked is null
     */
    public static boolean isTxtFile(final File file) throws NullPointerException {
        return requireNonNull(file).isFile()
            && file.getName().endsWith(TXT_FILE_ENDING);
    }

    /**
     * Creates a scanner object with the given file in the correct encoding.
     *
     * @param file the file to be read
     * @return a scanner object ready to read the file contents
     * @throws IOException if an I/O error occurs opening the source
     */
    public static Scanner createScanner(final File file) throws IOException {
        return new Scanner(file, UTF_8);
    }

}
