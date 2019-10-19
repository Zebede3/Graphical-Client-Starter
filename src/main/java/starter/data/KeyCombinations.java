package starter.data;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

public class KeyCombinations {

	public static final KeyCodeCombination COPY_ALL_KEY_COMBO = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
	public static final KeyCodeCombination COPY_KEY_COMBO = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN);
	public static final KeyCodeCombination PASTE_KEY_COMBO = new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN);
	public static final KeyCodeCombination D_CTRL_KEY_COMBO = new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN);
	
}
