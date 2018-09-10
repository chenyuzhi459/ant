package io.sugo.server.http.util;

import com.google.common.collect.ImmutableMap;
import io.sugo.server.http.jersey.JerseyClientFactory;

import javax.ws.rs.core.Response;

/**
 * Created by chenyuzhi on 17-10-23.
 */
public class Test {

	public static void main(String[] args) throws Exception {
		/*ExecutorService pool = Executors.newFixedThreadPool(5);
		for(int i=0;i<60;i++){

			pool.execute(
					new Runnable() {
						@Override
						public void run()  {
							 KafkaHandler.getKafkaHandler(Configure.getConfigure()).printTopicPartition("wuxianjiRT");

						}
					}
			);
		}
		pool.shutdown();*/

		/*DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
		DateTime dateTime = DateTime.parse("2017-07-16 21:07:35", format);
		System.out.println("m:"+ dateTime.getMillis());*/


		HttpMethodProxy httpMethodProxy = new HttpMethodProxy(JerseyClientFactory.create());
		String url = "http://192.168.0.225:8090/druid/indexer/v1/testBean";
//		TaskSearchCondition searchCondition = new TaskSearchCondition("a","b","SUCCESS");
//		searchCondition.setTaskPageItem(ImmutableMap.of("size",10,"offset",1));
//		Response response = httpMethodProxy.postWithObjectParam(url,searchCondition);
//		System.out.println(response.getEntity());
	}

}
