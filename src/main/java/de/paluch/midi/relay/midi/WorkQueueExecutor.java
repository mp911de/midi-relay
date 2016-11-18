package de.paluch.midi.relay.midi;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.sound.midi.MidiMessage;

import lombok.extern.slf4j.Slf4j;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@Slf4j
public class WorkQueueExecutor extends Thread {

    private volatile boolean running = true;
    private Queue<DelayedMidiMessage> queue = new ConcurrentLinkedQueue<DelayedMidiMessage>();
    private long delay = 0;
    private Callback callback;

    public WorkQueueExecutor() {
        super("WorkQueueExecutor");
    }

    public void submit(MidiMessage midiMessage) {
        if (callback == null) {
            throw new IllegalStateException("Callback not initialized");
        }
        queue.add(new DelayedMidiMessage(System.currentTimeMillis() + delay, midiMessage));
    }

    @Override
    public void run() {

        log.info("Starting Queue Processing");

        while (running) {
            try {
                sleep(10);
            } catch (InterruptedException e) {
                running = false;
            }

            while (processingNeeded()) {
                try {
                    callback.call(queue.poll().midiMessage);
                } catch (Exception e) {
                    log.warn(e.getMessage(), e);
                }
            }
        }

        log.info("Stopped Queue Processing");
    }

    private boolean processingNeeded() {
        if (queue.isEmpty()) {
            return false;
        }

        if (queue.peek().timestamp <= System.currentTimeMillis()) {
            return true;
        }

        return false;
    }

    public void shutdown() {

        log.info("Requesting shutdown");
        running = false;
    }

    private static class DelayedMidiMessage {
        long timestamp;
        MidiMessage midiMessage;

        public DelayedMidiMessage(long timestamp, MidiMessage midiMessage) {
            this.timestamp = timestamp;
            this.midiMessage = midiMessage;
        }
    }

    public interface Callback {
        void call(MidiMessage midiMessage);
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }
}
