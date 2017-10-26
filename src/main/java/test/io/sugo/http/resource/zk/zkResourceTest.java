package test.io.sugo.http.resource.zk; 

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import io.sugo.http.resource.ForwardResource;
import org.apache.zookeeper.CreateMode;
import org.junit.Test;

import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import static org.junit.Assert.assertEquals;

/** 
* zkResource Tester. 
* 
* @author <Authors name> 
* @since <pre>Oct 26, 2017</pre> 
* @version 1.0 
*/ 
public class zkResourceTest extends ForwardResource implements Runnable{
    public zkResourceTest() {
        pathPre = "http://localhost:6660/api/zk";
    }
    public static final String resultStr = "{\"sourceData\":\"192.168.0.224\",\"nodeType\":\"persist\"}";
/** 
* 
* Method: close() 
* 
*/ 
@Test
public void testClose() throws Exception { 
//TODO: Test goes here... 
} 



/** 
* 
* Method: getData(@QueryParam("path") String  path) 
* 
*/ 
@Test
public void testGetData() throws Exception {
    String queryParams = "path=/druid/announcements";

    String url = String.format("%s/summary/info?%s", pathPre, queryParams);
    System.out.println(url);

    WebResource resource = client.resource(url);
    ClientResponse result = resource.get(ClientResponse.class);
    String str = result.getEntity(String.class);
    System.out.println(str);
    assertEquals(str, resultStr);
}

    /**
     *
     * Method: createNode(@QueryParam("path") String path, @QueryParam("mode") String mode, String data)
     *
     */
    public static String path;
    public static String mode;
    public static String data;

@Test
public void testCreateNode() throws Exception {



    String url = String.format("%s/node", pathPre);
    System.out.println(url);

    MultivaluedMapImpl queryParams = new MultivaluedMapImpl();
    queryParams.add("path",path);
    queryParams.add("mode",mode);

    WebResource resource = client.resource(url);
    ClientResponse result = resource.queryParams(queryParams)
            .accept(MediaType.APPLICATION_JSON)
            .type(MediaType.TEXT_PLAIN)
            .post(ClientResponse.class,data);
    System.out.println(result.getStatus());
//    String str = result.getEntity(String.class);
//    System.out.println(str);
//    assertEquals(str, resultStr);
}

@Test
public void testZkResource() throws Exception {
    path = "/testPersist/1_child";
    mode = "EPHEMERAL_SEQUENTIAL";

    for(int i=0;i<100;i++) {
        data = i+"";
        Thread t = new Thread(new zkResourceTest());
        t.start();
    }
}

/** 
* 
* Method: getDataAndType(@QueryParam("path") String  path) 
* 
*/ 
@Test
public void testGetDataAndType() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getChildren(@QueryParam("path") String  path) 
* 
*/ 
@Test
public void testGetChildren() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: setData(@QueryParam("path") String path, String data) 
* 
*/ 
@Test
public void testSetData() throws Exception { 
//TODO: Test goes here... 
} 



/** 
* 
* Method: deleteNode(@QueryParam("path") String path) 
* 
*/ 
@Test
public void testDeleteNode() throws Exception { 
//TODO: Test goes here... 
}


    @Override
    public void run() {
        try {
//            testGetData();
            testCreateNode();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
