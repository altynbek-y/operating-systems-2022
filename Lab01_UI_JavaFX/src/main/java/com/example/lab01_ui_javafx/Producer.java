package com.example.lab01_ui_javafx;

import javafx.scene.control.TextField;

class Producer implements Runnable {

    TextField producerTextField;
    RingBuffer ringBuffer;

    public Producer(RingBuffer b, TextField d) {
        ringBuffer = b;
        producerTextField = d;
        new Thread(this).start();
    }

    public void run() {
        try {
            while (true) {
                producerTextField.setOnKeyTyped(ke -> ringBuffer.writeData(ke.getCharacter().charAt(0)));
            }
        } catch (NullPointerException e) {
            System.out.println("NullPointerException thrown!");
        }
    }
}
