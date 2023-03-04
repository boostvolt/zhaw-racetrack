package ch.zhaw.pm2.racetrack.strategy;

import static ch.zhaw.pm2.racetrack.Direction.DOWN;
import static ch.zhaw.pm2.racetrack.Direction.DOWN_LEFT;
import static ch.zhaw.pm2.racetrack.Direction.DOWN_RIGHT;
import static ch.zhaw.pm2.racetrack.Direction.LEFT;
import static ch.zhaw.pm2.racetrack.Direction.NONE;
import static ch.zhaw.pm2.racetrack.Direction.RIGHT;
import static ch.zhaw.pm2.racetrack.Direction.UP;
import static ch.zhaw.pm2.racetrack.Direction.UP_LEFT;
import static ch.zhaw.pm2.racetrack.Direction.UP_RIGHT;
import static ch.zhaw.pm2.racetrack.utils.FileUtil.createScanner;
import static ch.zhaw.pm2.racetrack.utils.FileUtil.isTxtFile;
import static java.lang.Integer.parseInt;
import static java.lang.Math.negateExact;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNull;

import ch.zhaw.pm2.racetrack.Car;
import ch.zhaw.pm2.racetrack.Direction;
import ch.zhaw.pm2.racetrack.PositionVector;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * Determines the next move based on a file containing points on a path.
 */
public class PathFollowerMoveStrategy implements MoveStrategy {

    public static final String PATH_CHARACTERS_REGEX = "[^\\d,-]";
    private static final String PATH_COORDINATE_REGEX = "\\(X:-?(\\d|\\d\\d), Y:-?(\\d|\\d\\d)\\)";
    private static final int MULTIPLICATION_THRESHOLD = 1;
    private static final int MULTIPLICATION_FACTOR = 2;
    private final Car car;
    private final List<PositionVector> positionVectorList;

    private Integer counter = 0;

    /**
     * Constructor of the {@link PathFollowerMoveStrategy}. It reads the given pathListFile to
     * propagate the positionVectorList.
     *
     * @param pathListFile The file to read the position vectors from
     * @param car          The car choosing that strategy
     * @throws IOException              Thrown if the {@link File} is not the expected type.
     * @throws IllegalArgumentException Thrown if the {@link File} does not hold the expected
     *                                  content.
     * @throws NullPointerException     Thrown if the {@link File} or the {@link Car} is null
     */
    public PathFollowerMoveStrategy(final File pathListFile, final Car car)
        throws IllegalArgumentException, IOException, NullPointerException {
        this.car = requireNonNull(car, "Car must not be null");
        requireNonNull(pathListFile, "pahListFile must not be null");

        if (!isTxtFile(pathListFile)) {
            throw new IllegalArgumentException("PathFollower File: only Txt files are allowed.");
        }

        positionVectorList = readPathFile(pathListFile);
    }

    public PathFollowerMoveStrategy(List<PositionVector> positionVectors, final Car car) {
        this.car = requireNonNull(car, "Car must not be null");
        this.positionVectorList = new LinkedList<>();
        this.positionVectorList.addAll(positionVectors);
    }

    /**
     * {@inheritDoc}
     *
     * @return next direction to follow the given path, {@link Direction#NONE} if there are no more
     * coordinates available
     */
    @Override
    public Direction nextMove() {
        counter++;
        if (hasReachedPathCoordinate()) {
            positionVectorList.remove(0);
        }

        if (!positionVectorList.isEmpty()) {
            int xVelocity = getAdjustedVelocity(car.getVelocity().getX());
            int yVelocity = getAdjustedVelocity(car.getVelocity().getY());

            return getNextValidDirection(
                positionVectorList.get(0).subtract(car.getCurrentPosition()), xVelocity, yVelocity);
        } else {
            return NONE;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTurnMessage(final Car car) {
        return String.format("Car %s is trying to move towards %s.", car.getId(),
            positionVectorList.stream()
                .findFirst()
                .map(Object::toString)
                .orElse("the horizon"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStatistics() {
        return format(CAR_WON_STATS_TEXT, counter);
    }

    /**
     * Provides the positionVectorList. Needed for testing purposes.
     *
     * @return the positionVectorList
     */
    List<PositionVector> getPositionVectorList() {
        return positionVectorList;
    }

    private boolean hasReachedPathCoordinate() {
        return !positionVectorList.isEmpty()
            && car.getCurrentPosition().equals(positionVectorList.get(0));
    }

    private Direction getNextValidDirection(final PositionVector difference, int xVelocity,
        int yVelocity) {
        final int xDiff = difference.getX();
        final int yDiff = difference.getY();

        if (leftTurnNeeded(xVelocity, xDiff)) {
            return getNextDirection(yVelocity, yDiff, DOWN_LEFT, UP_LEFT, LEFT);
        } else if (rightTurnNeeded(xVelocity, xDiff)) {
            return getNextDirection(yVelocity, yDiff, DOWN_RIGHT, UP_RIGHT, RIGHT);
        } else {
            return getNextDirection(yVelocity, yDiff, DOWN, UP, NONE);
        }
    }

    private boolean leftTurnNeeded(int xVelocity, int xDiff) {
        return xDiff - xVelocity < NOT_MOVING;
    }

    private boolean rightTurnNeeded(int xVelocity, int xDiff) {
        return xDiff - xVelocity > NOT_MOVING;
    }

    private Direction getNextDirection(int yVelocity, int yDiff, final Direction accelerator,
        final Direction breaker, final Direction neutral) {
        if (yDiff - yVelocity > NOT_MOVING) {
            return accelerator;
        } else if (yDiff - yVelocity < NOT_MOVING) {
            return breaker;
        } else {
            return neutral;
        }
    }

    /**
     * Multiplies the velocity if it is higher than 1
     *
     * @param axisVelocity velocity on the X or Y axis based on a {@link PositionVector}
     * @return the changed or unchanged velocity based on speed.
     */
    private int getAdjustedVelocity(int axisVelocity) {
        return axisVelocity > MULTIPLICATION_THRESHOLD || axisVelocity < negateExact(
            MULTIPLICATION_THRESHOLD)
            ? axisVelocity * MULTIPLICATION_FACTOR
            : axisVelocity;
    }

    /**
     * Checks file content to match a pattern. Matching Line is i.E. (X:13, Y:-2) The file content
     * is checked until there is an empty Line or fails if there is a Line not matching the
     * pattern.
     *
     * @param fileToRead Assumed to be a TXT {@link File}, Otherwise Throws {@link IOException}
     * @return An {@link List} of {@link PositionVector} Elements.
     * @throws IOException              thrown if there was an error Reading the File or a
     *                                  non-matching non-empty Line.
     * @throws IllegalArgumentException thrown if the provided file it not of the correct format
     */
    private List<PositionVector> readPathFile(final File fileToRead)
        throws IOException, IllegalArgumentException {
        List<PositionVector> filePathList = new ArrayList<>();

        try (Scanner fileScanner = createScanner(fileToRead)) {
            while (fileScanner.hasNext()) {
                String positionVectorAsString = fileScanner.nextLine();

                if (positionVectorAsString.isBlank()) {
                    return filePathList;
                }

                if (positionVectorAsString.matches(PATH_COORDINATE_REGEX)) {
                    String[] positionAsTupel = positionVectorAsString.replaceAll(
                        PATH_CHARACTERS_REGEX, "").split(",");
                    filePathList.add(new PositionVector(parseInt(positionAsTupel[0]),
                        parseInt(positionAsTupel[1])));
                } else {
                    throw new IllegalArgumentException(
                        "The content of the Path Follower File did not match valid path vectors. e.g. (X:5, Y:-15)");
                }
            }
        }

        return filePathList;
    }
}
