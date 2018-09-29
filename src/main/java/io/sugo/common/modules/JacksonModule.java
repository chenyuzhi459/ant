package io.sugo.common.modules;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.google.inject.*;
import io.sugo.common.guice.GuiceAnnotationIntrospector;
import io.sugo.common.guice.GuiceInjectableValues;
import io.sugo.common.guice.annotations.LazySingleton;
import io.sugo.common.guice.annotations.Json;
import io.sugo.common.utils.DefaultObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 */
public class JacksonModule implements Module {
    private static final Logger log = LogManager.getLogger(JacksonModule.class);

    @Override
    public void configure(Binder binder)
    {
        binder.bind(ObjectMapper.class).to(Key.get(ObjectMapper.class, Json.class));
    }

    @Provides
    @LazySingleton
    @Json
    public ObjectMapper jsonMapper(final Injector injector)
    {
        ObjectMapper objectMapper = new DefaultObjectMapper();
        setupJackson(injector, objectMapper);
        return objectMapper;
    }

    //jsonMapper序列化和反序列时通过guice的Injector进行注入. 从而使注解与guice统一起来
    private void setupJackson(Injector injector, final ObjectMapper mapper) {
        log.info("Init objectMapper.");
        final GuiceAnnotationIntrospector guiceIntrospector = new GuiceAnnotationIntrospector();

        mapper.setInjectableValues(new GuiceInjectableValues(injector));
        mapper.setAnnotationIntrospectors(
                new AnnotationIntrospectorPair(
                        guiceIntrospector, mapper.getSerializationConfig().getAnnotationIntrospector()
                ),
                new AnnotationIntrospectorPair(
                        guiceIntrospector, mapper.getDeserializationConfig().getAnnotationIntrospector()
                )
        );
    }
}
