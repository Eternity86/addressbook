package ru.arbiter2008.addressbook.fxml;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import static java.util.ResourceBundle.getBundle;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import ru.arbiter2008.addressbook.objects.Lang;

import lombok.extern.slf4j.Slf4j;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;


@Slf4j
public abstract class SpringFxmlView implements ApplicationContextAware, Observer {

    private static final String BUNDLES_FOLDER = "bundles/Locale";
    protected ObjectProperty<Object> presenterProperty;
    protected FXMLLoader fxmlLoader;
    protected ResourceBundle bundle;
    protected URL resource;
    protected Pane parent;
    protected Pane rootPane;
    private ApplicationContext applicationContext;


    public SpringFxmlView() {
        log.info("Inside SpringFxmlView() constructor...");
        this.presenterProperty = new SimpleObjectProperty<>();
        this.resource = getClass().getResource(getFxmlName());
        this.bundle = getResourceBundle(getBundleName());
    }


    public SpringFxmlView(URL resource) {
        log.info("Inside SpringFxmlView(...) constructor...");
        try {
            this.presenterProperty = new SimpleObjectProperty<>();
            this.resource = resource;
        } catch (Exception e) {
            log.error(e.getMessage() + " " + e.getCause());
        }

    }


    static String stripEnding(String clazz) {
        log.info("Inside stripEnding(...) method...");
        if (!clazz.endsWith("view")) {
            return clazz;
        }

        return clazz.substring(0, clazz.lastIndexOf("view"));
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        log.info("Inside setApplicationContext(...) method...");
        if (null != this.applicationContext) {
            return;
        }

        this.applicationContext = applicationContext;
    }


    @Override
    public void update(Observable o, Object arg) {
        log.info("Inside update(...) method...");
        Lang lang = (Lang) arg;

        if (null == rootPane) {
            rootPane = parent; // один раз в начале сохраняем ссылку на корневой контейнер
        }

        // получить новое дерево компонентов с нужной локалью
        parent = (Pane) getView(lang.getLocale());

        // заменить старые дочерние компоненты на новые - с другой локалью
        rootPane.getChildren().setAll(parent.getChildren());
    }


    private Object createControllerForType(Class<?> type) {
        return this.applicationContext.getBean(type);
    }


    FXMLLoader loadSynchronously(URL resource, ResourceBundle bundle) throws IllegalStateException {

        FXMLLoader loader = new FXMLLoader(resource, bundle);
        loader.setControllerFactory(this::createControllerForType);

        try {
            loader.load();
        } catch (IOException ex) {
            throw new IllegalStateException("Can't load " + getConventionalName(), ex);
        }

        return loader;
    }


    void ensureFxmlLoaderInitialized() {

        this.fxmlLoader = loadSynchronously(resource, bundle);
        this.presenterProperty.set(this.fxmlLoader.getController());

        if (this.fxmlLoader.getController() instanceof Observable) {
            ((Observable) this.fxmlLoader.getController()).addObserver(this);
        }

    }


    /**
     * Initializes the view by loading the FXML (if not happened yet) and returns
     * the top Node (parent) specified in the FXML file.
     *
     * @return
     */
    public Parent getView() {
        log.info("Inside getView() method...");
        bundle = ResourceBundle.getBundle(BUNDLES_FOLDER);

        ensureFxmlLoaderInitialized();

        parent = fxmlLoader.getRoot();
        addCSSIfAvailable(parent);

        return parent;
    }


    public Parent getView(Locale locale) {
        log.info("Inside getView(Locale...) method...");
        try {
            bundle = ResourceBundle.getBundle(BUNDLES_FOLDER, locale);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ensureFxmlLoaderInitialized();

        parent = fxmlLoader.getRoot();
        addCSSIfAvailable(parent);

        return parent;
    }


    /**
     * Initializes the view synchronously and invokes the consumer with the created
     * parent Node within the FX UI thread.
     *
     * @param consumer - an object interested in received the {@link Parent} as
     *                 callback
     */
    public void getView(Consumer<Parent> consumer) {
        log.info("Inside getView(Consumer ...) method... ");
        CompletableFuture.supplyAsync(this::getView, Platform::runLater).thenAccept(consumer);
    }


    /**
     * Scene Builder creates for each FXML document a root container. This method
     * omits the root container (e.g. {@link javafx.scene.layout.AnchorPane}) and
     * gives you the access to its first child.
     *
     * @return the first child of the {@link javafx.scene.layout.AnchorPane}
     */
    public Node getViewWithoutRootContainer() {

        ObservableList<Node> children = getView().getChildrenUnmodifiable();
        if (children.isEmpty()) {
            return null;
        }

        return children.listIterator().next();
    }


    void addCSSIfAvailable(Parent parent) {
        log.info("Inside addCSSIfAvailable(Parent...) method...");
        URL uri = getClass().getResource(getStyleSheetName());
        if (null == uri) {
            return;
        }

        String uriToCss = uri.toExternalForm();
        parent.getStylesheets().add(uriToCss);
    }


    private String getStyleSheetName() {
        return getConventionalName(".css");
    }


    /**
     * In case the view was not initialized yet, the conventional fxml
     * (airhacks.fxml for the AirhacksView and AirhacksPresenter) are loaded and the
     * specified presenter / controller is going to be constructed and returned.
     *
     * @return the corresponding controller / presenter (usually for a AirhacksView
     * the AirhacksPresenter)
     */
    public Object getPresenter() {

        ensureFxmlLoaderInitialized();

        return this.presenterProperty.get();
    }


    /**
     * Does not initialize the view. Only registers the Consumer and waits until the
     * the view is going to be created / the method FXMLView#getView or
     * FXMLView#getViewAsync invoked.
     *
     * @param presenterConsumer listener for the presenter construction
     */
    public void getPresenter(Consumer<Object> presenterConsumer) {

        this.presenterProperty.addListener((ObservableValue<?> o, Object oldValue, Object newValue) -> {
            presenterConsumer.accept(newValue);
        });
    }


    /**
     * @param ending the suffix to append
     * @return the conventional name with stripped ending
     */
    protected String getConventionalName(String ending) {
        return getConventionalName() + ending;
    }


    /**
     * @return the name of the view without the "View" prefix in lowerCase. For
     * AirhacksView just airhacks is going to be returned.
     */
    protected String getConventionalName() {
        return stripEnding(getClass().getSimpleName().toLowerCase());
    }


    String getBundleName() {
        return getClass().getPackage().getName() + "." + getConventionalName();
    }


    /**
     * @return the name of the fxml file derived from the FXML view. e.g. The name
     * for the AirhacksView is going to be airhacks.fxml.
     */
    final String getFxmlName() {
        return getConventionalName(".fxml");
    }


    private ResourceBundle getResourceBundle(String name) {
        try {
            return getBundle(name);
        } catch (MissingResourceException ex) {
            return null;
        }
    }


    /**
     * @return an existing resource bundle, or null
     */
    public ResourceBundle getResourceBundle() {
        return this.bundle;
    }

}
