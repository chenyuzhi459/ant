package io.sugo.kafka.factory;

import io.sugo.cache.Cache;
import io.sugo.http.Configure;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.log4j.Logger;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * Created by chenyuzhi on 17-10-23.
 */
public class KafkaFactory {

	private static final Logger LOG = Logger.getLogger(KafkaFactory.class);
	private static  KafkaFactory factory = new KafkaFactory();
	public static Configure configure;
	private KafkaFactory(){
	}

	public static KafkaFactory getFactory(Configure conf){
		configure = conf;
		return factory;
	}

	public  static KafkaConsumer<byte[], byte[]> newConsumer(String consumerId) {

		ClassLoader currCtxCl = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(KafkaFactory.class.getClassLoader());

			final Properties props = new Properties();
			//获取最新配置
			final Configure configure = Configure.getConfigure();
			props.setProperty("bootstrap.servers", consumerId.replace("[","").replace("]",""));

			props.setProperty("enable.auto.commit", configure.getProperty("kafka.properties","enable.auto.commit","false"));
			props.setProperty("auto.offset.reset", configure.getProperty("kafka.properties","auto.offset.reset","none"));
			props.setProperty("key.deserializer", configure.getProperty("kafka.properties","key.deserializer",ByteArrayDeserializer.class.getName()));
			props.setProperty("value.deserializer", configure.getProperty("kafka.properties","value.deserializer",ByteArrayDeserializer.class.getName()));
			System.out.println("kafka consumer properties:" + props);
			return new KafkaConsumer<>(props);
		} finally {
			Thread.currentThread().setContextClassLoader(currCtxCl);
		}
	}

	public KafkaConsumer getConsumer() throws ExecutionException {
		return Cache.getKafkaConsumerCache().get("consumer");
	}

	public static KafkaConsumer getConsumer(String key)  {
		try {
			LOG.info("conusmerId:"+key);
			return Cache.getKafkaConsumerCache().get(key);
		} catch (ExecutionException e) {
			LOG.error(e.getMessage());
		}
		return null;
	}

}
