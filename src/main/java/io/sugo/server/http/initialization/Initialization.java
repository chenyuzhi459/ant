package io.sugo.server.http.initialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import com.metamx.common.ISE;
import io.sugo.common.guice.annotations.Json;
import io.sugo.common.module.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by chenyuzhi on 18-9-18.
 */
public class Initialization {
	public static Injector makeInjectorWithModules(final Injector baseInjector, Iterable<? extends Module> modules) {

		List<Module> defaultModules = new ArrayList<>();
		for(Module m : modules){
			defaultModules.add(m);
		}
		defaultModules.add(new LifecycleModule());
		defaultModules.add(new ServerModule());
		defaultModules.add(new JettyServerModule());
		return Guice.createInjector(defaultModules);
	}


}
