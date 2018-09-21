package io.sugo.server.hive.client;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import io.sugo.server.hive.bean.SQLBean;
import io.sugo.server.http.Configure;
import org.apache.hive.jdbc.HivePreparedStatement;
import org.apache.hive.jdbc.HiveStatement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.parquet.Strings;

import javax.annotation.Nullable;
import javax.inject.Named;
import java.io.Closeable;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import static io.sugo.common.utils.Constants.*;


/**
 * Created by chenyuzhi on 18-3-29.
 */
public class HiveClient implements Closeable{
	private static final Logger log = LogManager.getLogger(HiveClient.class);
	private static String driverName = "org.apache.hive.jdbc.HiveDriver";

	@Inject @Named(Hive.JDBC_URL)
	private static String url;
	@Inject @Named(Hive.JDBC_USER)
	private static String user;
	@Inject @Named(Hive.JDBC_PASSWORD)
	private static String password;
	private static long DEFAULT_RETRYINTERVAL_SEC = 10;
	private static Map<SQLBean,HivePreparedStatement> runningSQLs = new ConcurrentHashMap<>();
	private Connection conn;
	private long connRetryInterval;

	public HiveClient(Configure configure) {
		connRetryInterval = configure.getLong(HIVE_PROPS, Hive.HIVE_CONN_RETRY_INTERVAL_SEC, DEFAULT_RETRYINTERVAL_SEC) * 1000;
	}

	public static boolean cancel(String queryId) throws SQLException {
		Set<SQLBean> targetSqlBeans = getSQLBeansByQueyId(queryId);

		if(targetSqlBeans.isEmpty()){
			return true;
		}

		HivePreparedStatement stmt ;
		for(SQLBean sqlBean:targetSqlBeans){
			stmt = runningSQLs.get(sqlBean);
			String sql = sqlBean.getSql();
			log.info(String.format("Cancel sql[%s] : %s",queryId, sql));

			stmt.cancel();
			log.info(String.format("Finished cancel sql[%s]",queryId));
		}
		return true;
	}

	private void mapParams(HivePreparedStatement stmt, List params) throws SQLException {
		if(params == null || params.isEmpty()) return;
		int length = params.size();
		int sqlParameterIndex;
		for (int i = 0; i < length; i++){

			Object param = params.get(i);
			sqlParameterIndex = i+1;
			if(param instanceof Number) {
				stmt.setInt(sqlParameterIndex,((Number)param).intValue());
			}else if(param instanceof String){
				stmt.setString(sqlParameterIndex,(String)param);
			}else if (param instanceof Boolean){
				stmt.setBoolean(sqlParameterIndex,(Boolean)param);
			}else{
				StringBuilder errStrBuilder = new StringBuilder();
				errStrBuilder.append("No mapping available for param type: ")
						.append(param.getClass().getSimpleName())
						.append(" value: ")
						.append(param);
				throw new RuntimeException(errStrBuilder.toString());
			}
		}
	}


	public  List executeQuery(SQLBean sqlBean)
			throws ClassNotFoundException, SQLException {

		List result = new ArrayList();
		if(conn == null){
			conn = getNewConn();
		}

		String queryId = sqlBean.getQueryId();
		String sql = sqlBean.getSql();
		List params= sqlBean.getParams();

		HivePreparedStatement stmt;
		synchronized (runningSQLs){
			this.checkRunningQueue(queryId);
			stmt = (HivePreparedStatement)conn.prepareStatement(sql);
			if(!Strings.isNullOrEmpty(queryId)){
				runningSQLs.put(sqlBean,stmt);
			}
		}

		try{
			String executeMsg = String.format("Executing sql[%s]: %s", queryId == null ? "" : queryId, sql);
			log.info(executeMsg);
			mapParams(stmt,params);
			if(stmt.execute()) result = parseResultSet(stmt.getResultSet());
			return result;
		}finally {
			if(!Strings.isNullOrEmpty(queryId) && runningSQLs.containsKey(sqlBean)) {runningSQLs.remove(sqlBean);}
			if(stmt !=null ) { stmt.close();}
		}
	}


	private static Set<SQLBean> getSQLBeansByQueyId(String queryId){
		return Sets.filter(runningSQLs.keySet(), new Predicate<SQLBean>() {
			@Override
			public boolean apply(@Nullable SQLBean sqlBean) {
				assert sqlBean != null;
				return sqlBean.getQueryId().equals(queryId);
			}
		});
	}

	private void checkRunningQueue(String queryId){
		if(Strings.isNullOrEmpty(queryId) ) return;
		if(getSQLBeansByQueyId(queryId).isEmpty()) return;

		throw new RuntimeException(String.format("sql [%s] has in the running queue,please wait!",queryId));
	}

	private List parseResultSet(ResultSet resultSet) throws SQLException {
		ResultSetMetaData metaData = resultSet.getMetaData();
		int colCount = metaData.getColumnCount();
		List res = Lists.newArrayList();
		while (resultSet.next()) {
			List row = Lists.newArrayList();
			for (int i = 1; i <= colCount; i++) {
				row.add(resultSet.getObject(i));
			}
			res.addAll(row);
		}

		return ImmutableList.copyOf(res);
	}


	public Connection getNewConn() throws ClassNotFoundException, SQLException {
		Class.forName(driverName);
		Connection newConn = DriverManager.getConnection(url, user, password);
		return newConn;
	}

	public boolean retryToGetNewConn(long timeOutMillis){
		int attempt = 0;
		Exception exception = null;
		close();
		long currentTimeMillis = System.currentTimeMillis();
		while (timeOutMillis<=0 || (System.currentTimeMillis() - currentTimeMillis) < timeOutMillis){
			++attempt;
			exception = null;
			String retryMsg = String.format("try to retry get new Connection, attemp[%s]", attempt);
			log.info(retryMsg);
			try {
				conn = getNewConn();
			} catch (Exception e) {
				exception = e;
				log.warn("retry get new Connection occurs exceptiion:" + e.getMessage());
			}

			if(exception == null){
				return true;
			}

			try {
				// after 500ms to retry
				close();
				Thread.sleep(connRetryInterval);
			} catch (InterruptedException e) {
				log.error(e.getMessage(), e);
			}
		}
		String errorMsg = String.format("retry get new Connection failed for timeout[%s s], attemp[%s]", timeOutMillis/1000,attempt);
		log.error(errorMsg);
		if(exception != null){
			log.error(exception.getMessage(), exception);
		}

		return false;
	}

	public void close() {
		if(null != conn) try {
			conn.close();
			conn = null;
		} catch (SQLException e) {
			log.error("Close hive conn error!", e);
		}
	}

	public static Map<SQLBean,HivePreparedStatement> getRunningSQLs(){
		return runningSQLs;
	}
}
