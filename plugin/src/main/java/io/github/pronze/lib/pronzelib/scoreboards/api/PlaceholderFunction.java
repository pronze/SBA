// 
// Decompiled by Procyon v0.5.36
// 

package io.github.pronze.lib.pronzelib.scoreboards.api;

import io.github.pronze.lib.pronzelib.scoreboards.data.PlaceholderData;

@FunctionalInterface
public interface PlaceholderFunction
{
    String handleReplace(final PlaceholderData p0);
}
