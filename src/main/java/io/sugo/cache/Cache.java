package io.sugo.cache;

import com.google.common.cache.*;
import io.sugo.http.Configure;
import io.sugo.kafka.factory.KafkaFactory;
import io.sugo.zookeeper.ZKClientHandler;
import io.sugo.zookeeper.factory.ZkFactory;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 * Created by chenyuzhi on 17-10-23.
 */
public class Cache {
	private static final Logger LOG = LogManager.getLogger(Cache.class);
	private static LoadingCache<String,KafkaConsumer> kafkaConsumerCache;
	private static LoadingCache<String,ZKClientHandler> zkClientCache;
	private static Object lock = new Object();


	public static LoadingCache<String,KafkaConsumer> getKafkaConsumerCache() {
		if(null == kafkaConsumerCache){        //lazy initialization
			synchronized (lock){
				if(null == kafkaConsumerCache){
					LOG.info("creating kafkaConsumerCache...");
					kafkaConsumerCache = CacheBuilder.newBuilder()
							.maximumSize(100)
							.expireAfterAccess(10, TimeUnit.MINUTES)
							.removalListener(new RemovalListener<String, KafkaConsumer>() {
								public void onRemoval(RemovalNotification<String, KafkaConsumer> removal){
									removal.getValue().close();
								}
							})
							.build(
									new CacheLoader<String, KafkaConsumer>() {
										@Override
										public KafkaConsumer load(String s)  {
											KafkaConsumer kafkaConsumer = null;
											try{
												LOG.info("created KafkaConsumer with key:"+s);
												kafkaConsumer = KafkaFactory.newConsumer(s);
											}catch (Exception e){
												LOG.error(e.getMessage(),e);
											}

											return kafkaConsumer;
										}
									}
							);
					LOG.info("kafkaConsumerCache has created successfully...");
				}
			}
		}
		return kafkaConsumerCache;
	}

	public static LoadingCache<String,ZKClientHandler> getZkClientCache() {
		if(null == zkClientCache){        //lazy initialization
			synchronized (lock){
				if(null == zkClientCache){
					LOG.info("creating zkClientCache...");
					zkClientCache = CacheBuilder.newBuilder()
							.maximumSize(100)
							.expireAfterAccess(10, TimeUnit.MINUTES)
							.removalListener(new RemovalListener<String, ZKClientHandler>() {
								public void onRemoval(RemovalNotification<String, ZKClientHandler> removal){
									removal.getValue().close();
								}
							})
							.build(
									new CacheLoader<String, ZKClientHandler>() {
										@Override
										public ZKClientHandler load(String s)   {
											ZKClientHandler zkClientHandler = null;
											try{
												LOG.info("created zkClientHandler with key:"+s);
												zkClientHandler = new ZKClientHandler(s, ZkFactory.getFactory().newClient(s));
											}catch(Exception e){
												LOG.error(e.getMessage(),e);
											}

											return zkClientHandler;
										}
									}
							);
					LOG.info("zkClientCache has created successfully...");
				}
			}
		}
		return zkClientCache;
	}
}
