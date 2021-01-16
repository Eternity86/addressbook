package ru.arbiter2008.addressbook;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import ru.arbiter2008.addressbook.fxml.MainView;
import ru.arbiter2008.addressbook.service.IAddressBook;
import ru.arbiter2008.addressbook.utils.LocaleManager;

import lombok.extern.slf4j.Slf4j;

import javafx.scene.Scene;
import javafx.stage.Stage;


// загрузчик приложения с помощью SpringBoot
// Наследуется от спец. класса интегратора JavaFxSpringIntegrator
@Slf4j
@SpringBootApplication
public class AddressBook extends JavaFxSpringIntegrator {

    private MainView mainView;
    private IAddressBook addressBookService;
    private Stage primaryStage;


    public static void main(String[] args) {
        // старт приложения
        log.info("Inside main() method...");
        launchSpringJavaFXApp(AddressBook.class, args);
    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        log.info("Inside start() method...");
        try {
            super.start(primaryStage);
            loadMainFXML(LocaleManager.RU_LOCALE, primaryStage);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

    }


    @Override
    public void init() throws Exception {
        log.info("Inside init() method...");
        super.init();
    }


    @Override
    public void stop() {
        log.info("Inside stop() method...");
        System.exit(0);
    }


    // загружает дерево компонентов и возвращает в виде VBox (корневой элемент в FXML)
    private void loadMainFXML(Locale locale, Stage primaryStage) {
        try {
            log.info("Inside loadMainFXML() method...");
            this.primaryStage = primaryStage;
            primaryStage.setScene(new Scene(mainView.getView(locale)));
            primaryStage.setMinHeight(700);
            primaryStage.setMinWidth(600);
            primaryStage.centerOnScreen();
            primaryStage.setTitle(mainView.getResourceBundle().getString("address_book"));
            primaryStage.show();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }


    @Autowired
    public void setMainView(MainView mainView) {
        this.mainView = mainView;
    }


    @Autowired
    public void setAddressBookService(IAddressBook addressBookService) {
        this.addressBookService = addressBookService;
    }

}
