/**
 * Config & constants
 */
public class CFG {
    // menu choices
    static final String[] MAIN_MENU = { "Start a typerace", "Top (local) runs" };
    static final String[] CONFIRM_CLEAR = { "Ye", "Nu" };
    static final String[] PICK_MODE = { "Quote", "Random words", "Main Menu" };
    static final String[] GAME_FINISHED = { "New game (same mode)", "Change mode", "Main menu" };
    static final String[] GAME_HISTORY = { "Most recent", "Highest accuracy", "Highest WPM", "Clear records", "Main Menu" };

    // messages
    static final String MAIN_MENU_TITLE = Colours.GREEN + "Typeracer CLI v2" + Colours.RESET;
    static final String MAIN_MENU_FOOTER = Colours.YELLOW + "(ctrl+c to quit...)" + Colours.RESET;
    static final String PICK_MODE_TITLE = "Choose test type";
    static final String QUOTE_TRY_LATER = "Error fetching quote, try again later";
    static final String GAME_HISTORY_TITLE = "Sort by...";
    static final String CONFIRM_CLEAR_TITLE = "Confirm clear?";
    static final String NAV_TIP = "[ < ^ v > Arrow keys to navigate ]";
    static final String EXIT_TIP = "Press ESC twice to quit";
    static final String TIMER_TIP = "Timer starts when you begin typing...";

    // miscellaneous
    static final int WORD_MODE_WORD_CAP = 15;
}
