package io.sugo.common.modules;

import com.google.inject.Binder;
import com.google.inject.Module;
import io.sugo.services.pathanalysis.dto.PathAnalysisDto;

/**
 * Created by chenyuzhi on 18-12-27.
 */
public class PathAnalysisModule implements Module {
	@Override
	public void configure(Binder binder) {
		binder.requestStaticInjection(PathAnalysisDto.class);
	}
}
