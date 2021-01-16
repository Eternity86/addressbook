package ru.arbiter2008.addressbook;

import java.util.Arrays;

import com.sun.javafx.application.LauncherImpl;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import ru.arbiter2008.addressbook.preloader.StartPreloader;

import lombok.extern.slf4j.Slf4j;

import javafx.application.Application;
import javafx.application.Preloader;
import javafx.stage.Stage;


// Спец. класс для "соединения" JavaFX со Spring
// Основная задача - загрузить JavaFX контейнер (контекст) внутри Spring-контейнера
@Slf4j
public abstract class JavaFxSpringIntegrator extends Application {

    private static String[] savedArgs; // аргументы при старте (если они есть)

    // Spring контекст для приложения - его нужно связать с JavFX контекстом
    private ConfigurableApplicationContext applicationContext;


    // старт javafx приложения
    protected static void launchSpringJavaFXApp(Class<? extends JavaFxSpringIntegrator> appClass, String[] args) {
        log.info("Inside launchSpringJavaFXApp() method...");
        JavaFxSpringIntegrator.savedArgs = args;
        // стартуем javafx приложение с прелоадером (окном инициализации)
        LauncherImpl.launchApplication(appClass, StartPreloader.class, args);
    }


    @Override
    public void stop() throws Exception {
        log.info("Inside stop() method...");
        super.stop();
        applicationContext.close();
    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        log.info("Inside start() method...");
        try {
            applicationContext = SpringApplication.run(getClass(), savedArgs);

            // главный момент - "присоединяем" экземпляр Application (который стартует JavaFX приложение) к контексту Spring
            applicationContext.getAutowireCapableBeanFactory().autowireBean(this);

            printBeans();

            // уведомить прелоадер, что загрузка прошла полностью (чтобы скрыть окно инициализации)
            LauncherImpl.notifyPreloader(this, new Preloader.ProgressNotification(100));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }


    // печать всех Spring-бинов
    private void printBeans() {
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        Arrays.sort(beanNames);
        for (String beanName : beanNames) {
            log.info(beanName);
        }
    }

}
