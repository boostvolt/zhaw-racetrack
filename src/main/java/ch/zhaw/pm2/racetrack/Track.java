package ch.zhaw.pm2.racetrack;

import static ch.zhaw.pm2.racetrack.SpaceType.TRACK;
import static ch.zhaw.pm2.racetrack.SpaceType.WALL;
import static ch.zhaw.pm2.racetrack.SpaceType.spaceTypeForChar;
import static ch.zhaw.pm2.racetrack.utils.FileUtil.createScanner;
import static java.lang.String.format;
import static java.lang.System.lineSeparator;
import static java.util.Objects.requireNonNull;
import static java.util.stream.IntStream.range;

import ch.zhaw.pm2.racetrack.given.TrackSpecification;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

/**
 * This class represents the racetrack board.
 *
 * <p>The racetrack board consists of a rectangular grid of 'width' columns and 'height' rows.
 * The zero point of he grid is at the top left. The x-axis points to the right and the y-axis
 * points downwards.</p>
 * <p>Positions on the track grid are specified using {@link PositionVector} objects. These are
 * vectors containing an x/y coordinate pair, pointing from the zero-point (top-left) to the
 * addressed space in the grid.</p>
 *
 * <p>Each position in the grid represents a space which can hold an enum object of type
 * {@link SpaceType}.<br> Possible Space types are:
 * <ul>
 *  <li>WALL : road boundary or off track space</li>
 *  <li>TRACK: road or open track space</li>
 *  <li>FINISH_LEFT, FINISH_RIGHT, FINISH_UP, FINISH_DOWN :  finish line spaces which have to be crossed
 *      in the indicated direction to winn the race.</li>
 * </ul>
 * <p>Beside the board the track contains the list of cars, with their current state (position, velocity, crashed,...)</p>
 *
 * <p>At initialization the track grid data is read from the given track file. The track data must be a
 * rectangular block of text. Empty lines at the start are ignored. Processing stops at the first empty line
 * following a non-empty line, or at the end of the file.</p>
 * <p>Characters in the line represent SpaceTypes. The mapping of the Characters is as follows:</p>
 * <ul>
 *   <li>WALL : '#'</li>
 *   <li>TRACK: ' '</li>
 *   <li>FINISH_LEFT : '&lt;'</li>
 *   <li>FINISH_RIGHT: '&gt;'</li>
 *   <li>FINISH_UP   : '^;'</li>
 *   <li>FINISH_DOWN: 'v'</li>
 *   <li>Any other character indicates the starting position of a car.<br>
 *       The character acts as the id for the car and must be unique.<br>
 *       There are 1 to {@link TrackSpecification#MAX_CARS} allowed. </li>
 * </ul>
 *
 * <p>All lines must have the same length, used to initialize the grid width).<br/>
 * Beginning empty lines are skipped. <br/>
 * The track ends with the first empty line or the file end.<br>
 * An {@link InvalidFileFormatException} is thrown, if
 * <ul>
 *   <li>the file contains no track lines (grid height is 0)</li>
 *   <li>not all track lines have the same length</li>
 *   <li>the file contains no cars</li>
 *   <li>the file contains more than {@link TrackSpecification#MAX_CARS} cars</li>
 * </ul>
 *
 * <p>The Tracks {@link #toString()} method returns a String representing the current state of the race
 * (including car positions and status)</p>
 */
public class Track implements TrackSpecification {

    public static final char CRASH_INDICATOR = 'X';

    private final Map<PositionVector, SpaceType> trackFields = new LinkedHashMap<>();
    private final List<Car> cars = new ArrayList<>();
    private int height;
    private int width;

    /**
     * Initialize a Track from the given track file.<br/> See class description for structure and
     * valid tracks.
     *
     * @param trackFile Reference to a file containing the track data
     * @throws IOException                if the track file can not be opened or reading fails
     * @throws InvalidFileFormatException if the track file contains invalid data (no track lines,
     *                                    inconsistent length, no cars)
     * @throws NullPointerException       if the TrackFile is null
     */
    public Track(final File trackFile) throws IOException, InvalidFileFormatException, NullPointerException {
        initializeTrack(requireNonNull(trackFile, "TrackFile must not be null."));
    }

    /**
     * Return the height (number of rows) of the track grid.
     *
     * @return Height of the track grid
     */
    public int getHeight() {
        return height;
    }

    /**
     * Return the width (number of columns) of the track grid.
     *
     * @return Width of the track grid
     */
    public int getWidth() {
        return width;
    }


    /**
     * Return the number of cars.
     *
     * @return Number of cars
     */
    @Override
    public int getCarCount() {
        return cars.size();
    }

    /**
     * Get instance of specified car.
     *
     * @param carIndex The zero-based carIndex number
     * @return The car instance at the given index
     * @throws IllegalArgumentException if the given carIndex is out of bounds.
     */
    @Override
    public Car getCar(int carIndex) {
        if (carIndex >= cars.size()) {
            throw new IllegalArgumentException(
                format("Car with index %s does not exist.", carIndex));
        }
        return cars.get(carIndex);
    }

    /**
     * Return the type of space at the given position. If the location is outside the track bounds,
     * it is considered a WALL.
     *
     * @param position The coordinates of the position to examine
     * @return The type of track position at the given location
     */
    @Override
    public SpaceType getSpaceTypeAtPosition(PositionVector position) {
        return trackFields.getOrDefault(position, WALL);
    }

    /**
     * Gets the character representation for the given position of the racetrack, including
     * cars.<br/> This can be used for generating the {@link #toString()} representation of the
     * racetrack.<br/> If there is an active car (not crashed) at the given position, then the car
     * id is returned.<br/> If there is a crashed car at the position, {@link #CRASH_INDICATOR} is
     * returned.<br/> Otherwise, the space character for the given position is returned
     *
     * @param row row (y-value) of the racetrack position
     * @param col column (x-value) of the racetrack position
     * @return character representing the position (col,row) on the track or {@link Car#getId()}
     * resp. {@link #CRASH_INDICATOR}, if a car is at the given position
     */
    @Override
    public char getCharRepresentationAtPosition(int row, int col) {
        final PositionVector positionVector = new PositionVector(col, row);
        final List<Car> carsAtPosition = getCarsAtPosition(positionVector);
        if (carsAtPosition.isEmpty()) {
            return trackFields.getOrDefault(positionVector, WALL).getSpaceChar();
        } else {
            return carsAtPosition.stream()
                .filter(car -> !car.isCrashed())
                .findFirst()
                .map(Car::getId)
                .orElse(CRASH_INDICATOR);
        }
    }

    /**
     * Return a String representation of the track, including the car locations and status.
     *
     * @return A String representation of the track
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (int row = 0; row < getHeight(); row++) {
            for (int col = 0; col < getWidth(); col++) {
                sb.append(getCharRepresentationAtPosition(row, col));
            }
            sb.append(lineSeparator());
        }
        return sb.toString();
    }

    /**
     * Checks if the position is currently occupied by any car. Also crashed cars count as the
     * position being occupied.
     *
     * @param position The position to be checked.
     * @return True if the position is occupied by a car. False otherwise
     */
    public boolean isPositionOccupied(final PositionVector position) {
        return cars.stream()
            .anyMatch(car -> position.equals(car.getCurrentPosition()));
    }

    /**
     * Initializes the track based on the contents of the specified track file.
     *
     * @param trackFile The file containing the track information.
     * @throws IOException                If there is an error while reading the track file.
     * @throws InvalidFileFormatException If the format of the track file is invalid.
     */
    private void initializeTrack(final File trackFile)
        throws IOException, InvalidFileFormatException {
        final List<String> lines = readLines(trackFile);

        width = lines.get(0).length();
        height = lines.size();

        for (int row = 0; row < getHeight(); row++) {
            for (int col = 0; col < getWidth(); col++) {
                addTrackField(lines.get(row).charAt(col), col, row);
            }
        }

        if (cars.size() < MIN_CARS) {
            throw new InvalidFileFormatException(format(
                "Track File contains not enough cars. Please specify a minimum of %s cars in the file.",
                MIN_CARS));
        }
    }

    /**
     * Reads the lines from the specified track file.
     *
     * @param trackFile The file containing the track information.
     * @return A list of strings, each string representing a line from the track file.
     * @throws InvalidFileFormatException If the format of the track file is invalid.
     * @throws IOException                If there is an error while reading the track file.
     */
    private List<String> readLines(final File trackFile)
        throws InvalidFileFormatException, IOException {
        boolean trackBlockStarted = false;
        final List<String> lines = new ArrayList<>();
        try (final Scanner scanner = createScanner(trackFile)) {
            while (scanner.hasNextLine()) {
                final String currentLine = scanner.nextLine();
                if (!isLineEmpty(currentLine)) {
                    trackBlockStarted = true;
                    lines.add(currentLine);
                } else if (trackBlockStarted) {
                    break; // Reading stops at first occurrence of an empty line after non-empty lines
                }
            }
        }
        if (!trackBlockStarted) {
            // Indicates that there were no non-empty lines in the file
            throw new InvalidFileFormatException("Track File contains no specified Track.");
        }
        if (!hasValidWidth(lines)) {
            throw new InvalidFileFormatException(
                "Not all track lines possess the same length. Unable to create track.");
        }
        return lines;
    }

    /**
     * Checks if the specified string is null or empty.
     *
     * @param line The string to check.
     * @return true if the string is null or empty, false otherwise.
     */
    private boolean isLineEmpty(final String line) {
        return line == null || line.isEmpty();
    }

    /**
     * Indicates whether all lines possess the same length.
     *
     * @param lines the lines from the input track file
     * @return true if all lines possess the same length, in which case the width is valid. False
     * otherwise.
     */
    private boolean hasValidWidth(final List<String> lines) {
        int length = lines.get(0).length();
        return range(1, lines.size())
            .noneMatch(i -> lines.get(i).length() != length);
    }

    /**
     * Adds a track field to the trackFields map based on the specified character and its position.
     * If the character represents a valid SpaceType, the corresponding SpaceType is added to the
     * map. If the character represents a starting position for a car, a new car is added to the
     * cars list and the corresponding SpaceType is set to TRACK in the map.
     *
     * @param charAtPosition The character at the specified position in the track file.
     * @param xAxis          The x-coordinate of the specified position.
     * @param yAxis          The y-coordinate of the specified position.
     * @throws InvalidFileFormatException If the character at the specified position does not
     *                                    represent a valid SpaceType or starting position for a
     *                                    car.
     */
    private void addTrackField(char charAtPosition, int xAxis, int yAxis)
        throws InvalidFileFormatException {
        final Optional<SpaceType> spaceType = spaceTypeForChar(charAtPosition);
        if (spaceType.isPresent()) {
            // Current character represents a valid SpaceType of the track
            trackFields.put(new PositionVector(xAxis, yAxis), spaceType.get());
        } else {
            // Current character represents starting position of a new car
            addCarAtPosition(charAtPosition, xAxis, yAxis);
            trackFields.put(new PositionVector(xAxis, yAxis), TRACK);
        }
    }

    /**
     * Adds a new car to the track at the specified position, given by its X and Y coordinates.
     *
     * @param charAtPosition the character that represents the new car in the track file
     * @param xAxis          the X coordinate of the position where the new car is added
     * @param yAxis          the Y coordinate of the position where the new car is added
     * @throws InvalidFileFormatException if the maximum number of allowed cars is reached, or if a
     *                                    car with the same character already exists
     */
    private void addCarAtPosition(char charAtPosition, int xAxis, int yAxis)
        throws InvalidFileFormatException {
        if (cars.size() == MAX_CARS) {
            throw new InvalidFileFormatException(
                format("Track contains %s cars, the allowed maximum is %s.", MAX_CARS + 1,
                    MAX_CARS));
        } else if (carWithCharExists(charAtPosition)) {
            throw new InvalidFileFormatException(format(
                "Car with character %s exists multiple times in Track File. Every car needs to have a unique character.",
                charAtPosition));
        } else {
            cars.add(new Car(charAtPosition, new PositionVector(xAxis, yAxis)));
        }
    }

    /**
     * Checks whether a car with the given character identifier already exists in the list of cars.
     *
     * @param charOfCar the character identifier of the car to check for existence
     * @return true if a car with the given identifier exists, false otherwise
     */
    private boolean carWithCharExists(char charOfCar) {
        return cars.stream()
            .anyMatch(car -> car.getId() == charOfCar);
    }

    /**
     * Returns a list of all cars that are at the specified position.
     *
     * @param positionVector The position to check for cars.
     * @return A list of cars at the specified position.
     */
    private List<Car> getCarsAtPosition(PositionVector positionVector) {
        return cars.stream()
            .filter(car -> car.getCurrentPosition().equals(positionVector))
            .toList();
    }

}
