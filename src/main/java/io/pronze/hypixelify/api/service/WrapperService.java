package io.pronze.hypixelify.api.service;

public interface WrapperService<T> {

    void register(T param);

    void unregister(T param);

}
