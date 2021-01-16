package ru.arbiter2008.addressbook.objects;

import java.util.Locale;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


/**
 * Объект для хранения данных языка
 * (в точ числе индекс, чтобы добавлять в выпадающий список).
 */
@Getter
@Setter
@AllArgsConstructor
public class Lang {

    private int index; // индекс нужен для порядка отображения
    private String code; // краткий код языка
    private String name;
    private Locale locale;


    @Override
    public String toString() { // текстовое представление объектов (например, для отображения в выпад. списке языков)
        return name;
    }

}
