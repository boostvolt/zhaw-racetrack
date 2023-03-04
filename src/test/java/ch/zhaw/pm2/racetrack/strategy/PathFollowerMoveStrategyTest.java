package ch.zhaw.pm2.racetrack.strategy;

import static ch.zhaw.pm2.racetrack.Game.NO_WINNER;
import static ch.zhaw.pm2.racetrack.strategy.MoveStrategy.CAR_WON_STATS_TEXT;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ch.zhaw.pm2.racetrack.Car;
import ch.zhaw.pm2.racetrack.Game;
import ch.zhaw.pm2.racetrack.InvalidFileFormatException;
import ch.zhaw.pm2.racetrack.PositionVector;
import ch.zhaw.pm2.racetrack.Track;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PathFollowerMoveStrategyTest {

    private static final String FILE_PATH = "/strategy/pathList/";
    private static final List<PositionVector> validPathList = new ArrayList<>() {
        {
            add(new PositionVector(5, -3));
            add(new PositionVector(11, -2));
            add(new PositionVector(6, 2));
            add(new PositionVector(0, 0));
        }
    };

    private final static Car testCar = new Car('a', new PositionVector(0, 0));
    private static File validPathFile;
    private static File challengePathFile;
    private static File invalidPathFile;
    private static File emptyPathFile;
    private static File emptyLinesPathFile;

    private static File challengeTrackFile;

    @BeforeAll
    public static void init() {
        validPathFile = new File(requireNonNull(
            MoveListStrategy.class.getResource(FILE_PATH + "valid-path-file.txt")).getFile());

        invalidPathFile = new File(requireNonNull(
            MoveListStrategy.class.getResource(FILE_PATH + "invalid-path-file.txt")).getFile());

        emptyPathFile = new File(requireNonNull(
            MoveListStrategy.class.getResource(FILE_PATH + "empty-path-file.txt")).getFile());

        emptyLinesPathFile = new File(requireNonNull(
            MoveListStrategy.class.getResource(FILE_PATH + "empty-lines-path-file.txt")).getFile());

        challengePathFile = new File(requireNonNull(MoveListStrategy.class.getResource(
            FILE_PATH + "challenge_handout_points.txt")).getFile());

        challengeTrackFile = new File(
            requireNonNull(MoveListStrategy.class.getResource("/tracks/validTrack.txt")).getFile());
    }

    /**
     * Testing constructor of {@link PathFollowerMoveStrategy} using a {@link File} with invalid
     * lines between paths.
     */
    @Test
    void testPathFollowerConstructorInvalid() {
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class,
            () -> new PathFollowerMoveStrategy(invalidPathFile, testCar));

        assertEquals(
            "The content of the Path Follower File did not match valid path vectors. e.g. (X:5, Y:-15)",
            exception.getMessage());
    }

    /**
     * Tests the {@link PathFollowerMoveStrategy} using a valid {@link File} with empty lines
     * between paths.
     *
     * @throws IOException thrown if there is an issue loading the file.
     */
    @Test
    void testEmptyLinesFile() throws IOException {
        PathFollowerMoveStrategy emptyLinesMoveStrategy = new PathFollowerMoveStrategy(
            emptyLinesPathFile, testCar);
        PathFollowerMoveStrategy emptyMoveStrategy = new PathFollowerMoveStrategy(emptyPathFile,
            testCar);

        assertEquals(2, emptyLinesMoveStrategy.getPositionVectorList().size());
        Assertions.assertTrue(emptyMoveStrategy.getPositionVectorList().isEmpty());
    }

    /**
     * Creates a new {@link PathFollowerMoveStrategy} with a test {@link Car} using a mock path list
     * {@link File}. Each element is checked against a local {@link List} of
     * {@link PositionVector}s. Fails if any of the path points do not match while moving the
     * {@link Car}.
     *
     * @throws IOException thrown if there is an issue loading the file.
     */
    @Test
    void testNextMove() throws IOException {
        PathFollowerMoveStrategy pathFollowerMoveStrategy = new PathFollowerMoveStrategy(
            validPathFile, testCar);

        while (!validPathList.isEmpty()) {
            testCar.accelerate(pathFollowerMoveStrategy.nextMove());
            testCar.move();
            assertEquals(validPathList.get(0),
                (pathFollowerMoveStrategy.getPositionVectorList().isEmpty()) ? null
                    : pathFollowerMoveStrategy.getPositionVectorList().get(0));
            if (testCar.getCurrentPosition().equals(validPathList.get(0))) {
                validPathList.remove(0);
            }
        }

        assertEquals(List.of(new PositionVector(0, 0)),
            pathFollowerMoveStrategy.getPositionVectorList());
    }

    /**
     * Creates a {@link Game} using the validTrack.txt {@link File} for the {@link Track}. The test
     * calls the @link {@link PathFollowerMoveStrategy#nextMove()} until either a winner is found or
     * a maximum of 45 turns to prevent an infinite loop.
     *
     * @throws IOException                thrown if there is an issue loading the file.
     * @throws InvalidFileFormatException thrown if there were invalid lines in the path
     *                                    {@link File}
     */
    @Test
    void testChallengeNextMove() throws IOException, InvalidFileFormatException {
        Game game = new Game(new Track(challengeTrackFile));
        Car challengeTestCar = game.getTrack().getCar(game.getCurrentCarIndex());
        PathFollowerMoveStrategy pathFollowerMoveStrategy = new PathFollowerMoveStrategy(
            challengePathFile, challengeTestCar);

        game.setCarMoveStrategy(game.getCurrentCarIndex(), pathFollowerMoveStrategy);

        int turnCounter = 0;

        while (game.getWinner() == NO_WINNER || turnCounter >= 45) {
            game.doCarTurn(challengeTestCar.getMoveStrategy().nextMove());
            turnCounter++;
        }

        assertEquals(game.getTrack().getCar(game.getWinner()).getId(), challengeTestCar.getId());
    }

    /**
     * Testing the return value of {@link PathFollowerMoveStrategy#getStatistics()}.
     *
     * @throws IOException thrown if the constructor fails with the provided {@link File}.
     */
    @Test
    void testGetStatistics() throws IOException {
        PathFollowerMoveStrategy pathFollowerMoveStrategy = new PathFollowerMoveStrategy(
            validPathFile, testCar);

        assertEquals(format(CAR_WON_STATS_TEXT, 0), pathFollowerMoveStrategy.getStatistics());
        pathFollowerMoveStrategy.nextMove();
        assertEquals(format(CAR_WON_STATS_TEXT, 1), pathFollowerMoveStrategy.getStatistics());
    }
}
