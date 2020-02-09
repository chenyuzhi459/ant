package io.sugo.common.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.sugo.common.redis.serderializer.UserGroupSerDeserializer;
import io.sugo.services.usergroup.bean.usergroup.GroupBean;
import org.apache.parquet.Strings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by chenyuzhi on 19-8-8.
 */
public class UserGroupUtil {
	public static final String AND_OPERATION = "and";
	public static final String OR_OPERATION = "or";
	public static final String EXCLUDE_OPERATION = "exclude";

	public static Map<String, List<GroupBean>> parseMultiUserGroupParam(List<GroupBean> userGroupList)  {
		if(userGroupList == null || userGroupList.isEmpty()) {
			throw new RuntimeException("userGroupList is empty");
		}
		Map<String, List<GroupBean>> paramMap = new HashMap<>(2);
		for(GroupBean userGroupBean : userGroupList){
			String type =  userGroupBean.getType();
			if(Strings.isNullOrEmpty(type)){
				continue;
			}

			if (type.equals("finalGroup")){
				paramMap.put("finalGroup", ImmutableList.of(userGroupBean));
			}else {
				paramMap.computeIfAbsent("AssistantGroupList", (key -> Lists.newArrayList())).add(userGroupBean);
			}
		}
		return paramMap;
	}

	public static Set<String> doDataOperation(String operation, Set<String> data1, Set<String> data2){
		switch (operation){
			case AND_OPERATION :
				return DataIntersection(data1, data2);
//				return DataIntersectionWithoutChange(data1, data2);
			case OR_OPERATION :
				return DataUion(data1, data2);
			case EXCLUDE_OPERATION:
				return DataDifference(data1, data2);
			default :
				break;
		}
		throw new UnsupportedOperationException(String.format("Can not do operation[%s] for user group data", operation));
	}

	//并集
	public static Set<String> DataUion(Set<String> data1, Set<String> data2){
		data1.addAll(data2);
		return data1;
	}

	//交集
	public static Set<String> DataIntersection(Set<String> data1, Set<String> data2){
		data1.retainAll(data2);
		return data1;
	}

	public static Set<String> DataIntersectionWithoutChange(Set<String> data1, Set<String> data2){
		return Sets.intersection(data1, data2);
	}

	//补集 'data1-data2'
	public static Set<String> DataDifference(Set<String> data1, Set<String> data2){
		data1.removeAll(data2);
		return data1;
	}

	//把分群ID写入Redis
	public static int writeDataToRedis(UserGroupSerDeserializer serDeserializer, Set<String> dataSet){
		for(String data: dataSet){
			if(data != null){
				serDeserializer.add(data);
			}
		}
		serDeserializer.serialize();
		return serDeserializer.getRowCount();
	}
}
