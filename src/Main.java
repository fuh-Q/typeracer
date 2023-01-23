import com.github.kwhat.jnativehook.GlobalScreen;

public class Main {
    static void onShutDown() {
        try { GlobalScreen.unregisterNativeHook(); }
        catch (Exception e) { e.printStackTrace(); }
    }

    public static void main(String[] args) {
        try {
            GlobalScreen.registerNativeHook();
        }
        catch (Exception e) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(e.getMessage());

            System.exit(1);
        }

        // start main menu
        MCBs.openHome();
        Runtime.getRuntime().addShutdownHook(new Thread(Main::onShutDown));
    }
}
