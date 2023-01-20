import com.github.kwhat.jnativehook.GlobalScreen;

public class Main {
    public static void main(String[] args) {
        try {
            GlobalScreen.registerNativeHook();
        }
        catch (Exception ex) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());

            System.exit(1);
        }

        // start main menu
        MCBs.openHome();
    }
}
