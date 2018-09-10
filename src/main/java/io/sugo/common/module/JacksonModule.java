package io.sugo.common.module;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.*;
import io.sugo.server.guice.LazySingleton;
import io.sugo.server.guice.annotations.Json;
import io.sugo.common.utils.DefaultObjectMapper;


/**
 */
public class JacksonModule implements Module
{
    @Override
    public void configure(Binder binder)
    {
        binder.bind(ObjectMapper.class).to(Key.get(ObjectMapper.class, Json.class));
    }

    @Provides
    @LazySingleton
    @Json
    public ObjectMapper jsonMapper()
    {
        return new DefaultObjectMapper();
    }

}
