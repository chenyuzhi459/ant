package io.sugo.dgraph.client;

import io.dgraph.DgraphClient;
import io.dgraph.DgraphGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import static io.dgraph.DgraphGrpc.*;
/**
 * Created by chenyuzhi on 18-11-16.
 */
public class ClientFatory {
	private static final DgraphStub STUB = getNewConnection();

	private static DgraphStub getNewConnection(){
		ManagedChannel channel =
				ManagedChannelBuilder.forAddress("192.168.0.212", 9080).usePlaintext().build();
		return DgraphGrpc.newStub(channel);
	}

	public static DgraphClient getNewClient(){
		return new DgraphClient(STUB);
	}

}
