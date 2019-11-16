package io.sugo.services.hive.thrift;

//import com.itclj.hive.QueryInstance;
//import com.itclj.kerberos.KerberosLogin;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.tools.javac.util.Convert;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hive.service.rpc.thrift.TCloseSessionReq;
import org.apache.hive.service.rpc.thrift.TOperationHandle;
import org.apache.hive.service.rpc.thrift.TOperationState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by chenyuzhi on 19-11-15.
 */
public class ItcljApplication {
	private static Logger logger = LoggerFactory.getLogger(ItcljApplication.class);
	static QueryInstance base = new QueryInstance();
	public static void main(String[] args) {
		try {
			splitSQL();
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}finally {
			base.close();
		}

	}

	public static void splitSQL() throws Throwable {
		File f = new File(ItcljApplication.class.getClassLoader().getResource("test.sql").toURI());
		BufferedReader reader = new BufferedReader(new FileReader(f));
		StringBuffer sb = new StringBuffer();
		String s;
		while ((s= reader.readLine()) != null){
			//注释的处理
			if(s.startsWith("--")){continue;}
			sb.append(s + "\n\r");
		}
		String sql = sb.toString();
		logger.info("sql===>" + sql);
		String[] sqls = sql.split("\\s*;\\s*(?=([^']*'[^']*')*[^']*$)");

		for(String s1: sqls)
		{
			logger.info("<====" + s1);

			executeSQL(s1);
		}



	}

	public static void executeSQL(String sql) throws Throwable{


		TOperationHandle handle = base.submitQuery(sql);//show databases //select count(*) from cdm_test1


		String log = base.getQueryLog(handle);
		System.out.println("LOG : " + log);

		while (base.getQueryHandleStatus(handle) == TOperationState.RUNNING_STATE) {
			Thread.sleep(5000);
			System.out.println("LOG : " + base.getQueryLog(handle));
		}

		System.out.println(base.getColumns(handle));
		List<Object> listRets = base.getResults(handle);
		base.toResults(listRets);
		for (Object obj : listRets) {
			System.out.println(obj);
		}
	}

}
