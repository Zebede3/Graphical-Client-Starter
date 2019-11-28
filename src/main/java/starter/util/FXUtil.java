package starter.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

public class FXUtil {

	@SafeVarargs
	public static <T> ObservableList<T> merge(ObservableList<T>... lists) {
		
        final ObservableList<T> list = FXCollections.observableArrayList();
        
        for (ObservableList<T> l : lists) {
            list.addAll(l);
            l.addListener((javafx.collections.ListChangeListener.Change<? extends T> c) -> {
                while (c.next()) {
                    if (c.wasAdded()) {
                        list.addAll(c.getAddedSubList());
                    }
                    if (c.wasRemoved()) {
                        list.removeAll(c.getRemoved());
                    }
                }
            });
        }

        return list;
	}
	
	public static String colorToCssRgb(Color color) {
		return "rgb(" + color.getRed() * 255 + "," + color.getGreen() * 255 + "," + color.getBlue() * 255 + ");";
	}

}
