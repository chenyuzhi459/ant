package io.sugo.common.guice;

import com.google.common.collect.Lists;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.metamx.common.lifecycle.Lifecycle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.List;

/**
 * A scope that adds objects to the Lifecycle.  This is by definition also a lazy singleton scope.
 */
public class LifecycleScope implements Scope
{
	private static final Logger log = LogManager.getLogger(LifecycleScope.class);
	private final Lifecycle.Stage stage;

	private Lifecycle lifecycle;
	private List<Object> instances = Lists.newLinkedList();

	public LifecycleScope(Lifecycle.Stage stage)
	{
		this.stage = stage;
	}

	public void setLifecycle(Lifecycle lifecycle)
	{
		this.lifecycle = lifecycle;
		synchronized (instances) {
			for (Object instance : instances) {
				lifecycle.addManagedInstance(instance);
			}
		}
	}

	@Override
	public <T> Provider<T> scope(final Key<T> key, final Provider<T> unscoped)
	{
		return new Provider<T>()
		{
			private volatile T value = null;

			@Override
			public synchronized T get()
			{
				if (value == null) {
					final T retVal = unscoped.get();

					synchronized (instances) {
						if (lifecycle == null) {
							instances.add(retVal);
						}
						else {
							try {
								lifecycle.addMaybeStartManagedInstance(retVal, stage);
							}
							catch (Exception e) {
								log.warn(String.format("Caught exception when trying to create a[%s]", key),e);
								return null;
							}
						}
					}

					value = retVal;
				}

				return value;
			}
		};
	}
}
