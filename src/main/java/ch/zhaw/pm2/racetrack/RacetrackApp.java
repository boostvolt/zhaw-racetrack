package ch.zhaw.pm2.racetrack;

import static ch.zhaw.pm2.racetrack.Game.NO_WINNER;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.SECONDS;

import ch.zhaw.pm2.racetrack.strategy.DoNotMoveStrategy;
import ch.zhaw.pm2.racetrack.strategy.MoveListStrategy;
import ch.zhaw.pm2.racetrack.strategy.MoveStrategy;
import ch.zhaw.pm2.racetrack.strategy.PathFinderStrategy;
import ch.zhaw.pm2.racetrack.strategy.PathFollowerMoveStrategy;
import ch.zhaw.pm2.racetrack.strategy.UserMoveStrategy;
import ch.zhaw.pm2.racetrack.ui.ConsoleUserInterface;
import ch.zhaw.pm2.racetrack.ui.UserInterface;
import ch.zhaw.pm2.racetrack.utils.FileUtil;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * The RacetrackApp Class is the entry point and handles the main game loop for the Racetrack Game
 * Application. It makes use of methods in {@link Game} and {@link ConsoleUserInterface} to forward
 * the game state until a winner has been decided.
 */
public class RacetrackApp {

    private static final String GAME_TITLE = "Racetrack";
    private static final String QUIT_SHORTCUT = "Q";
    private static final int END_GAME_WAIT_TIME_IN_SECONDS = 2;
    private static final int ROUND_WAIT_TIME_IN_SECONDS = 1;
    private static final String STANDARD_ERROR_MSG =
        format("See below for detailed error message.%s", System.lineSeparator());

    private final Config config;
    private final UserInterface userInterface;
    private Game game;

    private boolean draw;
    private boolean refreshGame;

    public RacetrackApp() {
        userInterface = new ConsoleUserInterface(GAME_TITLE, QUIT_SHORTCUT);
        config = new Config();
    }

    /**
     * Main method to start the application.
     *
     * @param args No args are expected.
     */
    public static void main(String[] args) {
        new RacetrackApp().newGame();
    }

    /**
     * Sets up the game and starts it if no errors are thrown. Otherwise, prompts for a retry or
     * cleans up the UI.
     */
    private void newGame() {
        userInterface.displayIntro(refreshGame);
        boolean initializationSuccessful = false;

        try {
            initializeGame();
            initializationSuccessful = true;
        } catch (IOException e) {
            decorateAndDisplayErrorMessage(e,
                "Issue occurred during initialization of the game. Check the chosen Files.");
        } catch (InvalidFileFormatException e) {
            decorateAndDisplayErrorMessage(e, "Issue with the File Format.");
        } catch (UnsupportedOperationException e) {
            decorateAndDisplayErrorMessage(e, "The selected action is not supported.");
        } catch (NullPointerException e) {
            decorateAndDisplayErrorMessage(e, "You forgot to provide something here.");
        } catch (Exception e) {
            decorateAndDisplayErrorMessage(e, "An unknown error occurred.");
        }

        if (initializationSuccessful) {
            runGame();
        } else {
            restartIfApplicable("Would you like to restart the initialization phase?");
        }
    }

    /**
     * Prompt the user if they want to restart the game.
     *
     * @param prompt a {@link String} to prompt the user.
     */
    private void restartIfApplicable(final String prompt) {
        if (TRUE.equals(userInterface.retrieveBoolean(prompt))) {
            newGame();
        } else {
            endGame();
        }
    }

    /**
     * Main game loop. Handles the running game and end of game cleanup.
     */
    private void runGame() {
        while (game.getWinner() == NO_WINNER && !draw) {
            runGameTurn();
        }

        userInterface.displayTrack(game.getTrack());
        if (game.getWinner() != NO_WINNER) {
            userInterface.displayWinner(game.getCarId(game.getWinner()));
            final Car winningCar = game.getTrack().getCar(game.getWinner());
            userInterface.displayMessage(winningCar.getMoveStrategy().getStatistics());
        } else {
            userInterface.displayMessage(
                "It's a tie! The drivers died of old age, since nobody moved.");
        }

        restartIfApplicable("Do you want to play again?");
    }

    /**
     * Print out a message and shut the game down gracefully.
     */
    private void endGame() {
        userInterface.displayMessage("Thanks for playing!");
        delayGameOutput(END_GAME_WAIT_TIME_IN_SECONDS);
        userInterface.endGame();
    }

    /**
     * Runs a single game turn. It displays the track, lets the car make his next move depending on
     * the chosen strategy, and then it switches to the next active car.
     */
    private void runGameTurn() {
        final Car car = game.getTrack().getCar(game.getCurrentCarIndex());

        userInterface.displayTrack(game.getTrack());
        userInterface.displayMessage(car.getMoveStrategy().getTurnMessage(car));

        game.doCarTurn(car.getMoveStrategy().nextMove());

        game.switchToNextActiveCar();
        delayGameOutput(ROUND_WAIT_TIME_IN_SECONDS);
    }

    private void decorateAndDisplayErrorMessage(final Throwable e, final String errorMessage) {
        userInterface.displayError(
            format(errorMessage + " %s%s", STANDARD_ERROR_MSG, e.getMessage()));
    }

    /**
     * Initiate game with {@link Track} {@link File} from the {@link UserInterface} And let the User
     * set {@link ch.zhaw.pm2.racetrack.strategy.MoveStrategy} for all {@link Car}s Sets the game to
     * a draw if only {@link DoNotMoveStrategy} has been selected.
     *
     * @throws InvalidFileFormatException thrown if any of the {@link File}s selected was invalid
     * @throws IOException                thrown if any of the {@link File}s selected could not be
     *                                    found or had errors.
     * @throws NullPointerException       if there are no move list or path follower files
     *                                    available
     * @throws IllegalArgumentException   if the selected path follower file is invalid
     */
    private void initializeGame() throws InvalidFileFormatException, IOException,
        NullPointerException, IllegalArgumentException {
        game = new Game(new Track(getTrackFile()));
        draw = true;
        refreshGame = true;

        for (int i = 0; i < game.getCarCount(); i++) {
            final MoveStrategy chosenMoveStrategy = chooseCarMoveStrategy(game.getCarId(i));
            game.setCarMoveStrategy(i, chosenMoveStrategy);
            game.switchToNextActiveCar();
            if (!(chosenMoveStrategy instanceof DoNotMoveStrategy)) {
                draw = false;
            }
        }
    }

    /**
     * Making sure that {@link Optional} {@link File} exists and can be loaded.
     *
     * @return a {@link File} containing the information for a {@link Track}
     */
    private File getTrackFile() {
        Optional<File> trackFile = retrieveTrackFileSelection();

        while (trackFile.isEmpty()) {
            userInterface.displayError("The selected file could not be found.");
            trackFile = retrieveTrackFileSelection();
        }

        return trackFile.get();
    }

    /**
     * Retrieves the {@link File} for a track using the {@link UserInterface}.
     *
     * @return an {@link Optional} {@link File} containing content for a {@link Track}.
     * @throws NullPointerException thrown if no {@link File} was supplied.
     */
    private Optional<File> retrieveTrackFileSelection() throws NullPointerException {
        return userInterface.retrieveFile(
            stream(requireNonNull(config.getTrackDirectory().listFiles()))
                .filter(FileUtil::isTxtFile)
                .toList(), "track");
    }

    /**
     * Lets the User choose the {@link MoveStrategy} for the current {@link Car}.
     *
     * @param carId from the current active car.
     * @return {@link MoveStrategy} for the {@link Car}
     * @throws IOException                thrown if the {@link File} selected was not found.
     * @throws InvalidFileFormatException thrown if the {@link File} selected was not the proper
     *                                    format.
     * @throws NullPointerException       if there are no move list or path follower files
     *                                    available
     * @throws IllegalArgumentException   if the selected path follower file is invalid
     */
    private MoveStrategy chooseCarMoveStrategy(char carId) throws IOException,
        InvalidFileFormatException, NullPointerException, IllegalArgumentException {
        return switch (userInterface.retrieveStrategy(carId)) {
            case DO_NOT_MOVE -> new DoNotMoveStrategy();
            case USER -> new UserMoveStrategy(userInterface,
                carId);
            case MOVE_LIST -> new MoveListStrategy(userInterface.retrieveFile(
                    stream(requireNonNull(config.getMoveDirectory().listFiles())).toList(), "move list")
                .orElseThrow(() -> new IOException("Move List Strategy File could not be found.")));
            case PATH_FOLLOWER -> new PathFollowerMoveStrategy(userInterface.retrieveFile(
                stream(requireNonNull(config.getFollowerDirectory().listFiles())).toList(),
                "path list").orElseThrow(
                () -> new IOException("Path Follower Strategy File could not be found")),
                game.getTrack().getCar(game.getCurrentCarIndex()));
            case PATH_FINDER -> new PathFinderStrategy(game.getTrack(),
                game.getTrack().getCar(game.getCurrentCarIndex()));
        };
    }

    /**
     * Delays the Game for the specified amount of micro seconds.
     *
     * @param seconds amount of seconds as whole Number.
     * @throws IllegalStateException if the current Thread was interrupted while sleeping.
     */
    private void delayGameOutput(int seconds) {
        try {
            SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Thread interrupted while sleeping: " + e.getMessage(),
                e);
        }
    }
}
