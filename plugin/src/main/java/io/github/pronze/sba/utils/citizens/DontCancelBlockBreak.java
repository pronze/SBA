package io.github.pronze.sba.utils.citizens;

import io.github.pronze.sba.utils.citizens.FakeDeathTrait.AiGoal;

public class DontCancelBlockBreak implements FakeDeathTrait.AiGoal {
    /**
     *
     */
    private final FakeDeathTrait fakeDeathTrait;

    /**
     * @param fakeDeathTrait
     */
    DontCancelBlockBreak(FakeDeathTrait fakeDeathTrait) {
        this.fakeDeathTrait = fakeDeathTrait;
    }

    @Override
    public boolean isAvailable() {

        return this.fakeDeathTrait.blockPlace() != null && this.fakeDeathTrait.blockPlace().isBreaking();
    }

    @Override
    public void doGoal() {

    }
}