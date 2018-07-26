package com.renny.plugin;

/**
 * Created by rjn on 2018/7/23.
 */
public class InitItem {

    private boolean background = false;

    private boolean inChildProcess = true;

    private boolean onlyInDebug = false;

    public boolean isOnlyInDebug() {
        return onlyInDebug;
    }

    public void setOnlyInDebug(boolean onlyInDebug) {
        this.onlyInDebug = onlyInDebug;
    }

    private int priority = 0;

    private long delay = 0;

    private String path = "";

    public boolean isBackground() {
        return background;
    }

    public void setBackground(boolean background) {
        this.background = background;
    }

    public boolean isInChildProcess() {
        return inChildProcess;
    }

    public void setInChildProcess(boolean inChildProcess) {
        this.inChildProcess = inChildProcess;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
