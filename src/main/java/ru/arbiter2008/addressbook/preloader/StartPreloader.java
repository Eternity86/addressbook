package ru.arbiter2008.addressbook.preloader;

import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


// Окно предзагрузки при старте приложения
// Используется готовый класс из Preloader из пакетов JavaFX
// https://docs.oracle.com/javafx/2/deployment/preloaders.htm
public class StartPreloader extends Preloader {

    private Stage preloaderStage;
    private Scene scene;
    private ProgressIndicator bar;


    @Override
    public void start(Stage primaryStage) throws Exception {
        this.preloaderStage = primaryStage;
        preloaderStage.initStyle(StageStyle.UNDECORATED);
        preloaderStage.setScene(scene);
        preloaderStage.show();
    }


    @Override
    public void init() {

        Platform.runLater(() -> {
            Label title = new Label("loading...");

            title.setTextAlignment(TextAlignment.CENTER);
            bar = new ProgressIndicator();
            bar.setPadding(new Insets(2));
            bar.setMaxSize(40, 40);

            HBox root = new HBox(bar, title);

            root.setAlignment(Pos.CENTER);

            scene = new Scene(root, 200, 100);
        });
    }


    @Override
    public void handleApplicationNotification(PreloaderNotification info) {
        if (info instanceof ProgressNotification) {
            if (((ProgressNotification) info).getProgress() == 100) {
                preloaderStage.hide(); // если загрузка 100% - закрываем
            }
        }
    }

}
