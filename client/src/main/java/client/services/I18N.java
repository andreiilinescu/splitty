// strongly inspired by https://www.sothawo.com/2016/09/how-to-implement-a-javafx-ui-where-the-language-can-be-changed-dynamically/
package client.services;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Labeled;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.Callable;

public class I18N {
    private static final ObjectProperty<Locale> LOCALE;

    static {
        LOCALE = new SimpleObjectProperty<>(getDefaultLocale());
        LOCALE.addListener((observable, oldVal, newVal) -> Locale.setDefault(newVal));
    }

    /**
     * get the supported Locales.
     *
     * @return List of all locales supported by the application.
     */
    public static List<Locale> getSupportedLocales() {
        return new ArrayList<>(Arrays.asList(
                Locale.ENGLISH,
                new Locale.Builder()
                    .setLanguage("nl")
                    .setScript("Latn")
                    .setRegion("nl").build()
        ));
    }

    /**
     * get the default locale. This is the systems default if contained in the supported locales, english otherwise.
     *
     * @return the systems default language if supported and else English.
     */
    public static Locale getDefaultLocale() {
        Locale sysDefault = Locale.getDefault();
        return getSupportedLocales().contains(sysDefault) ? sysDefault : Locale.ENGLISH;
    }

    public static Locale getLocale() {
        return LOCALE.get();
    }

    public static void setLocale(Locale locale) {
        localeProperty().set(locale);
        Locale.setDefault(locale);
    }

    public static ObjectProperty<Locale> localeProperty() {
        return LOCALE;
    }


    /**
     * gets the string with the given key from the resource bundle for the current locale and uses it as first argument
     * to MessageFormat.format, passing in the optional args and returning the result.
     *
     * @param key
     *         message key
     * @param args
     *         optional arguments for the message
     * @return localized formatted string
     */
    public static String get(final String key, final Object... args) {
        ResourceBundle bundle = ResourceBundle.getBundle("languages", getLocale());
        String retStr;
        try {
            retStr = bundle.getString(key);
        }
        catch (MissingResourceException e) {
            try {
                ResourceBundle bundle2 = ResourceBundle.getBundle("languages", Locale.ENGLISH);
                retStr = bundle2.getString(key);
            }
            catch (MissingResourceException e2) {
                retStr = "missing: " + key;
            }
        }
        return MessageFormat.format(retStr, args);
    }

    /**
     * creates a String Binding to a localized String that is computed by calling the given func
     *
     * @param func
     *         function called on every change
     * @return StringBinding
     */
    public static StringBinding createStringBinding(Callable<String> func) {
        return Bindings.createStringBinding(func, LOCALE);
    }

    /**
     * creates a String binding to a localized String for the given message bundle key
     *
     * @param key
     *         key
     * @return String binding
     */
    public static StringBinding createStringBinding(final String key, Object... args) {
        return Bindings.createStringBinding(() -> get(key, args), LOCALE);
    }

    public static void update(Labeled entity) {
        entity.textProperty().bind(createStringBinding(entity.getText()));
    }

    public static void update(Labeled entity, String textToBind) {
        entity.textProperty().bind(createStringBinding(textToBind));
    }

    public static void update(TableColumn entity) {
        entity.textProperty().bind(createStringBinding(entity.getText()));
    }

    public static void update(Text entity) {
        entity.textProperty().bind(createStringBinding(entity.getText()));
    }

    public static void update(TextField entity) {
        entity.promptTextProperty().bind(createStringBinding(entity.getPromptText()));
    }
}
