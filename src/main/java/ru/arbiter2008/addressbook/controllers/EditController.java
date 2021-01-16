package ru.arbiter2008.addressbook.controllers;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ru.arbiter2008.addressbook.fxml.SpringFxmlView;
import ru.arbiter2008.addressbook.model.Person;
import ru.arbiter2008.addressbook.utils.DialogManager;

import lombok.extern.slf4j.Slf4j;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


@Slf4j
@Component
public class EditController {

    private static final FileChooser fileChooser = new FileChooser(); // окно выбора файла

    @FXML
    private Button btnOk;
    @FXML
    private Button btnCancel;
    @FXML
    private TextField txtFIO;
    @FXML
    private TextArea txtAddress;
    @FXML
    private ImageView imagePhoto;
    @FXML
    private TextField txtPhone;

    private SpringFxmlView editView;
    private Person person;
    private ResourceBundle resourceBundle;
    private boolean saveClicked = false; // для определения нажатой кнопки


    public Person getPerson() {
        return person;
    }


    public void setPerson(Person person) {
        if (null == person) {
            return;
        }
        saveClicked = false;
        this.person = person;
    }


    // загрузить фото
    public void uploadPhoto() {
        log.info("Inside uploadPhoto() method...");
        // диалоговое окно загрузки
        File file = fileChooser.showOpenDialog(imagePhoto.getScene().getWindow());
        if (null != file) { // если загрузили файл
            try {
                imagePhoto.setImage(new Image(new FileInputStream(file))); // сохранить значение во временное поле
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }


    // закрыть диалоговое окно
    public void actionClose(ActionEvent actionEvent) {
        log.info("Inside actionClose(...) method...");
        Node source = (Node) actionEvent.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.hide();
    }


    // сохранить все измененные значения
    public void actionSave(ActionEvent actionEvent) {
        log.info("Inside actionSave(...) method...");
        if (!checkValues()) {
            return;
        }

        // записать все данные в объект
        person.setFio(txtFIO.getText());
        person.setPhone(txtPhone.getText());
        person.setAddress(txtAddress.getText());
        person.setPhoto(convertImage(imagePhoto.getImage()));
        saveClicked = true;

        actionClose(actionEvent); // закрыть диалоговое окно
    }


    // конвертация изображения в массив байтов (чтобы правильно сохранилось в БД)
    private byte[] convertImage(Image image) {
        log.info("Inside convertImage(...) method...");
        BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);

        byte[] bytes = null;
        try (ByteArrayOutputStream s = new ByteArrayOutputStream()) {
            ImageIO.write(bImage, "jpg", s);
            bytes = s.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bytes;
    }


    // все ли данные заполнены
    private boolean checkValues() {
        log.info("Inside checkValues() method...");
        if (txtFIO.getText().trim().length() == 0 || txtPhone.getText().trim().length() == 0) {
            DialogManager.showInfoDialog(resourceBundle.getString("error"), resourceBundle.getString("fill_field"));
            return false;
        }

        return true;
    }


    @FXML
    public void initialize() {
        log.info("Inside initialize() method...");
        this.resourceBundle = editView.getResourceBundle();
        if (null != person) {

            // загрузить данные объекта (при редактировании)
            txtFIO.setText(person.getFio());
            txtPhone.setText(person.getPhone());
            txtAddress.setText(person.getAddress());

            if (null != person.getPhoto()) { // если есть изображение - показать его
                imagePhoto.setImage(new Image(new ByteArrayInputStream(person.getPhoto())));
            }
        }

    }


    public boolean isSaveClicked() {
        log.info("Inside isSaveClicked() method...");
        return saveClicked;
    }


    public void loadPhoto(Event event) {
        log.info("Inside loadPhoto(...) method...");
        uploadPhoto();
    }


    @Autowired
    public void setEditView(SpringFxmlView editView) {
        this.editView = editView;
    }

}
