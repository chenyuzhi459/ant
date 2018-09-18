package io.sugo.common.module;

import com.fasterxml.jackson.databind.Module;

import java.util.List;

/**
 * Created by chenyuzhi on 18-9-18.
 */
public interface AntModule extends com.google.inject.Module
{
	public List<? extends Module> getJacksonModules();
}
