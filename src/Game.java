import java.util.Arrays;
import java.util.Date;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;

enum GameType { QUOTE, WORDS }

/**
 * Typeracer game. Has random words mode, as well as quote mode
 */
public class Game implements Receiving {
    private GameType gameType;

    private String text, internalText; // text is displayed to the user, internalText is kept track of for the game
    private Character[] typedChars;
    private Boolean[] beenTyped; // only the first key press for each character is counted for acc
    private KeyListener listener;
    private int beenTypedCounter, numBreaks, rights, curPos = 0; // numBreaks = # of newlines from line breaking || curPos = the "cursor"
    private double acc = 100.0;
    private boolean pendingQuit = false; // hit esc twice to quit
    private Date startedAt;
    private static final int[] VALID_KEYCODES = {
        1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
        11, 12, 14, 16, 17, 18, 19,
        20, 21, 22, 23, 24, 25, 30,
        31, 32, 33, 34, 35, 36, 37,
        38, 39, 40, 44, 45, 46, 47,
        48, 49, 50, 51, 52, 53, 57,
    }; // the alphabet, numbers, some punctuation, space, backspace, and escape

    public Game(String text, GameType type) {
        this.gameType = type;

        this.internalText = (type == GameType.QUOTE) ? transformQuote(text) : text;
        this.text = Util.wrapIt(text, 120);
        this.numBreaks = this.text.length() - text.length();
        this.listener = new KeyListener((Receiving) this);
        this.typedChars = new Character[text.length()];
        this.beenTyped = new Boolean[text.length()];

        render();
        GlobalScreen.addNativeKeyListener(this.listener);
    }

    // getters
    public GameType getGameType() { return this.gameType; }

    private void render() {
        String out = Colours.BLUE;
        int currentColour = 0; // flag im gonna use to keep track of the colour, 0 = grey  1 = green  2 = red

        // i is for text, j is for internalText
        // this is done since both sequences' lengths are different because one has formatting
        // and the other one doesn't
        for (int i = 0, j = -1; i < this.typedChars.length + numBreaks; i++) {
            char currentChar = this.text.charAt(i);
            if (currentChar == '\n') {
                out += "\n";
                continue;
            }

            else { j++; }
            char currentInternalChar = this.internalText.charAt(j);

            // if the character hasn't been typed
            if (this.typedChars[j] == null && currentColour != 0) {
                if (currentColour == 1 || currentColour == 2) { out += Colours.RESET; }
                currentColour = 0;
                out += Colours.BLUE;
            }

            else if (this.typedChars[j] != null) {
                // if the character is correct
                if (this.typedChars[j] == currentInternalChar && currentColour != 1) {
                    if (currentColour == 2) { out += Colours.RESET; }
                    currentColour = 1;
                    out += (Colours.GREEN + Colours.BOLD);
                }

                // if the character is incorrect
                else if (this.typedChars[j] != currentInternalChar && currentColour != 2) {
                    if (currentColour == 1) { out += Colours.RESET; }
                    currentColour = 2;
                    out += (Colours.RED_BG + Colours.WHITE + Colours.UNDERLINE);
                }
            }

            out += currentChar;
        }

        out += Colours.RESET;
        if (this.startedAt == null) { out += ("\n\n" + CFG.TIMER_TIP); } // start timer on first key press
        Util.clearConsole();
        System.out.print(out + "\n\n");
    }

    private String transformQuote(String quote) {
        // the problem is, i don't think there's a way to determine if shift is being held down
        // this may compromise the integrity of the idea of this being an actual typing test
        // but let's just pretend that it actually matters if you shift

        quote = quote
            .replace(":", ";")
            .replace("?", "/")
            .replace("$", "4")
            .replace("!", "1")
            .replace("(", "9")
            .replace(")", "0")
            .replace("_", "-")
            .replace("\"", "'");

        return quote.toLowerCase();
    }

    private String transformKeyText(String name) {
        // jnativehook returns certain keys' text as the literal name of the symbol
        // so we need to correct for that

        switch (name) {
            case "Space" -> { return " "; }
            case "Slash" -> { return "/"; }
            case "Minus" -> { return "-"; }
            case "Comma" -> { return ","; }
            case "Period" -> { return "."; }
            case "Quote" -> { return "\'"; }
            case "Semicolon" -> { return ";"; }
            default -> { return ""; }
        }
    }

    private void verifyKey(char c) {
        if (this.beenTyped[this.curPos] != null) { return; }
        else {
            this.beenTyped[this.curPos] = true;
            this.beenTypedCounter++;
        }

        if (c == this.internalText.charAt(this.curPos)) { this.rights++; }
        this.acc = (double) this.rights / this.beenTypedCounter * 100;
    }

    private void handleKey(String key) {
        if (this.startedAt == null) { this.startedAt = new Date(); } // start timer on first key press

        char toCheck;
        if (key.length() > 1) { key = transformKeyText(key); }

        toCheck = key.toLowerCase().charAt(0);
        this.typedChars[this.curPos] = toCheck;

        verifyKey(toCheck);
        this.curPos++;
    }

    private void handleBackSpace() {
        if (this.curPos > 0) { this.curPos--; }
        this.typedChars[this.curPos] = null;
    }

    private void handleEscape() {
        if (!this.pendingQuit) {
            this.pendingQuit = true;
            System.out.print(CFG.EXIT_TIP + "\n\n");
            return;
        }

        GlobalScreen.removeNativeKeyListener(this.listener);

        String message = "You have exited your game";
        Menu gameFinished = new Menu(message, CFG.GAME_FINISHED, MCBs::typeraceFinished);
        gameFinished.setParent(this);
    }

    private void handleFinish() {
        GlobalScreen.removeNativeKeyListener(this.listener);

        double weighed = (this.acc + Util.levenshtein(Util.joinCharacterArr(this.typedChars), this.internalText)) / 2;
        double wpm = Util.getWPM(this.internalText, this.startedAt, weighed);
        String stats = String.format(
            "%s %.2f %% acc %s|%s %.2f wpm %s",
            Colours.GREEN,
            weighed,
            Colours.RESET, Colours.YELLOW,
            wpm,
            Colours.RESET
        );

        Util.clearConsole();
        Util.saveStats(weighed, wpm, this.gameType.toString());

        Menu gameFinished = new Menu(stats, CFG.GAME_FINISHED, MCBs::typeraceFinished);
        gameFinished.setParent(this);
    }

    public void onKey(NativeKeyEvent e) {
        int keyCode = e.getKeyCode();
        if (Arrays.binarySearch(VALID_KEYCODES, keyCode) < 0) { return; }
        switch (keyCode) {
            case 1 -> { handleEscape(); }
            case 14 -> { handleBackSpace(); }
            default -> { handleKey(NativeKeyEvent.getKeyText(keyCode)); }
        }

        if (this.curPos == this.typedChars.length) { handleFinish(); }
        else if (keyCode != 1) {
            if (this.pendingQuit) { this.pendingQuit = false; }
            render();
        }
    }
}
