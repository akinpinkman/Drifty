package GUI.Forms;

import Enums.Mode;
import Utils.DriftyConstants;
import Utils.Environment;
import Utils.MessageBroker;
import Utils.Utility;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import static Utils.DriftyConstants.DRIFTY_WEBSITE_URL;
import static javafx.scene.layout.AnchorPane.*;

public class Main extends Application {
    private static Main INSTANCE;
    private static MessageBroker M;
    private Stage primaryStage;
    private Scene scene;
    private boolean firstRun = true;

    public static void main(String[] args) {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        Mode.setGUIMode();
        M = new MessageBroker();
        Environment.setMessageBroker(M);
        M.msgLogInfo(DriftyConstants.GUI_APPLICATION_STARTED);
        Environment.initializeEnvironment();
        Utility.setStartTime();
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage = Constants.getStage("Drifty GUI", true);
        this.primaryStage = primaryStage;
        createScene();
        INSTANCE = this;
    }

    private void createScene() {
        AnchorPane ap = new AnchorPane();
        MainGridPane gridPane = new MainGridPane();
        MenuBar menu = menuBar(getMenuItemsOfMenu(), getEditMenu(), getWindowMenu(), getHelpMenu());
        ap.getChildren().add(gridPane);
        ap.getChildren().add(menu);
        placeControl(gridPane, 40, 40, 40, 40);
        placeControl(menu, 0, 0, 0, -1);
        primaryStage.focusedProperty().addListener(((observable, oldValue, newValue) -> {
            if (firstRun) {
                firstRun = false;
                return;
            }
            if (GUI_Logic.isAutoPaste()) {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                if (clipboard.hasString()) {
                    String clipboardText = clipboard.getString();
                    if (Utility.isURL(clipboardText)) {
                        GUI_Logic.pasteFromClipboard(clipboardText);
                    }
                }
            }
        }));
        scene = Constants.getScene(ap);
        scene.setOnContextMenuRequested(e -> getRightClickContextMenu().show(scene.getWindow(), e.getScreenX(), e.getScreenY()));
        primaryStage.setScene(scene);
        primaryStage.show();
        menu.setUseSystemMenuBar(true);
        GUI_Logic.initLogic(gridPane);
    }

    private void placeControl(Node node, double left, double right, double top, double bottom) {
        if (top != -1) {
            setTopAnchor(node, top);
        }
        if (bottom != -1) {
            setBottomAnchor(node, bottom);
        }
        if (left != -1) {
            setLeftAnchor(node, left);
        }
        if (right != -1) {
            setRightAnchor(node, right);
        }
    }

    private Menu getMenuItemsOfMenu() {
        Menu menu = new Menu("Menu");
        MenuItem website = new MenuItem("Project Website");
        website.setOnAction(e -> openWebsite(DRIFTY_WEBSITE_URL));
        MenuItem exit = new MenuItem("Exit");
        exit.setOnAction(e -> {
            M.msgLogInfo(DriftyConstants.GUI_APPLICATION_TERMINATED);
            System.exit(0);
        });
        menu.getItems().setAll(website, exit);
        return menu;
    }

    private MenuBar menuBar(Menu... menus) {
        return new MenuBar(menus);
    }

    private Menu getWindowMenu() {
        Menu menu = new Menu("Window");
        MenuItem fullScreen = new MenuItem("Toggle Full Screen");
        fullScreen.setOnAction(e -> Main.toggleFullScreen());
        menu.getItems().setAll(fullScreen);
        return menu;
    }

    private Menu getHelpMenu() {
        Menu menu = new Menu("Help");
        MenuItem contactUs = new MenuItem("Contact Us");
        MenuItem contribute = new MenuItem("Contribute");
        MenuItem bug = new MenuItem("Report a Bug");
        MenuItem securityVulnerability = new MenuItem("Report a Security Vulnerability");
        MenuItem feature = new MenuItem("Suggest a Feature");
        contactUs.setOnAction(e -> openWebsite("https://saptarshisarkar12.github.io/Drifty/contact.html"));
        contribute.setOnAction(e -> openWebsite("https://github.com/SaptarshiSarkar12/Drifty"));
        bug.setOnAction(e -> openWebsite("https://github.com/SaptarshiSarkar12/Drifty/issues/new?assignees=&labels=bug%2CApp&template=Bug-for-application.yaml&title=%5BBUG%5D+"));
        securityVulnerability.setOnAction(e -> openWebsite("https://github.com/SaptarshiSarkar12/Drifty/security/advisories/new"));
        feature.setOnAction(e -> openWebsite("https://github.com/SaptarshiSarkar12/Drifty/issues/new?assignees=&labels=enhancement%2CApp&template=feature-request-application.yaml&title=%5BFEATURE%5D+"));
        menu.getItems().setAll(contactUs, contribute, bug, securityVulnerability, feature);
        return menu;
    }

    private Menu getEditMenu() {
        Menu menu = new Menu("Edit");
        MenuItem wipeHistory = new MenuItem("Clear Download History");
        wipeHistory.setOnAction(e -> {
            AskYesNo ask = new AskYesNo("Clear Download History", "Are you sure you wish to wipe out all of your download history?\n(This will NOT delete any downloaded files)", false);
            if (ask.getResponse().isYes()) {
                GUI_Logic.clearJobHistory();
            }
        });
        menu.getItems().addAll(wipeHistory);
        return menu;
    }

    private ContextMenu getRightClickContextMenu() {
        MenuItem miAdd = new MenuItem("Add Directory");
        MenuItem miDir = new MenuItem("Manage Directories");
        miAdd.setOnAction(e -> GUI_Logic.getDirectory());
        miDir.setOnAction(e -> {
            ManageFolders manage = new ManageFolders();
            manage.showScene();
            GUI_Logic.resetDownloadFoldersToActiveList();
        });
        ContextMenu contextMenu = new ContextMenu(miAdd, miDir);
        contextMenu.getStyleClass().add("rightClick");
        return contextMenu;
    }

    protected static void openWebsite(String websiteURL) {
        INSTANCE.getHostServices().showDocument(websiteURL);
    }

    public static void toggleFullScreen() {
        INSTANCE.primaryStage.setFullScreen(!INSTANCE.primaryStage.isFullScreen());
    }
}
