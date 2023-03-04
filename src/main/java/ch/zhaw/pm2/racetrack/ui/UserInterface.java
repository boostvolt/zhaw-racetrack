package ch.zhaw.pm2.racetrack.ui;

import ch.zhaw.pm2.racetrack.Car;
import ch.zhaw.pm2.racetrack.Direction;
import ch.zhaw.pm2.racetrack.Track;
import ch.zhaw.pm2.racetrack.strategy.MoveStrategy;
import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * Provides an interface for any kind of GUI. These methods are the minimum to be implemented to be
 * compatible with the Game.
 */
public interface UserInterface {

    /**
     * Retrieves a file from a list of tracks.
     *
     * @param trackFiles the list of files to choose from
     * @return the selected file
     */
    Optional<File> retrieveFile(final List<File> trackFiles, String fileType);

    /**
     * Prompts the user to select a move strategy for a car.
     *
     * @param carId the ID of the car
     * @return the selected move strategy
     */
    MoveStrategy.StrategyType retrieveStrategy(final char carId);

    /**
     * Prompts the user to select a {@link Direction}.
     *
     * @param carId the ID of the car
     * @return the selected {@link Direction}
     */
    Direction retrieveDirection(final char carId);

    /**
     * Prompts the user to input a string
     *
     * @param prompt Text prompt to display
     * @return the input {@link String}
     */
    String retrieveString(final String prompt);

    /**
     * Prompts the user to answer a yes or no question.
     *
     * @param prompt the prompt to display
     * @return the answer in form of a {@code boolean} that was read from the console
     */
    Boolean retrieveBoolean(final String prompt);

    /**
     * Displays a track on the screen.
     *
     * @param track the {@link Track} to display
     */
    void displayTrack(final Track track);

    /**
     * Displays the winner of the race based on the {@link Car} ID.
     *
     * @param carId the ID of the {@link Car} that won the race
     */
    void displayWinner(final char carId);

    /**
     * Displays an error message on the screen.
     *
     * @param message the error message to display
     */
    void displayError(final String message);

    /**
     * Displays a message on the screen.
     *
     * @param message the message to display
     */
    void displayMessage(final String message);

    /**
     * Displays an intro on the screen.
     */
    void displayIntro(boolean refresh);

    /**
     * Closes all open UserInterfaces to shut down the program.
     */
    void endGame();

}
