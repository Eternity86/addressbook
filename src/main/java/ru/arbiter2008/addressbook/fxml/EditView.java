package ru.arbiter2008.addressbook.fxml;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;


// Загрузка FXML в среде Spring-контейнера
@Slf4j
@Component
public class EditView extends SpringFxmlView {

    private static final String FXML_EDIT = "ru.arbiter2008.addressbook/fxml/edit.fxml";


    public EditView() {
        super(MainView.class.getClassLoader().getResource(FXML_EDIT));
        log.info("Inside EditView() constructor...");
    }

}
