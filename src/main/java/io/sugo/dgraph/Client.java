package io.sugo.dgraph;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import io.dgraph.DgraphClient;
import io.dgraph.DgraphGrpc;
import io.dgraph.Transaction;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.io.IOException;
import java.util.*;

import static io.dgraph.DgraphGrpc.DgraphStub;
import static io.dgraph.DgraphProto.*;

/**
 * Created by chenyuzhi on 18-11-8.
 */
public class Client {
	private static ObjectMapper objectMapper = new ObjectMapper();
	public static DgraphClient createClient(){
		ManagedChannel channel =
				ManagedChannelBuilder.forAddress("192.168.0.212", 9080).usePlaintext().build();
		DgraphStub stub = DgraphGrpc.newStub(channel);
		return new DgraphClient(stub);
	}

	public static void addSchema(DgraphClient dgraphClient){
		String schema = "    " +
				"	 product_id: string @index(exact) .\n" +
				"    product_name: string @index(term) .\n" +
				"    product_price: float .\n" +
				"    product_category: string @index(exact) .\n" +
				"    product_sub_category: string @index(exact) .\n" +
				"    activity_title: string @index(term) .\n" +
				"    activity_date: datetime .\n" +
				"    activity_type: string @index(exact) .\n" +
				"    member_gender: string @index(exact) .\n" +
				"    member_id: string @index(exact) .\n" +
				"    member_name: string @index(exact) .\n" +
				"    member_level: int .\n" +
				"    member_next_level_of: uid @reverse .\n" +
				"    member_phone: string @index(exact) .\n" +
				"    member_province: string @index(exact) .\n" +
				"    member_city: string @index(exact) .";
		Operation op = Operation.newBuilder().setSchema(schema).build();
		dgraphClient.alter(op);
	}



	private static void countNext(List<Map<String, Object>> nextLevel,final Set<String> memberIds){
		nextLevel.forEach((o) ->{
			String id = (String)o.get("member_id");
			memberIds.add(id);
			if(o.get("next_level") != null){
				countNext((List<Map<String, Object>>)o.get("next_level"), memberIds);
			}
		});
	}

	public static void main(String[] args) throws IOException {
		DgraphClient dgraphClient = createClient();
// 		createData(dgraphClient);
		// Query
		String query =
				"{\n" +
						"\tall(func: allofterms(member_id, \"4114396\")) @recurse(loop: false) {  #可以在recurse指定深度depth\n" +
						"        member_id\n" +
						"        member_name\n" +
						"        member_gender\n" +
						"        member_level\n" +
						"        next_level:~member_next_level_of \n" +
						"        member_phone\n" +
						"        member_province\n" +
						"        member_city\n" +
						"\t}\n" +
						"   \n" +
						"}";

		Map<String, String> vars = Collections.singletonMap("$a", "Alice");
		Response res = dgraphClient.newReadOnlyTransaction().queryWithVars(query, vars);

		// Deserialize
		ByteString resJson = res.getJson();
		String res_result = resJson.toStringUtf8();
		System.out.println(res_result);

//		Set<String> nextLevelIds = new HashSet<>();
//		List<Map<String, Object>> nextLevel = (List<Map<String, Object>>)ppl.all.get(0).get("next_level");
//		countNext(nextLevel, nextLevelIds);
//		System.out.println("size======" + nextLevelIds.size());
		// Print results


	}


}
