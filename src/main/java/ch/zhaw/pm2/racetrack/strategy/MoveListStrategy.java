package ch.zhaw.pm2.racetrack.strategy;

import static ch.zhaw.pm2.racetrack.Direction.NONE;
import static ch.zhaw.pm2.racetrack.utils.FileUtil.createScanner;
import static ch.zhaw.pm2.racetrack.utils.FileUtil.isTxtFile;
import static java.lang.String.format;

import ch.zhaw.pm2.racetrack.Car;
import ch.zhaw.pm2.racetrack.Direction;
import ch.zhaw.pm2.racetrack.InvalidFileFormatException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Determines the next move based on a {@link File} containing a list of {@link Direction}s.
 */
public class MoveListStrategy implements MoveStrategy {

    private final List<Direction> moveList;

    private Integer counter = 0;

    /**
     * Constructor for the Strategy. Accepts a txt {@link File} and reads the {@link Direction}
     * values (if any) from the {@link File}. If the {@link File} is not a txt {@link File} it
     * throws an {@link IOException}.
     *
     * @param moveListFile expects a txt {@link File} with {@link Direction} arguments.
     * @throws IOException                Thrown if the {@link File} is not the expected type.
     * @throws InvalidFileFormatException Thrown if the {@link File} does not hold the expected
     *                                    content.
     */
    public MoveListStrategy(final File moveListFile)
        throws IOException, InvalidFileFormatException {
        if (!isTxtFile(moveListFile)) {
            throw new IOException("Only Txt files are allowed.");
        }

        moveList = readMoves(moveListFile);
    }

    /**
     * {@inheritDoc}
     *
     * @return next {@link Direction} from move {@link File} or {@link Direction#NONE}, if no more
     * moves are available.
     */
    @Override
    public Direction nextMove() {
        counter++;
        return !moveList.isEmpty()
            ? moveList.remove(0)
            : NONE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTurnMessage(final Car car) {
        return format("Car %s next move is %s", car.getId(),
            (moveList.stream().findFirst().orElse(NONE)));
    }

    /**
     * Read {@link Direction} values out of given {@link File} {@link BufferedReader}.
     *
     * @param fileToRead {@link File} move list {@link File} to Scan.
     * @return List of {@link Direction} values.
     * @throws IOException                Thrown if {@link File} could not be loaded.
     * @throws InvalidFileFormatException Thrown if {@link File} did include non
     *                                    {@link Direction}s.
     */
    private List<Direction> readMoves(final File fileToRead)
        throws InvalidFileFormatException, IOException {
        List<Direction> fileMoveList = new ArrayList<>();

        try (Scanner fileScanner = createScanner(fileToRead)) {
            while (fileScanner.hasNext()) {
                final String nextLine = fileScanner.nextLine();

                if (nextLine.isBlank()) {
                    return fileMoveList;
                }

                if (!Direction.isValidDirection(nextLine)) {
                    throw new InvalidFileFormatException(format(
                        "The Move List File supplied should only include valid directions. %s",
                        Arrays.toString(Direction.values())));
                }

                fileMoveList.add(Direction.valueOf(nextLine));
            }
        }

        return fileMoveList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStatistics() {
        return format(CAR_WON_STATS_TEXT, counter);
    }
}
