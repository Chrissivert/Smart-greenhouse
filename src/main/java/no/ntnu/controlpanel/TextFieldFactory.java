package no.ntnu.controlpanel;

import javafx.scene.control.TextField;

/**
 * Factory class for creating text fields.
 *
 * @version 2023.05.02
 * @author Chris Sivert Sylte
 */
public class TextFieldFactory {
    /**
     * Create text field with defaults text field builder.
     *
     * @return the text field builder
     */
    public static TextFieldBuilder createTextFieldWithDefaults() {
        return new TextFieldBuilder(new TextField());
    }
}