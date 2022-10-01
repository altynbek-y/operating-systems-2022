package com.example.lab01_ui_javafx;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.WindowEvent;

public class Main extends Application {  // Главный класс приложения JavaFX должен наследоваться
    // от класса javafx.application.Application

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception { // Главный класс имеет один абстрактный метод, который нужно реализовать
        Label label1 = new Label("Producer");
        TextField producerTextField = new TextField();
        HBox producer = new HBox(50, label1, producerTextField);
        producer.setAlignment(Pos.CENTER);

        Label label2 = new Label("DigitConsumer");
        TextField digitTextField = new TextField();
        HBox digitConsumer = new HBox(20, label2, digitTextField);
        digitConsumer.setAlignment(Pos.CENTER);

        Label label3 = new Label("LetterConsumer");
        TextField letterTextField = new TextField();
        HBox letterConsumer = new HBox(20, label3, letterTextField);
        letterConsumer.setAlignment(Pos.CENTER);

        Label label4 = new Label("OtherConsumer");
        TextField otherTextField = new TextField();
        HBox otherConsumer = new HBox(20, label4, otherTextField);
        otherConsumer.setAlignment(Pos.CENTER);

        //instantiate (create) buffer shared by Producer & Consumer
        RingBuffer ringBuffer = new RingBuffer(1000000);

        //producerTextField.setOnKeyTyped(ke -> ringBuffer.writeData(ke.getCharacter().charAt(0)));

        VBox vbox = new VBox(20, producer, digitConsumer, letterConsumer, otherConsumer);

        Scene scene = new Scene(vbox, 500, 200);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Decision of Producer-Consumers problem with Semaphore");
        primaryStage.show();

        new Thread(new Producer(ringBuffer, producerTextField));
        new Thread(new ConsumerThread(ringBuffer, CharTypes.digit, digitTextField));
        new Thread(new ConsumerThread(ringBuffer, CharTypes.letter, letterTextField));
        new Thread(new ConsumerThread(ringBuffer, CharTypes.other, otherTextField));

        primaryStage.setOnCloseRequest(windowEvent -> System.exit(0));
    }
}
