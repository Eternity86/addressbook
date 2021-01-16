package ru.arbiter2008.addressbook.controllers;

import java.lang.reflect.Method;
import java.util.Observable;
import java.util.ResourceBundle;

import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import ru.arbiter2008.addressbook.fxml.EditView;
import ru.arbiter2008.addressbook.fxml.MainView;
import ru.arbiter2008.addressbook.model.Person;
import ru.arbiter2008.addressbook.objects.Lang;
import ru.arbiter2008.addressbook.service.IAddressBook;
import ru.arbiter2008.addressbook.utils.DialogManager;
import ru.arbiter2008.addressbook.utils.LocaleManager;

import lombok.extern.slf4j.Slf4j;

import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;


@Slf4j
@Component
public class MainController extends Observable {

    public static final int MAX_PAGE_SHOW = 5;
    private static final int PAGE_SIZE = 15;
    private static final String RU_CODE = "ru";
    private static final String EN_CODE = "en";

    // загрузчик fxml файлов
    private final FXMLLoader fxmlLoader = new FXMLLoader();

    @FXML
    private Button btnAdd;
    @FXML
    private Button btnEdit;
    @FXML
    private Button btnDelete;
    @FXML
    private CustomTextField txtSearch;
    @FXML
    private Button btnSearch;
    @FXML
    private TableView<Person> tableAddressBook;
    @FXML
    private TableColumn<Person, String> columnFIO;
    @FXML
    private TableColumn<Person, String> columnPhone;
    @FXML
    private Label labelCount;
    @FXML
    private Pagination pagination;
    @FXML
    private ComboBox<Lang> comboLocales;

    private Parent fxmlEdit;
    private Stage editDialogStage;
    private ResourceBundle resourceBundle;
    private ObservableList<Person> personList;
    private Page<Person> page; // текущие постраничные данные
    private IAddressBook addressBook;
    private MainView mainView;
    private EditView editView;
    private EditController editController;


    @FXML
    public void initialize() {
        log.info("Inside initialize() method...");
        pagination.setMaxPageIndicatorCount(MAX_PAGE_SHOW); // настройка компонента постраничности
        this.resourceBundle = mainView.getResourceBundle(); // переводы

        // типы полей таблицы
        columnFIO.setCellValueFactory(new PropertyValueFactory<>("fio"));
        columnPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));

        setupClearButtonField(txtSearch); // текстовое поле с кнопкой очистки

        initUI(); // инициализация UI - все кнопки, данные, языки и пр.
        initListeners();  // слушатели различных событий
    }


    // текстовое поле с кнопкой очистки (код взят из документации)
    // https://controlsfx.bitbucket.io/org/controlsfx/control/textfield/CustomTextField.html
    private void setupClearButtonField(CustomTextField customTextField) {
        log.info("Inside setupClearButtonField(...) method...");
        try {
            Method m = TextFields.class.getDeclaredMethod("setupClearButtonField", TextField.class, ObjectProperty.class);
            m.setAccessible(true);
            m.invoke(null, customTextField, customTextField.rightProperty());

            customTextField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (txtSearch.getText().trim().length() == 0) { // если полностью очистили текст - вернуть все записи
                    actionSearch(null); // загрузка всех данных
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void initUI() {
        log.info("Inside initUI() method...");
        fillLangComboBox();
        fillTable();
    }


    private void fillPagination(Page<Person> page) {
        log.info("Inside fillPagination(...) method...");
        pagination.setDisable(page.getTotalPages() <= 1);

        pagination.setPageCount(page.getTotalPages());

        personList = FXCollections.observableArrayList(page.getContent());
        tableAddressBook.setItems(personList);
    }


    private void fillLangComboBox() {
        log.info("Inside fillLangComboBox() method...");
        Lang langRU = new Lang(0, RU_CODE, resourceBundle.getString("ru"), LocaleManager.RU_LOCALE);
        Lang langEN = new Lang(1, EN_CODE, resourceBundle.getString("en"), LocaleManager.EN_LOCALE);

        comboLocales.getItems().add(langRU);
        comboLocales.getItems().add(langEN);

        if (null == LocaleManager.currentLang) {
            comboLocales.getSelectionModel().select(0);
            LocaleManager.currentLang = langRU;
        } else {
            comboLocales.getSelectionModel().select(LocaleManager.currentLang.getIndex());
        }
    }


    // все возможные слушатели событий
    private void initListeners() {
        log.info("Inside initListeners() method...");
        // слушает двойное нажатие для редактирования записи
        tableAddressBook.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                btnEdit.fire(); // нажимаем кнопку редактирования
            }
        });

        // слушает переключение языка
        comboLocales.getSelectionModel().selectedItemProperty().addListener((ov, t, t1) -> {
            Lang selectedLang = ov.getValue();
            LocaleManager.currentLang = selectedLang;

            // уведомить всех слушателей, что произошла смена языка
            setChanged();
            notifyObservers(selectedLang);
        });

        // переход по страницам данных
        pagination.currentPageIndexProperty().addListener((observable, oldValue, newValue) -> fillTable(newValue.intValue()));
    }


    // обновляет счетчик - кол-во найденных данных
    private void updateCountLabel(long count) {
        log.info("Inside updateCountLabel(...) method...");
        labelCount.setText(resourceBundle.getString("count") + ": " + count);
    }


    // выполнить действие в зависимости от нажатой кнопки
    public void actionButtonPressed(ActionEvent actionEvent) {
        log.info("Inside actionButtonPressed(...) method...");
        Object source = actionEvent.getSource();

        // если нажата не кнопка - выходим из метода
        if (!(source instanceof Button)) {
            return;
        }

        // получить выбранного person (если редактирование или удаление)
        Person selectedPerson = (Person) tableAddressBook.getSelectionModel().getSelectedItem();

        boolean dataChanged = false; // были ли изменены данные (обновлять таблицу потом или нет)

        // определить нажатую кнопку
        Button clickedButton = (Button) source;

        switch (clickedButton.getId()) {
            case "btnAdd":
                editController.setPerson(new Person());
                showDialog();

                if (editController.isSaveClicked()) {
                    addressBook.add(editController.getPerson());
                    dataChanged = true;
                }

                break;

            case "btnEdit":
                if (!personIsSelected(selectedPerson)) {
                    return;
                }
                editController.setPerson(selectedPerson);
                showDialog();

                if (editController.isSaveClicked()) {
                    // коллекция в addressBookImpl и так обновляется, т.к. мы ее редактируем в диалоговом окне и сохраняем при нажатии на ОК
                    addressBook.update(selectedPerson);
                    dataChanged = true;
                }

                break;

            case "btnDelete":
                if (!personIsSelected(selectedPerson) || !(confirmDelete())) {
                    return;
                }

                dataChanged = true;
                addressBook.delete(selectedPerson);
                break;
        }


        // обновить список, если запись была изменена
        if (dataChanged) {
            actionSearch(actionEvent);
        }

    }


    private boolean confirmDelete() {
        log.info("Inside confirmDelete() method...");
        return DialogManager.showConfirmDialog(resourceBundle
                .getString("confirm"), resourceBundle.getString("confirm_delete"))
                .get() == ButtonType.OK;

    }


    private boolean personIsSelected(Person selectedPerson) {
        log.info("Inside personIsSelected(Person...) method ...");
        if (null == selectedPerson) {
            DialogManager.showInfoDialog(resourceBundle.getString("error"), resourceBundle.getString("select_person"));
            return false;
        }
        return true;
    }


    // диалоговое окно при создании/редактировании
    private void showDialog() {

        if (null == editDialogStage) {
            editDialogStage = new Stage();

            editDialogStage.setMinHeight(150);
            editDialogStage.setMinWidth(300);
            editDialogStage.setResizable(false);
            editDialogStage.initModality(Modality.WINDOW_MODAL);
            editDialogStage.initOwner(comboLocales.getParent().getScene().getWindow());
            editDialogStage.centerOnScreen();
        }

        editDialogStage.setScene(new Scene(editView.getView(LocaleManager.currentLang.getLocale())));

        editDialogStage.setTitle(resourceBundle.getString("edit"));

        editDialogStage.showAndWait(); // для ожидания закрытия модального окна

    }


    public void actionSearch(ActionEvent actionEvent) {
        fillTable();
    }


    // для показа данных с первой страницы
    private void fillTable() {
        if (txtSearch.getText().trim().length() == 0) {
            page = addressBook.findAll(0, PAGE_SIZE);
        } else {
            page = addressBook.findAll(0, PAGE_SIZE, txtSearch.getText());
        }
        fillPagination(page);
        pagination.setCurrentPageIndex(0);
        updateCountLabel(page.getTotalElements());

    }


    // для показа данных с любой страницы
    private void fillTable(int pageNumber) {
        if (txtSearch.getText().trim().length() == 0) {
            page = addressBook.findAll(pageNumber, PAGE_SIZE);
        } else {
            page = addressBook.findAll(pageNumber, PAGE_SIZE, txtSearch.getText());
        }
        fillPagination(page);
        updateCountLabel(page.getTotalElements());
    }


    @Autowired
    public void setAddressBook(IAddressBook addressBook) {
        this.addressBook = addressBook;
    }


    @Autowired
    public void setMainView(MainView mainView) {
        this.mainView = mainView;
    }


    @Autowired
    public void setEditView(EditView editView) {
        this.editView = editView;
    }


    @Autowired
    public void setEditController(EditController editController) {
        this.editController = editController;
    }

}
