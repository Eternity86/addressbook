package ru.arbiter2008.addressbook.fxml;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;


// Загрузка FXML в среде Spring контейнера
@Slf4j
@Component
public class MainView extends SpringFxmlView {

    private static final String FXML_MAIN = "ru.arbiter2008.addressbook/fxml/main.fxml";


    public MainView() {
        super(MainView.class.getClassLoader().getResource(FXML_MAIN));
        log.info("Inside MainView() constructor...");
    }

}
