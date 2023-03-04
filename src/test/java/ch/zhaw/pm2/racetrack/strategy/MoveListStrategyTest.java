package ch.zhaw.pm2.racetrack.strategy;

import static ch.zhaw.pm2.racetrack.strategy.MoveStrategy.CAR_WON_STATS_TEXT;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ch.zhaw.pm2.racetrack.Direction;
import ch.zhaw.pm2.racetrack.InvalidFileFormatException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MoveListStrategyTest {

    private static final String FILE_PATH = "/strategy/moveList/";
    private static final List<Direction> validDirectionList = new ArrayList<>() {
        {
            add(Direction.DOWN_LEFT);
            add(Direction.DOWN);
            add(Direction.DOWN_RIGHT);
            add(Direction.LEFT);
            add(Direction.NONE);
            add(Direction.RIGHT);
            add(Direction.UP_LEFT);
            add(Direction.UP);
            add(Direction.UP_RIGHT);
            add(Direction.DOWN_LEFT);
        }
    };

    private static final List<Direction> validEmptyLinesList = new ArrayList<>() {
        {
            add(Direction.DOWN_LEFT);
            add(Direction.NONE);
            add(Direction.NONE);
        }
    };

    private static File validMoveFile;
    private static File invalidMoveFile;
    private static File emptyMoveListFile;
    private static File validEmptyLinesMoveListFile;

    @BeforeAll
    public static void init() {
        validMoveFile = new File(
            requireNonNull(
                MoveListStrategy.class.getResource(FILE_PATH + "valid-move-list.txt"))
                .getFile());

        invalidMoveFile = new File(
            requireNonNull(
                MoveListStrategyTest.class.getResource(FILE_PATH + "invalid-move-list.txt"))
                .getFile());

        emptyMoveListFile = new File(
            requireNonNull(
                MoveListStrategyTest.class.getResource(FILE_PATH + "empty-move-list.txt"))
                .getFile());

        validEmptyLinesMoveListFile = new File(requireNonNull(
            MoveListStrategyTest.class.getResource(FILE_PATH + "empty-lines-move-list.txt"))
            .getFile());
    }

    /**
     * Checks an invalid File with no {@link Direction} values.
     */
    @Test
    void testMoveListConstructorInvalid() {
        assertThrows(InvalidFileFormatException.class, () -> new MoveListStrategy(invalidMoveFile));
    }

    /**
     * Checks if the {@link MoveListStrategy#nextMove()} method properly gets the {@link Direction}
     * values from the file and executes them in order.
     *
     * @throws IOException                thrown if the constructor fails with a valid file.
     * @throws InvalidFileFormatException thrown if the file contains invalid arguments.
     */
    @Test
    void testNextMove() throws IOException, InvalidFileFormatException {
        MoveListStrategy moveListStrategy = new MoveListStrategy(validMoveFile);

        for (Direction direction :
            validDirectionList) {
            assertEquals(direction, moveListStrategy.nextMove());
        }
    }

    /**
     * Checks if the {@link MoveListStrategy#nextMove()} still works with an empty txt {@link File}.
     * Should always return {@link Direction#NONE}
     *
     * @throws IOException                thrown if the constructor fails with a valid file.
     * @throws InvalidFileFormatException thrown if the file contains invalid arguments.
     */
    @Test
    void testNextMoveEmptyFile() throws IOException, InvalidFileFormatException {
        MoveListStrategy moveListStrategy = new MoveListStrategy(emptyMoveListFile);

        for (int moveNr = 0; moveNr < 2; moveNr++) {
            assertEquals(Direction.NONE, moveListStrategy.nextMove());
        }
    }

    /**
     * Checks if the {@link MoveListStrategy#nextMove()} method properly gets the {@link Direction}
     * values from the file and executes them in order even if there are empty Lines.
     *
     * @throws IOException                thrown if the constructor fails with a valid file.
     * @throws InvalidFileFormatException thrown if the file contains invalid arguments.
     */
    @Test
    void testNextMoveEmptyLinesFile() throws IOException, InvalidFileFormatException {
        MoveListStrategy moveListStrategy = new MoveListStrategy(validEmptyLinesMoveListFile);

        for (Direction direction :
            validEmptyLinesList) {
            assertEquals(direction, moveListStrategy.nextMove());
        }
    }

    /**
     * Testing the return value of {@link MoveListStrategy#getStatistics()}.
     *
     * @throws IOException                thrown if the constructor fails with the provided
     *                                    {@link File}.
     * @throws InvalidFileFormatException thrown if the file contains invalid arguments.
     */
    @Test
    void testGetStatistics() throws IOException, InvalidFileFormatException {
        MoveListStrategy moveListStrategy = new MoveListStrategy(validMoveFile);

        assertEquals(format(CAR_WON_STATS_TEXT, 0), moveListStrategy.getStatistics());
        moveListStrategy.nextMove();
        assertEquals(format(CAR_WON_STATS_TEXT, 1), moveListStrategy.getStatistics());
    }
}
