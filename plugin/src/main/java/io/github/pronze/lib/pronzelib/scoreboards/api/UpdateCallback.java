// 
// Decompiled by Procyon v0.5.36
// 

package io.github.pronze.lib.pronzelib.scoreboards.api;

import io.github.pronze.lib.pronzelib.scoreboards.Scoreboard;

@FunctionalInterface
public interface UpdateCallback
{
    boolean onCallback(final Scoreboard p0);
}
