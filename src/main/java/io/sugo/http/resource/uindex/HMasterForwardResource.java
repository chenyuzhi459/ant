package io.sugo.http.resource.uindex;

import io.sugo.http.resource.ForwardResource;

/**
 * Created by chenyuzhi on 18-5-23.
 */
public class HMasterForwardResource extends ForwardResource {
	public HMasterForwardResource() {
		ip = getIp("uindex.properties","hmaster");
	}
}
