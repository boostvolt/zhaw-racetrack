package ch.zhaw.pm2.racetrack;

import static ch.zhaw.pm2.racetrack.SpaceType.FINISH_RIGHT;
import static ch.zhaw.pm2.racetrack.SpaceType.TRACK;
import static ch.zhaw.pm2.racetrack.SpaceType.WALL;
import static ch.zhaw.pm2.racetrack.Track.CRASH_INDICATOR;
import static ch.zhaw.pm2.racetrack.given.TrackSpecification.MAX_CARS;
import static ch.zhaw.pm2.racetrack.utils.FileUtil.createScanner;
import static java.lang.String.format;
import static java.lang.System.lineSeparator;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TrackTest {

    private static final char CAR_ID_A = 'a';
    private static final char CAR_ID_B = 'b';

    private Track track;

    @Test
    void testLoadValidTrack() throws InvalidFileFormatException, IOException {
        final File inputFile = getInputFile("validTrack");
        track = new Track(inputFile);

        assertEquals(2, track.getCarCount());
        assertCar(0, CAR_ID_A, 22);
        assertCar(1, CAR_ID_B, 24);

        assertEquals(26, track.getHeight());
        assertEquals(63, track.getWidth());

        assertEquals(WALL, track.getSpaceTypeAtPosition(new PositionVector(0, 0)));
        assertEquals(WALL, track.getSpaceTypeAtPosition(new PositionVector(62, 0)));
        assertEquals(TRACK, track.getSpaceTypeAtPosition(new PositionVector(17, 1)));
        assertEquals(TRACK, track.getSpaceTypeAtPosition(new PositionVector(24, 22)));
        assertEquals(TRACK, track.getSpaceTypeAtPosition(new PositionVector(24, 24)));
        assertEquals(FINISH_RIGHT, track.getSpaceTypeAtPosition(new PositionVector(22, 22)));
        assertEquals(WALL, track.getSpaceTypeAtPosition(new PositionVector(999, 999)));

        assertEquals(WALL.getSpaceChar(), track.getCharRepresentationAtPosition(0, 0));
        assertEquals(TRACK.getSpaceChar(), track.getCharRepresentationAtPosition(1, 17));
        assertEquals(CAR_ID_A, track.getCharRepresentationAtPosition(22, 24));
        assertEquals(CAR_ID_B, track.getCharRepresentationAtPosition(24, 24));
        assertEquals(FINISH_RIGHT.getSpaceChar(), track.getCharRepresentationAtPosition(22, 22));

        assertToString(inputFile);
    }

    @Test
    void testCarCrashed() throws InvalidFileFormatException, IOException {
        track = new Track(getInputFile("validTrack"));

        track.getCar(0).crash(new PositionVector(25, 22));
        assertEquals(CRASH_INDICATOR, track.getCharRepresentationAtPosition(22, 25));

        track.getCar(0).crash(new PositionVector(16, 1));
        assertEquals(CRASH_INDICATOR, track.getCharRepresentationAtPosition(1, 16));
    }

    @Test
    void testNonUniqueCarId() {
        final InvalidFileFormatException exception = assertThrows(InvalidFileFormatException.class,
            () -> new Track(getInputFile("nonUniqueCarIds")));
        assertEquals(
            "Car with character a exists multiple times in Track File. Every car needs to have a unique character.",
            exception.getMessage());
    }

    @Test
    void testCarLimitExceeded() {
        final InvalidFileFormatException exception = assertThrows(InvalidFileFormatException.class,
            () -> new Track(getInputFile("carLimitExceeded")));
        assertEquals(
            "Track contains 10 cars, the allowed maximum is 9.",
            exception.getMessage());
    }

    @Test
    void testCarLimitMet() throws InvalidFileFormatException, IOException {
        final File inputFile = getInputFile("carLimitMet");
        track = new Track(inputFile);

        assertEquals(MAX_CARS, track.getCarCount());

        assertToString(inputFile);
    }

    @ParameterizedTest
    @ValueSource(strings = {"noCars", "oneCar"})
    void testCarMinimum(String fileName) {
        final InvalidFileFormatException exception = assertThrows(InvalidFileFormatException.class,
            () -> new Track(getInputFile(fileName)));
        assertEquals(
            "Track File contains not enough cars. Please specify a minimum of 2 cars in the file.",
            exception.getMessage());
    }

    @Test
    void testGetCarOutOfBounds() throws InvalidFileFormatException, IOException {
        track = new Track(getInputFile("validTrack"));
        final int carCount = track.getCarCount();

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> track.getCar(carCount));
        assertEquals(
            format("Car with index %s does not exist.", track.getCarCount()),
            exception.getMessage());
    }

    @Test
    void testOnlyEmptyLines() {
        final InvalidFileFormatException exception = assertThrows(InvalidFileFormatException.class,
            () -> new Track(getInputFile("empty")));
        assertEquals(
            "Track File contains no specified Track.",
            exception.getMessage());
    }

    @Test
    void testIntermediateEmptyLine() throws InvalidFileFormatException, IOException {
        final File inputFile = getInputFile("intermediateEmptyLine");
        track = new Track(inputFile);

        assertEquals(7, track.getHeight());
    }

    @Test
    void testInvalidWidth() {
        final InvalidFileFormatException exception = assertThrows(InvalidFileFormatException.class,
            () -> new Track(getInputFile("invalidWidth")));
        assertEquals(
            "Not all track lines possess the same length. Unable to create track.",
            exception.getMessage());
    }

    @Test
    void testNullFile() {
        final NullPointerException exception = assertThrows(NullPointerException.class,
            () -> new Track(null));
        assertEquals(
            "TrackFile must not be null.",
            exception.getMessage());
    }

    @Test
    void testOutOfBoundsCharRepresentation() throws InvalidFileFormatException, IOException {
        track = new Track(getInputFile("validTrack"));

        assertEquals(WALL.getSpaceChar(),
            track.getCharRepresentationAtPosition(999, 999));
    }

    @Test
    void testMultipleCarsOnSamePositionOneNotCrashed()
        throws InvalidFileFormatException, IOException {
        track = new Track(getInputFile("validTrack"));

        final Car carA = track.getCar(0);
        final Car carB = track.getCar(1);
        carA.crash(carB.getCurrentPosition());

        final PositionVector currentPosition = carA.getCurrentPosition();
        assertEquals(CAR_ID_B,
            track.getCharRepresentationAtPosition(currentPosition.getY(), currentPosition.getX()));
    }

    @Test
    void testMultipleCarsOnSamePositionBothCrashed()
        throws InvalidFileFormatException, IOException {
        track = new Track(getInputFile("validTrack"));

        final Car carA = track.getCar(0);
        final Car carB = track.getCar(1);
        carA.crash(carB.getCurrentPosition());
        carB.crash(carA.getCurrentPosition());

        final PositionVector currentPosition = carA.getCurrentPosition();
        assertEquals(CRASH_INDICATOR,
            track.getCharRepresentationAtPosition(currentPosition.getY(), currentPosition.getX()));
    }

    @Test
    void testIsPositionOccupied() throws InvalidFileFormatException, IOException {
        track = new Track(getInputFile("validTrack"));
        assertTrue(track.isPositionOccupied(new PositionVector(24, 22)));
        assertFalse(track.isPositionOccupied(new PositionVector(24, 23)));
    }

    private void assertCar(int carIndex, char expectedCarId, int yPosition) {
        final Car carA = track.getCar(carIndex);
        assertEquals(expectedCarId, carA.getId());
        assertEquals(new PositionVector(24, yPosition), carA.getCurrentPosition());
    }

    private File getInputFile(String fileName) {
        return new File(
            requireNonNull(getClass().getResource(format("/tracks/%s.txt", fileName))).getFile());
    }

    private void assertToString(File inputFile) throws IOException {
        final Scanner scanner = createScanner(inputFile);
        final StringBuilder sb = new StringBuilder();
        while (scanner.hasNextLine()) {
            sb.append(scanner.nextLine());
            sb.append(lineSeparator());
        }
        assertEquals(sb.toString(), track.toString());
    }

}
