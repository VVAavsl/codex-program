package com.example.myapplication;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

/** 计时器：首次点击后开始，每秒回调一次，可停止/重置/恢复。 */
public class GameTimer {

    public interface TickListener {
        void onTick(int elapsedSeconds);
    }

    private final Handler handler = new Handler(Looper.getMainLooper());
    private TickListener listener;
    private boolean running = false;
    private long elapsedMs = 0L;
    private long baseMs = 0L;

    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            if (running) {
                elapsedMs = SystemClock.elapsedRealtime() - baseMs;
                if (listener != null) {
                    listener.onTick((int) (elapsedMs / 1000L));
                }
                long next = 1000L - (elapsedMs % 1000L);
                handler.postDelayed(this, next);
            }
        }
    };

    public void setListener(TickListener listener) {
        this.listener = listener;
    }

    public void start() {
        if (running) {
            return;
        }
        running = true;
        baseMs = SystemClock.elapsedRealtime() - elapsedMs;
        handler.removeCallbacks(ticker);
        handler.post(ticker);
    }

    public void stop() {
        if (!running) {
            return;
        }
        running = false;
        elapsedMs = SystemClock.elapsedRealtime() - baseMs;
        handler.removeCallbacks(ticker);
    }

    public void reset() {
        running = false;
        elapsedMs = 0L;
        handler.removeCallbacks(ticker);
    }

    public void setElapsedSeconds(int seconds) {
        if (seconds < 0) {
            seconds = 0;
        }
        elapsedMs = seconds * 1000L;
        if (running) {
            baseMs = SystemClock.elapsedRealtime() - elapsedMs;
        }
    }

    public int getElapsedSeconds() {
        long ms = running ? SystemClock.elapsedRealtime() - baseMs : elapsedMs;
        return (int) (ms / 1000L);
    }

    public boolean isRunning() {
        return running;
    }
}
