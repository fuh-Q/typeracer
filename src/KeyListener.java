import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

interface Receiving { void onKey(NativeKeyEvent e); }

/**
 * Base key listener class, this is where jnativehook's events get sent to
 */
public class KeyListener implements NativeKeyListener {
	private Receiving receiver;

	// don't care about these, they're just part of the interface
	public void nativeKeyPressed(NativeKeyEvent e) {}
	public void nativeKeyTyped(NativeKeyEvent e) {}

	public KeyListener(Receiving o) { this.receiver = o; }

	public void nativeKeyReleased(NativeKeyEvent e) { this.receiver.onKey(e); }
}
