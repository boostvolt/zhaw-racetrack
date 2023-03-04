package ch.zhaw.pm2.racetrack.utils;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import org.junit.jupiter.api.Test;

/**
 * Tests the functionality of the {@link FileUtil} class.
 */
class FileUtilTest {

    /**
     Tests whether the isTxtFile method of the FileUtil class returns true for a valid txt file.
     */
    @Test
    void testIsTxtFile() {
        assertTrue(FileUtil.isTxtFile(new File(requireNonNull(
            getClass().getResource("/tracks/validTrack.txt")).getFile())));
    }

    /**
     Tests whether the isTxtFile method of the FileUtil class returns false for a file with an invalid format.
     */
    @Test
    void testIsNoTxtFile() {
        assertFalse(FileUtil.isTxtFile(new File(requireNonNull(
            getClass().getResource("/invalidFileFormat.csv")).getFile())));
    }

    /**
     * Tests whether the createScanner method of the FileUtil class returns the correct scanner.
     * @throws IOException if an I/O error occurs opening the source
     */
    @Test
    void testCreateScanner() throws IOException {
        final Scanner scanner = FileUtil.createScanner(new File(requireNonNull(
            getClass().getResource("/tracks/validTrack.txt")).getFile()));
        assertNotNull(scanner);
        assertTrue(scanner.hasNextLine());
    }

}
