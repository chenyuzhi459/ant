package io.sugo.common.guice;

import com.google.inject.Key;

/**
 * Created by chenyuzhi on 18-9-18.
 */
public class KeyHolder<T>
{
	private final Key<? extends T> key;

	public KeyHolder(
			Key<? extends T> key
	)
	{
		this.key = key;
	}

	public Key<? extends T> getKey()
	{
		return key;
	}
}