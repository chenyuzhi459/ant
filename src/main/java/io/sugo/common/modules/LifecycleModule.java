package io.sugo.common.modules;

import com.google.inject.*;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.metamx.common.lifecycle.Lifecycle;
import io.sugo.common.guice.KeyHolder;
import io.sugo.common.guice.LifecycleScope;
import io.sugo.common.guice.annotations.LazySingleton;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * A Module to add lifecycle management to the injector.
 */
public class LifecycleModule implements Module
{
	private final LifecycleScope scope = new LifecycleScope(Lifecycle.Stage.NORMAL);
	private final LifecycleScope lastScope = new LifecycleScope(Lifecycle.Stage.LAST);
	/**
	 * Registers a class to instantiate eagerly.  Classes mentioned here will be pulled out of
	 * the injector with an injector.getInstance() call when the lifecycle is created.
	 *
	 * Eagerly loaded classes will *not* be automatically added to the Lifecycle unless they are bound to the proper
	 * scope.  That is, they are generally eagerly loaded because the loading operation will produce some beneficial
	 * side-effect even if nothing actually directly depends on the instance.
	 *
	 * This mechanism exists to allow the {@link com.metamx.common.lifecycle.Lifecycle} to be the primary entry point from the injector, not to
	 * auto-register things with the {@link com.metamx.common.lifecycle.Lifecycle}.  It is also possible to just bind things eagerly with Guice,
	 * it is not clear which is actually the best approach.  This is more explicit, but eager bindings inside of modules
	 * is less error-prone.
	 *
	 * @param clazz, the class to instantiate
	 * @return this, for chaining.
	 */
	public static void register(Binder binder, Class<?> clazz)
	{
		registerKey(binder, Key.get(clazz));
	}

	/**
	 * Registers a class/annotation combination to instantiate eagerly.  Classes mentioned here will be pulled out of
	 * the injector with an injector.getInstance() call when the lifecycle is created.
	 *
	 * Eagerly loaded classes will *not* be automatically added to the Lifecycle unless they are bound to the proper
	 * scope.  That is, they are generally eagerly loaded because the loading operation will produce some beneficial
	 * side-effect even if nothing actually directly depends on the instance.
	 *
	 * This mechanism exists to allow the {@link com.metamx.common.lifecycle.Lifecycle} to be the primary entry point from the injector, not to
	 * auto-register things with the {@link com.metamx.common.lifecycle.Lifecycle}.  It is also possible to just bind things eagerly with Guice,
	 * it is not clear which is actually the best approach.  This is more explicit, but eager bindings inside of modules
	 * is less error-prone.
	 *
	 * @param clazz, the class to instantiate
	 * @param annotation The annotation instance to register with Guice, usually a Named annotation
	 * @return this, for chaining.
	 */
	public static void register(Binder binder, Class<?> clazz, Annotation annotation)
	{
		registerKey(binder, Key.get(clazz, annotation));
	}

	/**
	 * Registers a class/annotation combination to instantiate eagerly.  Classes mentioned here will be pulled out of
	 * the injector with an injector.getInstance() call when the lifecycle is created.
	 *
	 * Eagerly loaded classes will *not* be automatically added to the Lifecycle unless they are bound to the proper
	 * scope.  That is, they are generally eagerly loaded because the loading operation will produce some beneficial
	 * side-effect even if nothing actually directly depends on the instance.
	 *
	 * This mechanism exists to allow the {@link com.metamx.common.lifecycle.Lifecycle} to be the primary entry point from the injector, not to
	 * auto-register things with the {@link com.metamx.common.lifecycle.Lifecycle}.  It is also possible to just bind things eagerly with Guice,
	 * it is not clear which is actually the best approach.  This is more explicit, but eager bindings inside of modules
	 * is less error-prone.
	 *
	 * @param clazz, the class to instantiate
	 * @param annotation The annotation class to register with Guice
	 * @return this, for chaining
	 */
	public static void register(Binder binder, Class<?> clazz, Class<? extends Annotation> annotation)
	{
		registerKey(binder, Key.get(clazz, annotation));
	}

	/**
	 * Registers a key to instantiate eagerly.  {@link com.google.inject.Key}s mentioned here will be pulled out of
	 * the injector with an injector.getInstance() call when the lifecycle is created.
	 *
	 * Eagerly loaded classes will *not* be automatically added to the Lifecycle unless they are bound to the proper
	 * scope.  That is, they are generally eagerly loaded because the loading operation will produce some beneficial
	 * side-effect even if nothing actually directly depends on the instance.
	 *
	 * This mechanism exists to allow the {@link com.metamx.common.lifecycle.Lifecycle} to be the primary entry point
	 * from the injector, not to auto-register things with the {@link com.metamx.common.lifecycle.Lifecycle}.  It is
	 * also possible to just bind things eagerly with Guice, it is not clear which is actually the best approach.
	 * This is more explicit, but eager bindings inside of modules is less error-prone.
	 *
	 * @param key The key to use in finding the DruidNode instance
	 */
	public static void registerKey(Binder binder, Key<?> key)
	{
		getEagerBinder(binder).addBinding().toInstance(new KeyHolder<Object>(key));
	}

	private static Multibinder<KeyHolder> getEagerBinder(Binder binder)
	{
		return Multibinder.newSetBinder(binder, KeyHolder.class, Names.named("lifecycle"));
	}

	@Override
	public void configure(Binder binder)
	{
		getEagerBinder(binder); // Load up the eager binder so that it will inject the empty set at a minimum.
	}

	@Provides
	@LazySingleton
	public Lifecycle getLifecycle(final Injector injector)
	{
		final Key<Set<KeyHolder>> keyHolderKey = Key.get(new TypeLiteral<Set<KeyHolder>>(){}, Names.named("lifecycle"));
		final Set<KeyHolder> eagerClasses = injector.getInstance(keyHolderKey);

		Lifecycle lifecycle = new Lifecycle(){
			@Override
			public void start() throws Exception
			{
				for (KeyHolder<?> holder : eagerClasses) {
					injector.getInstance(holder.getKey()); // Pull the key so as to "eagerly" load up the class.
				}
				super.start();
			}
		};
		scope.setLifecycle(lifecycle);
		lastScope.setLifecycle(lifecycle);

		return lifecycle;
	}
}