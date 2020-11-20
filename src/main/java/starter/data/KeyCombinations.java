package starter.data;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

public class KeyCombinations {

	public static final KeyCodeCombination CTRL_SHIFT_C_COMBO = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
	public static final KeyCodeCombination COPY_KEY_COMBO = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN);
	public static final KeyCodeCombination PASTE_KEY_COMBO = new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN);
	public static final KeyCodeCombination D_CTRL_KEY_COMBO = new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN);
	public static final KeyCodeCombination S_ALT_KEY_COMBO = new KeyCodeCombination(KeyCode.S, KeyCombination.ALT_DOWN);
	public static final KeyCodeCombination U_ALT_KEY_COMBO = new KeyCodeCombination(KeyCode.U, KeyCombination.ALT_DOWN);
	public static final KeyCodeCombination DELETE_KEY_COMBO = new KeyCodeCombination(KeyCode.DELETE);
	public static final KeyCodeCombination N_CTRL_KEY_COMBO = new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN);
	public static final KeyCodeCombination UNDO = new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN);
	public static final KeyCodeCombination REDO = new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN);
	
}
