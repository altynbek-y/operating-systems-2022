package com.example.lab01_ui_javafx;

import javafx.scene.control.TextField;

import java.util.concurrent.Semaphore;

public class RingBuffer {
    private Semaphore busy;
    private Semaphore full;
    private Semaphore empty;
    private char[] data;
    private int head, tail;
    public RingBuffer(int size) {
        data = new char[size];
        head = tail = 0;
        busy = new Semaphore(1);
        full = new Semaphore(0);
        empty = new Semaphore(size);
    }
    public void writeData(char inChar) {
        try {
            this.empty.acquire();
            this.busy.acquire();
            //Put item
            data[head] = inChar;
            head = (head + 1) % data.length;
            this.busy.release();
            this.full.release();
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }
    public void readData(CharTypes charTypes, TextField textField) {
        try {
            this.full.acquire();
            this.busy.acquire();
            // Take item
            char outChar = data[tail];
            if(
                    (Character.isDigit(outChar) && charTypes == CharTypes.digit) ||
                    (Character.isLetter(outChar) && charTypes == CharTypes.letter) ||
                    ((!Character.isLetter(outChar) && !Character.isDigit(outChar) && (int)outChar>0)
                            && charTypes == CharTypes.other)
            ) {
                textField.setText(String.valueOf(outChar));
                tail = (tail + 1) % data.length;
                this.empty.release();
            }
            this.full.release();
            this.busy.release();
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }
}