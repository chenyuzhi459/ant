package io.sugo.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import io.sugo.services.query.GroupByQuery;
import io.sugo.services.query.UserGroupQuery;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * Created by chenyuzhi on 19-8-5.
 */
public class QueryUtil {
	private static final Logger log = LogManager.getLogger(QueryUtil.class);
	@Inject
	private static ObjectMapper jsonMapper;

	public static List<Map> getGroupByQueryResult(String broker, GroupByQuery query) {
		try {
			String queryStr = jsonMapper.writeValueAsString(query);
			log.info(String.format("Begin to request GroupByQueryResult, requestMetada:\n" +
					">>>>>>>>>>>>>>>>[GroupByQuery]\n " +
					"broker= %s \n param= %s\n" +
					"<<<<<<<<<<<<<<<<", broker, queryStr));

			long before = System.currentTimeMillis();
			List<Map> result = HttpClinetUtil.getQueryResult(broker, queryStr);
			long after = System.currentTimeMillis();
			log.info(String.format("GroupByQuery total cost %d ms.", after - before));
			return result;
		}catch (Throwable t){
			throw Throwables.propagate(t);
		}
	}

	public static List<Map> getUserGroupQueryResult(String broker, UserGroupQuery query) {
		try {
			String queryStr = jsonMapper.writeValueAsString(query);
			log.info(String.format("Begin to request UserGroupQueryResult, requestMetada:\n" +
					">>>>>>>>>>>>>>>>[UserGroupQuery]\n " +
					"broker= %s \n param= %s\n" +
					"<<<<<<<<<<<<<<<<", broker, queryStr));

			long before = System.currentTimeMillis();

			List<Map> queryResult = HttpClinetUtil.getQueryResult(broker, queryStr);
			Map queryResultMap = queryResult.get(0);
			//prune queryResultMap
			queryResultMap.remove("v");
			queryResultMap.computeIfPresent("event", (key, value)->{
				Map<String, Object> eventMap = (Map)value;
				return Maps.filterKeys(eventMap, (key2-> key2.equals("RowCount")));
			});

			long after = System.currentTimeMillis();
			log.info(String.format("UserGroupQuery total cost %d ms.", after - before));
			return queryResult;
		}catch (Throwable t){
			throw Throwables.propagate(t);
		}
	}
}
