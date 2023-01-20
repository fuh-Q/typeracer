/**
 * Callbacks for Menu class instances
 */
public class MCBs {
    // just to avoid repetition
    static void openHome() { new Menu(CFG.MAIN_MENU_TITLE, CFG.MAIN_MENU_FOOTER, CFG.MAIN_MENU, MCBs::mainMenu); }

    static void openHistory() { openHistory("timestamp"); }
    static void openHistory(String filter) {
        String title = Util.getTop10Stats(filter);
        new Menu(title + "\n\n" + CFG.GAME_HISTORY_TITLE, CFG.GAME_HISTORY, MCBs::gameHistory, true);
    }

    // actual callbacks start here
    static void mainMenu(Menu m) {
        // 0 = typerace game  1 = typerace history  2 = settings
        switch (m.getSelected()) {
            case 0 -> { new Menu(CFG.PICK_MODE_TITLE, CFG.PICK_MODE, MCBs::chooseGamemode); }
            case 1 -> { openHistory(); }
        }
    }

    static void chooseGamemode(Menu m) {
        // 0 = typing a quote  1 = typing random words
        switch (m.getSelected()) {
            case 0 -> {
                try { new Game(Util.fetchQuote(), GameType.QUOTE); }
                catch (Exception e) { new Menu(e.getMessage(), CFG.PICK_MODE, MCBs::chooseGamemode); }
            }

            case 1 -> { new Game(Util.getWords(CFG.WORD_MODE_WORD_CAP), GameType.WORDS); }
            case 2 -> { openHome(); }
        }
    }

    static void confirmClear(Menu m) {
        // 0 = confirm  1 = abort
        switch (m.getSelected()) {
            case 0 -> {
                Util.dropRecords();
                openHistory();
            }

            case 1 -> { openHistory(); }
        }
    }

    static void gameHistory(Menu m) {
        // 0 = date sort  1 = acc sort  2 = wpm sort  3 = clear records  4 = main menu
        String filter = "";

        switch (m.getSelected()) {
            case 0 -> { filter = "timestamp"; }
            case 1 -> { filter = "accuracy"; }
            case 2 -> { filter = "wpm"; }

            case 3 -> { new Menu(CFG.CONFIRM_CLEAR_TITLE, CFG.CONFIRM_CLEAR, MCBs::confirmClear); }

            case 4 -> { openHome(); }
        }

        if (m.getSelected() <= 2) { openHistory(filter); }
    }

    static void typeraceFinished(Menu m) {
        // 0 = new game (same mode)  1 = change mode  2 = main menu
        String words;
        GameType previousGame = ((Game) m.getParent()).getGameType();
        try { words = (previousGame == GameType.WORDS) ? Util.getWords(CFG.WORD_MODE_WORD_CAP) : Util.fetchQuote(); }
        catch (Exception e) {
            new Menu(e.getMessage(), CFG.PICK_MODE, MCBs::chooseGamemode);
            return;
        }

        switch (m.getSelected()) {
            case 0 -> { new Game(words, previousGame); }
            case 1 -> { new Menu(CFG.PICK_MODE_TITLE, CFG.PICK_MODE, MCBs::chooseGamemode); }
            case 2 -> { openHome(); }
        }
    }
}
