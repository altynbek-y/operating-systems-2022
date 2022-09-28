import java.util.Scanner;
import java.util.concurrent.Semaphore;

class RingBuffer {
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
    public void readData(int thread_id) {
        try {
            this.full.acquire();
            this.busy.acquire();
            // Take item
            char outChar = data[tail];
            if(
                    (Character.isDigit(outChar) && thread_id == ConsumerThread.DIGIT_CONSUMER_THREAD) ||
                    (Character.isLetter(outChar) && thread_id == ConsumerThread.LETTER_CONSUMER_THREAD) ||
                    ((!Character.isLetter(outChar) && !Character.isDigit(outChar)) && thread_id == ConsumerThread.OTHER_CONSUMER_THREAD)
            ) {
                System.out.println("Thread " + thread_id + ": has read " + outChar);
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
class ProducerThread extends Thread {
    private RingBuffer ringBuffer;
    public ProducerThread(RingBuffer ringBuffer) {
        this.ringBuffer = ringBuffer;
        new Thread(this).start();
    }
    @Override
    public void run() {
        try {
            while(true) {
                // Random ASCII between 21 and 7A
                ringBuffer.writeData((char)(Math.random() * 101 + 21));
            }
        } catch(Exception e) {
            System.out.println("Exception is caught: " + e);
        }
    }
}
class ConsumerThread extends Thread {
    public static final int LETTER_CONSUMER_THREAD = 0;
    public static final int DIGIT_CONSUMER_THREAD = 1;
    public static final int OTHER_CONSUMER_THREAD = 2;
    private RingBuffer ringBuffer;
    private int threadId;
    public ConsumerThread(RingBuffer ringBuffer, int threadId) {
        this.ringBuffer = ringBuffer;
        this.threadId = threadId;
        new Thread(this).start();
    }
    @Override
    public void run() {
        try {
            while(true) {
                ringBuffer.readData(this.threadId);
            }
        } catch(Exception e) {
            System.out.println("Exception is caught: " + e);
        }
    }
}
public class Main {
    public static void main(String[] args) {
        RingBuffer ringBuffer = new RingBuffer(8);
        new ProducerThread(ringBuffer);
        new ConsumerThread(ringBuffer, ConsumerThread.LETTER_CONSUMER_THREAD);
        new ConsumerThread(ringBuffer, ConsumerThread.DIGIT_CONSUMER_THREAD);
        new ConsumerThread(ringBuffer, ConsumerThread.OTHER_CONSUMER_THREAD);
    }
}