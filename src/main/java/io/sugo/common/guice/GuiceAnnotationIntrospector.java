package io.sugo.common.guice;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;
import com.google.inject.BindingAnnotation;
import com.google.inject.Key;
import com.metamx.common.IAE;

import java.lang.annotation.Annotation;

/**
 * Created by chenyuzhi on 18-9-14.
 */
public class GuiceAnnotationIntrospector extends NopAnnotationIntrospector
{
	@Override
	public Object findInjectableValueId(AnnotatedMember m)
	{
		if (m.getAnnotation(JacksonInject.class) == null) {
			return null;
		}

		Annotation guiceAnnotation = null;
		for (Annotation annotation : m.annotations()) {
			if (annotation.annotationType().isAnnotationPresent(BindingAnnotation.class)) {
				guiceAnnotation = annotation;
				break;
			}
		}

		if (guiceAnnotation == null) {
			if (m instanceof AnnotatedMethod) {
				throw new IAE("Annotated methods don't work very well yet...");
			}
			return Key.get(m.getGenericType());
		}
		return Key.get(m.getGenericType(), guiceAnnotation);
	}
}