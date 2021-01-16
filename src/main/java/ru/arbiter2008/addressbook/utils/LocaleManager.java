package ru.arbiter2008.addressbook.utils;

import java.util.Locale;

import ru.arbiter2008.addressbook.objects.Lang;


public class LocaleManager {

    // коды языков
    public static final String RU_CODE = "ru";
    public static final String EN_CODE = "en";

    // доступные языки приложения
    public static final Locale RU_LOCALE = new Locale(RU_CODE);
    public static final Locale EN_LOCALE = new Locale(EN_CODE);

    // текущий выбранный язык
    public static Lang currentLang;

}
