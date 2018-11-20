package io.sugo.dgraph.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.dgraph.DgraphClient;
import io.sugo.common.guice.annotations.Json;

import java.io.IOException;
import java.util.Map;

import static io.dgraph.DgraphProto.*;
/**
 * Created by chenyuzhi on 18-11-16.
 */
public class ClientProxy {
	private final DgraphClient client;
	private final ObjectMapper jsonMapper;

	@Inject
	public ClientProxy(@Json ObjectMapper jsonMapper) {
		this.client = ClientFatory.getNewClient();
		this.jsonMapper = jsonMapper;
	}

	public Map<String, Object> query(String query) throws IOException {
		Response res = client.newReadOnlyTransaction().query(query);
		return jsonMapper.readValue(res.getJson().toStringUtf8(), Map.class);
	}
}
