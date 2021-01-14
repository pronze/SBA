package pronze.hypixelify.api.wrapper;

public interface PlayerWrapper {

    String getName();

    boolean canShout();

    void shout();

    int getShoutTimeOut();

    int getKills();

    int getBedDestroys();

    int getDeaths();

    double getKD();

    int getXP();

    int getLevel();

    int getWins();

    String getProgress();

    int getIntegerProgress();

    String getCompletedBoxes();

    void sendMessage(String message);
}
