package io.github.pronze.sba.service;

import java.util.Optional;

public interface WrapperService<K, V> {

    /**
     * Wraps the object and registers it into a map.
     * @param param the object to register
     */
    void register(K param);

    /**
     * Unregister the object from the map.
     * @param param the object to unregister
     */
    void unregister(K param);

    /**
     * Gets an optional containing the object that may have been registered prior to this method call, empty otherwise.
     * @param param the object to query
     * @return an optional that may or may not be empty depending on the query
     */
    Optional<V> get(K param);

}
