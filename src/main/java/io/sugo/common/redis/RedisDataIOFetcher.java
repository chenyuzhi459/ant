package io.sugo.common.redis;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.sugo.services.cache.Caches;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;

/**
 * Created by janpychou on 上午11:01.
 * Mail: janpychou@qq.com
 */
public class RedisDataIOFetcher implements DataIOFetcher {
  private static final Logger log = LogManager.getLogger(RedisDataIOFetcher.class);
  private final static int BATCH_SIZE = 1 * 1024 * 1024;
  private final static int DIRECT_BUFFER_SIZE = 100 * 1024 * 1024;
  private Caches.RedisClientCache cache;

  private String groupId;
  private final RedisInfo redisInfo;

  @JsonCreator
  public RedisDataIOFetcher(
      @JsonProperty("hostAndPorts") String hostAndPorts,
      @JsonProperty("clusterMode") boolean clusterMode,
      @JsonProperty("sentinelMode") boolean sentinelMode,
      @JsonProperty("masterName") String masterName,
      @JsonProperty("password") String password,
      @JsonProperty("groupId") String groupId,
      @JacksonInject Caches.RedisClientCache redisClientCache
  ) {
    this.groupId = groupId;
    redisInfo = new RedisInfo(hostAndPorts, clusterMode, sentinelMode, masterName, password);
    cache = redisClientCache;
  }

  @JsonProperty("groupId")
  public String getGroupId() {
    return groupId;
  }

  @JsonProperty("hostAndPorts")
  public String getHostAndPorts() {
    return redisInfo.getHostAndPorts();
  }

  @JsonProperty("clusterMode")
  public boolean getClusterMode() {
    return redisInfo.isClusterMode();
  }

  @JsonProperty("sentinelMode")
  public boolean getSentinelMode() {
    return redisInfo.isSentinelMode();
  }

  @JsonProperty("masterName")
  public String getMasterName() {
    return redisInfo.getMasterName();
  }

  @JsonProperty("password")
  public String getPassword() {
    return redisInfo.getPassword();
  }

  @Override
  public String toString() {
    return "RedisDataIOFetcher{" +
        ", groupId='" + groupId + '\'' +
        "redisInfo='" + redisInfo + '\'' +
        '}';
  }

  @Override
  public int hashCode() {
    int result = (groupId != null ? groupId.hashCode() : 0);
    result = 31 * result + redisInfo.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    RedisDataIOFetcher that = (RedisDataIOFetcher) o;
    if (groupId != null ? !groupId.equals(that.groupId) : that.groupId != null) {
      return false;
    }
    if (redisInfo != null ? !redisInfo.equals(that.redisInfo) : that.redisInfo != null) {
      return false;
    }
    return true;
  }

  @Override
  public byte[] readBytes() {
    long start = System.currentTimeMillis();
    RedisClientWrapper wrapper = cache.getRedisClient(redisInfo);
    Long listSize = wrapper.llen(groupId);
    if (listSize == 0) {
      cache.releaseRedisClient(redisInfo, wrapper);
      return new byte[0];
    }

    ByteBuffer buf = ByteBuffer.allocate(DIRECT_BUFFER_SIZE);

    byte[] bytes;
    buf.clear();
    for (int i = 0; i < listSize; i++) {
      byte[] item = wrapper.lindex(groupId, i);
      buf.put(item);
    }

    buf.flip();
    bytes = new byte[buf.limit()];
    buf.get(bytes);
    cache.releaseRedisClient(redisInfo, wrapper);
    long end = System.currentTimeMillis();
    log.info(String.format("Read redis[%s] spendTime:%d ms, data bytes:%d", groupId, end - start, bytes.length));
    return bytes;
  }

  @Override
  public void writeBytes(byte[] buf) {
    RedisClientWrapper wrapper = cache.getRedisClient(redisInfo);
    byte[] dest = new byte[BATCH_SIZE];
    int srcPos = 0;
    int length = BATCH_SIZE;
    int totalSize = buf.length;
    wrapper.del(groupId);
    while (srcPos < buf.length) {
      if (length > totalSize - srcPos) {
        length = totalSize - srcPos;
        dest = new byte[length];
      }
      System.arraycopy(buf, srcPos, dest, 0, length);
      srcPos += length;
      wrapper.rpush(groupId, dest);
    }
    cache.releaseRedisClient(redisInfo, wrapper);
  }

  @Override
  public void close() {
  }


  public RedisDataIOFetcher clone(String newGroupId) {

    RedisDataIOFetcher redisIOFactory = new RedisDataIOFetcher(
            redisInfo.getHostAndPorts(),
            redisInfo.isClusterMode(),
            redisInfo.isSentinelMode(),
            redisInfo.getMasterName(),
            redisInfo.getPassword(),
            newGroupId,
            cache
    );
    return redisIOFactory;
  }

  public RedisInfo getRedisInfo() {
    return redisInfo;
  }
}
