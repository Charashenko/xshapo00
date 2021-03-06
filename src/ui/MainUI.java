package ui;

import controller.OnExitApp;
import controller.OnImportOrderFile;
import controller.OnShowAppAbout;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import model.Clock;
import model.GoodsType;
import model.Order;
import view.CartView;
import view.WarehouseView;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Main MainUI class
 *
 * @author Stefan Olenocin
 * @author Viktor Shapochkin
 */
public class MainUI extends Application {

    private static Clock clock = new Clock(300); // default value
    private static final Text informationText = new Text();
    private static WarehouseView warehouseView;
    private static Order order;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        VBox root = new VBox();
        setupLayout(root);

        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setTitle("Warehouse");
        primaryStage.setScene(scene);
        primaryStage.minWidthProperty().setValue(1000);
        primaryStage.minHeightProperty().setValue(600);
        primaryStage.setOnCloseRequest(windowEvent -> {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();
    }

    /**
     * Setup main UI layout
     *
     * @param root Root node
     */
    public static void setupLayout(VBox root) {
        String borderStyle = "-fx-padding: 5;" +
                "-fx-border-style: solid inside;" +
                "-fx-border-width: 2;" +
                "-fx-border-insets: 2;" +
                "-fx-border-color: black;";

        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        MenuItem importOrderItem = new MenuItem("Import order file");
        importOrderItem.setOnAction(actionEvent -> {
            new OnImportOrderFile().handle(actionEvent, warehouseView, order);
        });
        MenuItem showAppAboutItem = new MenuItem("About");
        showAppAboutItem.setOnAction(actionEvent -> new OnShowAppAbout().handle(actionEvent));
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(actionEvent -> new OnExitApp().handle(actionEvent));
        fileMenu.getItems().addAll(importOrderItem, showAppAboutItem, exitItem);
        menuBar.getMenus().addAll(fileMenu);

        HBox mainSection = new HBox();
        TabPane tabPane = new TabPane();
        Tab warehouseTab = new Tab("Warehouse");
        warehouseTab.setContent(setupWarehouseTab(informationText));
        warehouseTab.setClosable(false);
        warehouseTab.getContent().setStyle(borderStyle);
        Tab ordersTab = new Tab("Orders");
        ordersTab.setContent(setupOrdersTab());
        ordersTab.setClosable(false);
        ordersTab.getContent().setStyle(borderStyle);
        tabPane.getTabs().addAll(warehouseTab, ordersTab);

        VBox informationPanel = new VBox();

        HBox buttonBox = new HBox();
        buttonBox.setSpacing(7);
        buttonBox.setAlignment(Pos.CENTER);
        ProgressIndicator simulationIndicator = new ProgressIndicator(-1);
        simulationIndicator.setPrefSize(20, 20);
        simulationIndicator.setVisible(false);
        Button runButton = new Button("Run");
        Button stopButton = new Button("Stop");
        stopButton.setDisable(true);
        Button resetButton = new Button("Reset");
        Button configureButton = new Button("Configure");
        Button jumpButton = new Button("Jump");
        TextField jumpValue = new TextField("1");
        Text currentOrder = new Text("[Current order]\n" + order.getOrderItemsAsString());
        jumpValue.setPrefWidth(30);

        runButton.setOnAction(actionEvent -> {
            simulationIndicator.setVisible(true);
            runButton.setDisable(true);
            stopButton.setDisable(false);
            resetButton.setDisable(false);
            jumpButton.setDisable(true);
            clock.setRunning(true);
            systemUpdate();
        });

        stopButton.setOnAction(mouseEvent -> {
            simulationIndicator.setVisible(false);
            stopButton.setDisable(true);
            runButton.setDisable(false);
            jumpButton.setDisable(false);
            clock.setRunning(false);
        });

        resetButton.setOnAction(mouseEvent -> {
            simulationIndicator.setVisible(false);
            resetButton.setDisable(true);
            runButton.setDisable(false);
            stopButton.setDisable(true);
            clock.setRunning(false);
            warehouseTab.setContent(setupWarehouseTab(informationText));
            warehouseTab.getContent().setStyle(borderStyle);
            order = warehouseView.parseOrdersYaml().get(0);
            currentOrder.setText("[Current order]\n" + order.getOrderItemsAsString());
        });

        jumpButton.setOnAction(actionEvent -> {
            resetButton.setDisable(false);
            jumpNumberOfPoints(Integer.parseInt(jumpValue.getText()));
        });

        buttonBox.getChildren().addAll(runButton, stopButton, configureButton, resetButton, jumpButton, jumpValue, simulationIndicator);

        VBox configureBox = new VBox();
        HBox currentBox = new HBox();
        Label currentLabel = new Label("Current clock");
        Label currentClock = new Label(String.valueOf(clock.getClock()));
        currentBox.setSpacing(10);
        currentBox.setAlignment(Pos.CENTER);
        currentBox.getChildren().addAll(currentLabel, currentClock);

        HBox newBox = new HBox();
        Label newLabel = new Label("New clock");
        TextField newClock = new TextField();
        newClock.setMaxWidth(75);
        newBox.setSpacing(5);
        newBox.setAlignment(Pos.CENTER);
        newBox.getChildren().addAll(newLabel, newClock);
        configureBox.getChildren().addAll(currentBox, newBox);

        configureButton.setOnAction(mouseEvent -> {
            try {
                if (Integer.parseInt(newClock.getText()) >= 100) {
                    clock.setClock(Integer.parseInt(newClock.getText()));
                    currentClock.setText(newClock.getText());
                } else {
                    clock.setClock(100);
                    currentClock.setText("100");
                }
            } catch (NumberFormatException e) {
                mouseEvent.consume();
            }
        });

        informationText.setFont(new Font("", 18));
        informationText.setTextAlignment(TextAlignment.CENTER);

        currentOrder.setFont(new Font("", 18));
        informationPanel.getChildren().addAll(buttonBox, configureBox, currentOrder, informationText);
        informationPanel.setMinWidth(400);
        informationPanel.setPadding(new Insets(20, 2, 2, 2));
        informationPanel.setAlignment(Pos.TOP_CENTER);
        informationPanel.setSpacing(7);

        AtomicBoolean selectedWarehouse = new AtomicBoolean(true);
        ordersTab.setOnSelectionChanged(event -> {
            runButton.setVisible(!runButton.isVisible());
            stopButton.setVisible(!stopButton.isVisible());
            configureButton.setVisible(!configureButton.isVisible());
            resetButton.setVisible(!resetButton.isVisible());
            configureBox.setVisible(!configureBox.isVisible());
            jumpButton.setVisible(!jumpButton.isVisible());
            jumpValue.setVisible(!jumpValue.isVisible());
            currentOrder.setVisible(!currentOrder.isVisible());
            selectedWarehouse.set(!selectedWarehouse.get());
            currentOrder.setText("[Current order]\n" + order.getOrderItemsAsString());
            if (!selectedWarehouse.get()) {
                simulationIndicator.setVisible(false);
                resetButton.setDisable(true);
                runButton.setDisable(false);
                stopButton.setDisable(true);
                clock.setRunning(!clock.isRunning());
                warehouseTab.setContent(setupWarehouseTab(informationText));
                warehouseTab.getContent().setStyle(borderStyle);

            }
        });

        mainSection.getChildren().add(tabPane);
        mainSection.getChildren().add(informationPanel);

        root.getChildren().addAll(menuBar, mainSection);
    }

    /**
     * Setup warehouse tab in UI
     *
     * @param informationText Text information area
     * @return Main node of warehouse tab
     */
    public static Node setupWarehouseTab(Text informationText) {
        ZoomableScrollPane zoomablePane = new ZoomableScrollPane();
        zoomablePane.setPrefSize(1920, 1080);
        warehouseView = new WarehouseView(informationText);
        zoomablePane.addContent(warehouseView.getGuiWarehouse());
        return zoomablePane;
    }

    /**
     * Setup orders tab in UI
     *
     * @return Main node of orders tab
     */
    public static Node setupOrdersTab() {
        VBox ordersVBox = new VBox();
        ordersVBox.setPrefSize(1920, 1080);
        ordersVBox.setSpacing(5);
        HBox ordersControl = new HBox();
        ordersControl.setPadding(new Insets(20, 0, 10, 20));
        ordersControl.setSpacing(5);
        ChoiceBox<GoodsType> choiceBox = new ChoiceBox<>();
        choiceBox.getItems().addAll(Arrays.asList(GoodsType.values()));
        Button plus = new Button("+");
        Button minus = new Button("-");
        plus.setDisable(true);
        minus.setDisable(true);
        Label countLabel = new Label("1");
        countLabel.setFont(new Font(20));
        Slider slider = new Slider();
        slider.setMax(24);
        slider.setMin(1);
        slider.setMajorTickUnit(4);
        slider.setBlockIncrement(1);
        slider.setShowTickMarks(true);
        slider.setPrefWidth(300);
        slider.setDisable(true);

        slider.valueProperty().addListener(observable -> {
            countLabel.setText(String.valueOf((int) slider.getValue()));
        });

        plus.setOnAction(actionEvent -> {
            countLabel.setText(String.valueOf(Integer.parseInt(countLabel.getText()) + 1));
            slider.setValue(Integer.parseInt(countLabel.getText()));
        });

        minus.setOnAction(actionEvent -> {
            if (Integer.parseInt(countLabel.getText()) - 1 >= 1) {
                countLabel.setText(String.valueOf(Integer.parseInt(countLabel.getText()) - 1));
                slider.setValue(Integer.parseInt(countLabel.getText()));
            }
        });

        ordersControl.getChildren().addAll(choiceBox, plus, minus, slider, countLabel);
        HBox orderManipulationButtons = new HBox();
        orderManipulationButtons.setSpacing(10);
        orderManipulationButtons.setPadding(new Insets(0, 0, 10, 20));
        Button addButton = new Button("Add to order");
        Button removeButton = new Button("Remove from order");
        Button deleteButton = new Button("Delete current order");
        addButton.setDisable(true);
        removeButton.setDisable(true);

        choiceBox.setOnAction(actionEvent -> {
            plus.setDisable(false);
            minus.setDisable(false);
            addButton.setDisable(false);
            removeButton.setDisable(false);
            deleteButton.setDisable(false);
            slider.setDisable(false);
        });

        orderManipulationButtons.getChildren().addAll(addButton, removeButton, deleteButton);

        VBox currentOrderTextBox = new VBox();
        currentOrderTextBox.setPadding(new Insets(0, 0, 10, 20));
        Label currentOrderLabel = new Label("Current order:");
        currentOrderLabel.setFont(new Font(20));
        Text currentOrder = new Text();
        currentOrder.setFont(new Font(16));

        order = warehouseView.parseOrdersYaml().get(0);
        currentOrder.setText(order.getOrderItemsAsString());

        addButton.setOnAction(actionEvent -> {
            order.addGoodsToOrder(choiceBox.getValue(), Integer.parseInt(countLabel.getText()));
            currentOrder.setText(order.getOrderItemsAsString());
        });
        removeButton.setOnAction(actionEvent -> {
            order.removeGoodsFromOrder(choiceBox.getValue(), Integer.parseInt(countLabel.getText()));
            currentOrder.setText(order.getOrderItemsAsString());
        });

        deleteButton.setOnAction(actionEvent -> {
            order.clearOrder();
            currentOrder.setText(order.getOrderItemsAsString());
        });

        currentOrderTextBox.getChildren().addAll(currentOrderLabel, currentOrder);
        ordersVBox.getChildren().addAll(ordersControl, orderManipulationButtons, currentOrderTextBox);
        return ordersVBox;
    }

    /**
     * Updates system every specified time and calculates routes of carts
     */
    public static void systemUpdate() {
        new Thread(() -> { //cart movement thread
            try {
                order.divideCurrentOrder(warehouseView);
                do {
                    for (CartView cv : warehouseView.getCartViews()) {
                        cv.getCart().getPathfinder().computePath();
                        cv.getCart().nextStep(clock.getClock() - clock.getClock() / 10, warehouseView);
                    }
                    Thread.sleep(clock.getClock());
                } while (clock.isRunning());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Jumps forward in time based on specified number
     *
     * @param jumpValue Number of times to jump
     */
    public static void jumpNumberOfPoints(int jumpValue) {
        new Thread(() -> {
            order.divideCurrentOrder(warehouseView);
            for (CartView cv : warehouseView.getCartViews()) {
                cv.getCart().getPathfinder().computePath();
            }
            for (int i = 0; i < jumpValue; i++) {
                for (CartView cv : warehouseView.getCartViews()) {
                    cv.getCart().nextStepWithoutAnimation(warehouseView);
                }
            }
            warehouseView.drawGui();
        }).start();
    }

    /**
     * Gets warehouse view
     *
     * @return WarehouseView object
     */
    public static WarehouseView getWarehouseView() {
        return warehouseView;
    }
}
