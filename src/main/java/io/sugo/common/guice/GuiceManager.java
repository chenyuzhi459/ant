package io.sugo.common.guice;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import java.util.ArrayList;
import java.util.List;


public class GuiceManager {
	private List<Module> modules;
	private Injector injector;

	public GuiceManager(List<Module> modules) {
		this.modules = ImmutableList.copyOf(modules);
		this.injector = Guice.createInjector(modules);
	}

	public static class Builder{
		private List<Module> _modules = new ArrayList<>();

		public Builder(){
		}

		public Builder addModule(Module m){
			_modules.add(m);
			return this;
		}

		public GuiceManager build(){
			return new GuiceManager(_modules);
		}
	}

	public <T> T getInstance(Class<T> tClass){
		return injector.getInstance(tClass);
	}

	public List<Module> getModules(){
		return modules;
	}

	public Injector getInjector(){
		return injector;
	}
}
