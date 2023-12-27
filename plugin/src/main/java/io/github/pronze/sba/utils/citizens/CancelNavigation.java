package io.github.pronze.sba.utils.citizens;

import io.github.pronze.sba.utils.citizens.FakeDeathTrait.AiGoal;

public class CancelNavigation implements FakeDeathTrait.AiGoal {

    /**
     *
     */
    private final FakeDeathTrait fakeDeathTrait;

    /**
     * @param fakeDeathTrait
     */
    CancelNavigation(FakeDeathTrait fakeDeathTrait) {
        this.fakeDeathTrait = fakeDeathTrait;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void doGoal() {
        // Basically do nothing if it can't find another goal
        this.fakeDeathTrait.getNPC().getNavigator().cancelNavigation();
    }

}