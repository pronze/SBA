package io.github.pronze.lib.pronzelib.scoreboards.scoreboardr.plugin.utility;
//https://github.com/RienBijl/Scoreboard-revision/blob/master/src/main/java/rien/bijl/Scoreboard/r/Plugin/Utility/LineLimits.java
public class LineLimits {

    private static int lineLimit = 0;

    public static int getLineLimit()
    {
        if (lineLimit != 0) {
            return lineLimit;
        }

        int minor = ServerVersion.minor();

        if (minor >= 13) {
            lineLimit = 64;
        } else {
            lineLimit = 16;
        }

        return lineLimit;
    }

}