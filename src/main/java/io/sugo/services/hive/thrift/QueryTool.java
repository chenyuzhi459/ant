package io.sugo.services.hive.thrift;

import org.apache.hadoop.hive.thrift.client.TUGIAssumingTransport;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hive.service.auth.PlainSaslHelper;
import org.apache.hive.service.rpc.thrift.TCLIService;
import org.apache.hive.service.rpc.thrift.TOpenSessionReq;
import org.apache.hive.service.rpc.thrift.TOpenSessionResp;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TSaslClientTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenyuzhi on 19-11-15.
 */
public class QueryTool {
	private static Logger logger = LoggerFactory.getLogger(QueryTool.class);

	public static TTransport getSocketInstance(String host, int port)
			throws IOException {
		TTransport transport = new TSocket(host, port);
		//transport.setTimeout(100000);
		Map<String, String> saslProperties = new HashMap<String, String>();
		saslProperties.put("javax.security.sasl.qop", "auth-conf,auth-int,auth");
		saslProperties.put("javax.security.sasl.server.authentication", "true");
//		saslProperties.put("hive.server2.logging.operation.level", "VERBOSE");

		logger.info("Security is enabled: {}", UserGroupInformation.isSecurityEnabled());

		UserGroupInformation currentUser = UserGroupInformation.getCurrentUser();
		logger.info("Current user: {}", currentUser);

		TSaslClientTransport saslTransport = new TSaslClientTransport(
				"PLAIN", // tell SASL to use GSSAPI, which supports Kerberos
				null, // authorizationid - null
				null, // kerberos primary for server - "myprincipal" in myprincipal/my.server.com@MY.REALM
				"",// kerberos instance for server - "my.server.com" in myprincipal/my.server.com@MY.REALM
				saslProperties, // Properties set, above
				new PlainSaslHelper.PlainCallbackHandler("root", "123456"), // callback handler - null
				transport); // underlying transport


		return new TUGIAssumingTransport(saslTransport, currentUser);
	}

	/**
	 * 如果使用此方法中设置的user进行访问，则需要 HiveServer2 启用模拟 hive.server2.enable.impersonation,
	 * hive.server2.enable.doAs = true即HiveServer2 Default Group打钩 获取TOpenSessionResp
	 *
	 * @return
	 * @throws TException
	 */
  /**/
	public static TOpenSessionResp openSession(TCLIService.Client client, String user, String pwd)
			throws TException {
		TOpenSessionReq openSessionReq = new TOpenSessionReq();
		openSessionReq.setUsername(user);
		openSessionReq.setPassword(pwd);
		openSessionReq.setUsernameIsSet(true);

		return client.OpenSession(openSessionReq);
	}

	public static TOpenSessionResp openSession(TCLIService.Client client) throws TException {
		TOpenSessionReq openSessionReq = new TOpenSessionReq();
		return client.OpenSession(openSessionReq);
	}

}
