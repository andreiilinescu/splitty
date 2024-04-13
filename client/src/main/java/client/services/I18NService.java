package client.services;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.Labeled;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

public interface I18NService {

    void createLocale(String language, String script, String region);

    List<Locale> getSupportedLocales();

    /**
     * get the default locale. This is the systems default if contained in the supported locales, english otherwise.
     *
     * @return the systems default language if supported and else English.
     */
    default Locale getDefaultLocale() {
        Locale sysDefault = Locale.getDefault();
        return this.getSupportedLocales().contains(sysDefault) ? sysDefault : Locale.ENGLISH;
    }

    Locale getLocale();

    void setLocale(Locale locale);

    ObjectProperty<Locale> localeProperty();

    String get(String key, Object... args);

    StringBinding createStringBinding(Callable<String> func);

    StringBinding createStringBinding(String key, Object... args);

    void update(Labeled entity);

    void update(Labeled entity, String textToBind);

    void update(TableColumn entity);

    void update(Text entity);

    void update(TextField entity);

}
