package io.github.pronze.sba.game.task;

import java.text.SimpleDateFormat;

public class GeneratorTask extends AbstractGameTaskImpl {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");

    private int elapsedTime;

    @Override
    protected void run() {

    }

    public String getTimeLeftForNextEvent() {
        // TODO:
        return null;
    }

    public String getNextTierName() {
        // TODO:
        return null;
    }
}
