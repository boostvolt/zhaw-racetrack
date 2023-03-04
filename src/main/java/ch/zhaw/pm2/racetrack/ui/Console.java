package ch.zhaw.pm2.racetrack.ui;

import static java.awt.Color.BLACK;
import static java.awt.Color.BLUE;
import static java.awt.Color.CYAN;
import static java.awt.Color.GREEN;
import static java.awt.Color.ORANGE;
import static java.awt.Color.PINK;
import static java.awt.Color.RED;
import static java.awt.Color.WHITE;
import static java.awt.Color.YELLOW;
import static java.lang.System.exit;
import static java.util.Arrays.stream;

import java.awt.Color;
import java.util.List;
import java.util.function.Consumer;
import org.beryx.textio.TerminalProperties;
import org.beryx.textio.TextIO;
import org.beryx.textio.swing.SwingTextTerminal;

/**
 * Represents a console that provides input and output capabilities.
 */
public class Console {

    private static final int ACCEPTABLE_EXIT_CODE = 0;
    private static final int PANE_WIDTH = 1300;
    private static final int PANE_HEIGHT = 1000;
    private static final int COLOR_SWITCH_SLEEP_TIME = 100;

    private final TextIO textIO;
    private final SwingTextTerminal textTerminal;

    /**
     * Constructs a new Console with the given pane title.
     *
     * @param paneTitle the title of the terminal pane
     */
    public Console(final String paneTitle, final String quitShortcut) {
        textIO = new TextIO(new SwingTextTerminal());
        textTerminal = (SwingTextTerminal) textIO.getTextTerminal();
        initProperties(paneTitle, quitShortcut);
    }

    /**
     * Sharing Bookmarking functionality to refresh content on the console. Set a bookmark to return
     * to later. See {@link #returnToBookmark(String)}
     *
     * @param bookmark the bookmark to set
     */
    public void setBookmark(String bookmark) {
        textTerminal.setBookmark(bookmark);
    }

    /**
     * Return to previously set Bookmark, see {@link #setBookmark(String)}.
     *
     * @param bookmark the bookmark to set
     */
    public void returnToBookmark(String bookmark) {
        textTerminal.resetToBookmark(bookmark);
    }

    /**
     * Prints the given message to the console without any line break.
     *
     * @param message the message to print
     */
    public void print(final String message) {
        textTerminal.print(message);
    }

    /**
     * Prints the given message to the console and inserts a line break afterwards.
     *
     * @param message the message to print
     */
    public void printLine(final String message) {
        textTerminal.println(message);
    }

    /**
     * Prints an empty line to the console.
     */
    public void printEmptyLine() {
        printLine("");
    }

    /**
     * Prompts the user to enter a string.
     *
     * @param prompt the message to display to prompt the user for input
     * @return the string entered by the user
     */
    public String readString(final String prompt) {
        return textIO.newStringInputReader()
            .read(prompt);
    }

    /**
     * Prompts the user to select a string from a given list of strings.
     *
     * @param prompt the message to display to prompt the user for input
     * @param list   the list of strings to choose from
     * @return the string selected by the user
     */
    public String readStringList(final String prompt, final List<String> list) {
        return textIO.newStringInputReader()
            .withNumberedPossibleValues(list)
            .read(prompt);
    }

    /**
     * Reads an EnumClass from the console.
     *
     * @param prompt    the prompt to display
     * @param <E>       the {@link Enum} class
     * @param enumClass the {@link Enum} class to read
     * @return the {@link Enum} that was read from the console
     */
    public <E extends Enum<E>> E readEnum(final String prompt, final Class<E> enumClass) {
        return textIO.newEnumInputReader(enumClass)
            .read(prompt);
    }

    /**
     * Gets a yes or no response from the user.
     *
     * @param prompt the prompt to display
     * @return the answer in form of a {@code boolean} that was read from the console
     */
    public boolean readBoolean(String prompt) {
        return textIO.newBooleanInputReader().read(prompt);
    }

    /**
     * Disposes the {@link TextIO} and {@link org.beryx.textio.TextTerminal} instances.
     */
    public void close() {
        textIO.dispose();
        textTerminal.dispose();
    }

    /**
     * Changes the background color of the console to indicate that a player has won. Celebration
     * time!
     */
    public void celebrate() {
        for (int i = 0; i < 10; i++) {
            Color[] colors = new Color[]{GREEN, RED, BLUE, YELLOW, CYAN, PINK};
            stream(colors).forEach(color -> {
                changePaneColor(color);
                try {
                    Thread.sleep(COLOR_SWITCH_SLEEP_TIME);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(
                        String.format("Thread interrupted while sleeping: %s", e.getMessage()), e);
                }
            });
        }

        changePaneColor(BLACK);
    }

    /**
     * Changes the prompt color of the console to the specified color.
     *
     * @param color the color to set the prompt color to
     */
    public void changePromptColor(final Color color) {
        getProperties().setPromptColor(color);
    }

    /**
     * Initializes the properties of the {@link org.beryx.textio.TextTerminal}.
     *
     * @param paneTitle the title of the terminal pane
     */
    private void initProperties(final String paneTitle, final String quitShortcut) {
        registerUserInterrupt(quitShortcut);
        textTerminal.setPaneTitle(paneTitle);
        final TerminalProperties<SwingTextTerminal> properties = getProperties();
        properties.setPromptColor(WHITE);
        properties.setInputBold(true);
        properties.setInputColor(ORANGE);
        properties.setPaneDimension(PANE_WIDTH, PANE_HEIGHT);
    }

    private void registerUserInterrupt(String quitShortcut) {
        textTerminal.setUserInterruptKey(quitShortcut);
        final Consumer<SwingTextTerminal> handler = textTerminal -> {
            System.out.println("User quit the game using the X or the shortcut " + quitShortcut);
            textIO.dispose();
            textTerminal.dispose();
            exit(ACCEPTABLE_EXIT_CODE);
        };
        textTerminal.registerUserInterruptHandler(handler, false);
    }

    /**
     * Returns the properties of the text terminal.
     *
     * @return The properties of the text terminal.
     */
    private TerminalProperties<SwingTextTerminal> getProperties() {
        return textTerminal.getProperties();
    }

    /**
     * Changes the background color of the pane to the specified color.
     *
     * @param color the color to set the pane's background color to
     */
    private void changePaneColor(final Color color) {
        getProperties().setPaneBackgroundColor(color);
    }

}
