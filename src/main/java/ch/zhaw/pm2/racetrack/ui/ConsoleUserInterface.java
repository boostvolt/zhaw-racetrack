package ch.zhaw.pm2.racetrack.ui;

import static java.awt.Color.RED;
import static java.awt.Color.WHITE;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import ch.zhaw.pm2.racetrack.Direction;
import ch.zhaw.pm2.racetrack.Track;
import ch.zhaw.pm2.racetrack.strategy.MoveStrategy;
import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * The ConsoleUserInterface class is an implementation of the UserInterface interface. It provides
 * methods to interact with the user through the console and access various aspects of car racing
 * tracks.
 *
 * @see UserInterface
 */
public class ConsoleUserInterface implements UserInterface {

    private static final String LOGO = """

        .____                   __            _________            __________                      __                        __
        |    |    __ __   ____ |  | _____.__. \\______  \\           \\______   \\_____    ____  _____/  |_____________    ____ |  | __
        |    |   |  |  \\_/ ___\\|  |/ <   |  |     /    /   ______   |       _/\\__  \\ _/ ___\\/ __ \\   __\\_  __ \\__  \\ _/ ___\\|  |/ /
        |    |___|  |  /\\  \\___|    < \\___  |    /    /   /_____/   |    |   \\ / __ \\\\  \\__\\  ___/|  |  |  | \\// __ \\\\  \\___|    <
        |_______ \\____/  \\___  >__|_ \\/ ____|   /____/              |____|_  /(____  /\\___  >___  >__|  |__|  (____  /\\___  >__|_ \\
                \\/           \\/     \\/\\/                                   \\/      \\/     \\/    \\/                 \\/     \\/     \\/

        """;

    private final Console console;
    private final String title;
    private final String quitShortcut;

    /**
     * Creates a new instance of ConsoleUserInterface. Initializes a new Console with the title
     * "RaceTrack".
     *
     * @param title        of the game window.
     * @param quitShortcut key input that immediately closes the game.
     * @throws NullPointerException thrown if {@link #title} or {@link #quitShortcut} are null.
     */
    public ConsoleUserInterface(final String title, final String quitShortcut) {
        console = new Console(title, quitShortcut);
        this.title = requireNonNull(title, "title must not be null");
        this.quitShortcut = requireNonNull(quitShortcut, "quitShortcut must not be null");
    }

    /**
     * {@inheritDoc}
     *
     * @see UserInterface#retrieveFile
     */
    public Optional<File> retrieveFile(List<File> availableFiles, String fileType)
        throws NullPointerException {
        final String chosenFile = requireNonNull(
            console.readStringList(format("Select %s file", fileType),
                availableFiles.stream().map(File::getName).toList()), "trackFile must not be null");

        return availableFiles.stream()
            .filter(file -> file.getName().equals(chosenFile))
            .findFirst();
    }

    /**
     * {@inheritDoc}
     *
     * @see UserInterface#retrieveString(String)
     */
    public String retrieveString(final String prompt) {
        return console.readString(format(prompt));
    }

    /**
     * {@inheritDoc}
     *
     * @see UserInterface#retrieveBoolean(String)
     */
    @Override
    public Boolean retrieveBoolean(String prompt) {
        return console.readBoolean(prompt);
    }

    /**
     * {@inheritDoc}
     *
     * @see UserInterface#retrieveStrategy
     */
    public MoveStrategy.StrategyType retrieveStrategy(char carId) {
        return requireNonNull(console.readEnum(format("Select strategy for car %s", carId),
            MoveStrategy.StrategyType.class), "strategy must not be null");
    }

    /**
     * {@inheritDoc}
     *
     * @see UserInterface#retrieveDirection
     */
    public Direction retrieveDirection(char carId) {
        return requireNonNull(
            console.readEnum(format("Select direction for car %s", carId), Direction.class),
            "direction must not be null");
    }

    /**
     * {@inheritDoc}
     *
     * @see UserInterface#displayTrack
     */
    public void displayTrack(Track track) {
        requireNonNull(track, "track must not be null");
        console.returnToBookmark(title);
        console.print(track.toString());
    }

    /**
     * {@inheritDoc}
     *
     * @see UserInterface#displayWinner
     */
    public void displayWinner(char carId) {
        console.printLine(format("The winner is car %s", carId));
        console.celebrate();
    }

    /**
     * {@inheritDoc}
     *
     * @see UserInterface#displayError
     */
    public void displayError(String message) {
        console.changePromptColor(RED);
        console.printLine(message);
        console.changePromptColor(WHITE);
        console.printEmptyLine();
    }

    /**
     * {@inheritDoc}
     *
     * @see UserInterface#displayMessage
     */
    public void displayMessage(String message) {
        console.printLine(message);
    }

    /**
     * {@inheritDoc}
     *
     * @see UserInterface#displayIntro
     */
    public void displayIntro(boolean refresh) {
        console.printLine(LOGO);
        console.printLine(format("Welcome to a new game of %s", title));
        console.printLine(format("You can end the game anytime by pressing %s", quitShortcut));
        if (refresh) {
            console.returnToBookmark(title);
        } else {
            console.setBookmark(title);
        }
        console.printEmptyLine();
    }

    /**
     * {@inheritDoc}
     *
     * @see UserInterface#endGame
     */
    public void endGame() {
        console.close();
    }

}
