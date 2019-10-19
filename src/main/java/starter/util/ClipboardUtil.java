package starter.util;

import java.util.Collection;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import starter.gson.GsonFactory;
import starter.models.AccountConfiguration;

public class ClipboardUtil {

	// note that this turns the collection to an array
	public static void copyAccountsToClipboard(Collection<AccountConfiguration> accs) {
		final String s = GsonFactory.buildGson().toJson(accs.toArray(new AccountConfiguration[0]));
		set(s);
	}
	
	public static void set(String text) {
		final ClipboardContent contents = new ClipboardContent();
		contents.put(DataFormat.PLAIN_TEXT, text);
		Clipboard.getSystemClipboard().setContent(contents);
	}
	
	public static String getText() {
		return (String) Clipboard.getSystemClipboard().getContent(DataFormat.PLAIN_TEXT);
	}
	
	public static <T> T grabFromClipboard(Class<T> klass) {
		
		final Clipboard clipboard = Clipboard.getSystemClipboard();
		
		if (!clipboard.hasContent(DataFormat.PLAIN_TEXT))
			return null;
		
		final String content = (String) clipboard.getContent(DataFormat.PLAIN_TEXT);
		if (content == null)
			return null;
		
		final T item = GsonFactory.buildGson().fromJson(content, klass);
		if (item == null)
			return null;
		
		return item;
	}
	
}
