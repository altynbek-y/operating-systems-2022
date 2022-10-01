package com.example.lab01_ui_javafx;

import javafx.scene.control.TextField;

public class ConsumerThread  implements Runnable {
    private RingBuffer ringBuffer;
    private CharTypes charTypes;
    private TextField textField;
    private TextField producerTextField;

    public ConsumerThread (RingBuffer ringBuffer, CharTypes charTypes, TextField textField) {
        this.ringBuffer = ringBuffer;
        this.charTypes = charTypes;
        this.textField = textField;
        new Thread(this).start();
    }
    @Override
    public void run() {
        try {
            while(true) {
                ringBuffer.readData(this.charTypes, this.textField);
            }
        } catch(Exception e) {
            Thread.currentThread().interrupt();
        }
    }
}