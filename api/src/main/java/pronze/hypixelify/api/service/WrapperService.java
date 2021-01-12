package pronze.hypixelify.api.service;

public interface WrapperService<T> {

    /**
     *
     * @param param the object to register
     */
    void register(T param);

    /**
     *
     * @param param the object to unregister
     */
    void unregister(T param);

}
