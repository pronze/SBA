package io.github.pronze.sba.lang;

import org.jetbrains.annotations.NotNull;

/**
 * Represents an implementation for the LanguageService.
 */
public interface ILanguageService {
    /**
     *
     * @param arguments the arguments to query the language file
     * @return an message instance
     */
    @NotNull
    Message get(String... arguments);
}
