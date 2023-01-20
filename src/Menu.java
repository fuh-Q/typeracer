import java.util.Arrays;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;

interface ItemCallback { void run(Menu m); }

/**
 * A highly customizable class allowing you to create arrow key-operated menus in the terminal
 */
public class Menu implements Receiving {
    private Object parent; // the class from which this menu was initialized
    private String title, footer;
    private String[] entries;
    private KeyListener listener;
    private ItemCallback callback; // this method is ran when the user makes a pick
    private int selected = -1;
    private boolean horizontal = false;
    private static final int[] VALID_KEYCODES = { 28, 57416, 57419, 57421, 57424 }; // enter, up, left, right, down

    public Menu(String t, String[] en, ItemCallback cb) { this(t, en, cb, false); }
    public Menu(String t, String f, String[] en, ItemCallback cb) { this(t, f, en, cb, false); }
    public Menu(String t, String[] en, ItemCallback cb, boolean hz) { this(t, "", en, cb, hz); }
    public Menu(String t, String f, String[] en, ItemCallback cb, boolean hz) {
        if (t.length() == 0) { throw new IllegalArgumentException("Title cannot be empty"); }

        this.title = t;
        this.footer = (f.length() == 0) ? "" : "\n\n" + f;
        this.entries = en;
        this.listener = new KeyListener((Receiving) this);
        this.callback = cb;
        this.horizontal = hz;

        render();
        GlobalScreen.addNativeKeyListener(this.listener);
    }

    // getters
    public int getSelected() { return this.selected; }
    public KeyListener getListener() { return this.listener; }
    public boolean getHorizontal() { return this.horizontal; }
    public Object getParent() { return this.parent; }

    // setters
    public void setParent(Object p) { this.parent = p; }

    public void render() {
        String out = this.title + (this.selected < 0 ? " " + CFG.NAV_TIP + "\n" : "\n");
        if (this.horizontal) { out += "\n"; }
        for (int i = 0; i < this.entries.length; i++) {
            String prefix, suffix = "";
            if (!this.horizontal) { prefix = "\n" + (i == this.selected ? (Colours.BLUE + Colours.BOLD + ">> ") : "> "); }
            else {
                if (i == this.selected) {
                    prefix = Colours.BLUE + Colours.BOLD + "> ";
                    suffix = " <  ";
                }

                else { prefix = ""; suffix = "  "; }
            }

            out += (prefix + this.entries[i] + suffix);
            if (i == this.selected) { out += Colours.RESET; }
        }

        Util.clearConsole();
        System.out.print(out + this.footer + "\n\n");
    }

    private void back() {
        if (this.selected <= 0) {
            this.selected = this.entries.length - 1;
            render();
            return;
        }

        this.selected--;
        render();
    }

    private void next() {
        if (this.selected >= this.entries.length - 1) {
            this.selected = 0;
            render();
            return;
        }

        this.selected++;
        render();
    }

    public void onKey(NativeKeyEvent e) {
        int keyCode = e.getKeyCode();
        if (Arrays.binarySearch(VALID_KEYCODES, keyCode) < 0) { return; }
        switch (keyCode) {
            case 28 -> { // enter key
                if (this.selected < 0) { break; }
                GlobalScreen.removeNativeKeyListener(this.listener);

                Util.clearConsole();
                this.callback.run(this);
            }

            case 57416 -> { back(); } // up arrow
            case 57419 -> { back(); } // left arrow
            case 57421 -> { next(); } // right arrow
            case 57424 -> { next(); } // down arrow
        }
    }
}
