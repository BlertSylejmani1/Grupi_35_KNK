package com.smartinventory.service;

import java.util.Locale;
import java.util.ResourceBundle;

public final class LanguageService {
    private static Locale locale = Locale.ENGLISH;

    private LanguageService() {
    }

    public static void setLanguage(String languageTag) {
        locale = Locale.forLanguageTag(languageTag);
    }

    public static ResourceBundle getBundle() {
        return ResourceBundle.getBundle("com.smartinventory.i18n.messages", locale);
    }

    public static String get(String key) {
        return getBundle().getString(key);
    }
}
