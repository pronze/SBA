package pronze.sba.service;

import java.util.Optional;

public interface WrapperService<K, V> {

    /**
     *
     * @param param the object to register
     */
    void register(K param);

    /**
     *
     * @param param the object to unregister
     */
    void unregister(K param);

    /**
     *
     * @param param the object to query
     * @return
     */
    Optional<V> get(K param);

}
