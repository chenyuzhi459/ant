package io.sugo.server.http.resource;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import io.sugo.common.utils.StringUtil;
import io.sugo.server.hive.SQLManager;
import io.sugo.server.hive.bean.SQLBean;
import io.sugo.server.hive.client.HiveClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import javax.annotation.Nullable;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by chenyuzhi on 18-5-9.
 */
@Path("/hive/client")
public class HiveClientResource {
	private static final Logger log = LogManager.getLogger(HiveClientResource.class);
	private SQLManager sqlManager;

	@Inject
	public HiveClientResource(SQLManager sqlManager){
		this.sqlManager = sqlManager;
	}

	@GET
	@Path("/task/result")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResult(){

		Collection results = Collections2.filter(SQLManager.RESULT_MAP.values(), new Predicate<SQLManager.SQLResult>() {
			@Override
			public boolean apply(@Nullable SQLManager.SQLResult sqlResult) {
				assert sqlResult != null;
				sqlResult.setChecked(true);
				return true;
			}
		});
		return Response.ok(ImmutableMap.of("data",results)).build();
	}


	@GET
	@Path("/task/queue")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTaskQueue() {

		int taskCount ;
		Collection<SQLBean> pendingSQLs= SQLManager.PENDING_QUEUE;
		Set<SQLBean> runningSQLs= HiveClient.getRunningSQLs().keySet();

		taskCount = pendingSQLs.size() + runningSQLs.size();
		return Response.ok(ImmutableMap.of(
				"taskCount",taskCount,
				"pendingQueue",pendingSQLs,
				"runningQueue",runningSQLs))
				.build();
	}

	@POST
	@Path("task/cancel")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response cancel(List<String> sqlIds) {

		Response.ResponseBuilder builder ;

		builder = Response.ok(sqlManager.cancel(sqlIds));
		return builder.build();
	}

	@POST
	@Path("/execute/sync")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response executeSync(SQLBean sqlBean) {
		Response.ResponseBuilder builder ;
		try {
			List result = sqlManager.executeSQL(sqlBean);
			builder = Response.ok(ImmutableMap.of("result",result,"success",true,"message","ok"));
		} catch (Exception e){
			log.error("Execute sql error!",e);
			builder = buildExecuteFailedMsg(Response.Status.INTERNAL_SERVER_ERROR,e.getMessage());
		}
		return builder.build();
	}


	@POST
	@Path("/execute")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response execute(SQLBean sqlBean) {
		Response.ResponseBuilder builder ;

		try {
			String queryId = sqlBean.getQueryId();
			if(!StringUtil.isEmpty(queryId)){
				Collection sameSQLs = SQLManager.getSqlBeanInPendingQueue(Collections.singletonList(queryId));
				if(sameSQLs.size() > 0) {
					String message = String.format("sql [%s] has in the queue,please wait!",queryId);
					log.warn(message);
					builder = buildExecuteFailedMsg(Response.Status.INTERNAL_SERVER_ERROR,message);
				}else{
					SQLManager.addSqlBeanToPendingQueue(sqlBean);
					builder =  Response.ok(ImmutableMap.of("result",Collections.emptyList(),"success",true,"message","accepted"));
				}

			}else {
				List result = sqlManager.executeSQL(sqlBean);
				builder = Response.ok(ImmutableMap.of("result",result,"success",true,"message","ok"));
			}

		} catch (Exception e){
			log.error("Execute sql error!",e);
			builder = buildExecuteFailedMsg(Response.Status.INTERNAL_SERVER_ERROR,e.getMessage());
		}
		return builder.build();
	}

	private Response.ResponseBuilder  buildExecuteFailedMsg(Response.Status status,String msg){
		return Response.status(status)
				.entity(ImmutableMap.of("success",false,"message",msg));
	}
}
