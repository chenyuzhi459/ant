package io.sugo.server.redis;

import redis.clients.jedis.HostAndPort;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

public class RedisInfo {
  private final String hostAndPorts;
  private final boolean clusterMode;
  private final boolean sentinelMode;
  private final String masterName;
  private final String password;

  private final String clientStr;
  private Set<HostAndPort> nodes;

  public RedisInfo(
      String hostAndPorts,
      boolean clusterMode,
      boolean sentinelMode,
      String masterName,
      String password
  ){
    this.hostAndPorts = hostAndPorts;
    this.clusterMode = clusterMode;
    this.sentinelMode = sentinelMode;
    this.masterName = masterName;
    this.password = password;
    nodes = parseHostAndPorts(hostAndPorts);
    clientStr = String.format("%s-%s-%s-%s-%s", hostAndPorts, clusterMode, sentinelMode, masterName, password);
  }

  private Set<HostAndPort> parseHostAndPorts(String hostAndPorts) {
    StringTokenizer tokenizer = new StringTokenizer(hostAndPorts, ",;");
    String token;
    String[] tmp;
    Set<HostAndPort> nodes = new HashSet<>();
    while (tokenizer.hasMoreTokens()) {
      token = tokenizer.nextToken();
      tmp = token.split(":");
      nodes.add(new HostAndPort(tmp[0], Integer.valueOf(tmp[1])));
    }
    return nodes;
  }

  public String getHostAndPorts() {
    return hostAndPorts;
  }

  public boolean isClusterMode() {
    return clusterMode;
  }

  public boolean isSentinelMode() {
    return sentinelMode;
  }

  public String getMasterName() {
    return masterName;
  }

  public String getPassword() {
    return password;
  }

  public String getClientStr() {
    return clientStr;
  }

  public Set<HostAndPort> getNodes() {
    return nodes;
  }

  @Override
  public String toString() {
    return "DataRedisIOFactory{" +
        "nodes='" + hostAndPorts + '\'' +
        ", clusterMode='" + clusterMode + '\'' +
        ", sentinelMode='" + sentinelMode + '\'' +
        ", masterName='" + masterName + '\'' +
        ", password='" + password + '\'' +
        '}';
  }

  @Override
  public int hashCode() {
    int result = (hostAndPorts != null ? hostAndPorts.hashCode() : 0);
    result = 31 * result + (password != null ? password.hashCode() : 0);
    result = 31 * result + (clusterMode ? 1 : 0);
    result = 31 * result + (sentinelMode ? 1 : 0);
    result = 31 * result + (masterName != null ? masterName.hashCode() : 0);
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

    RedisInfo that = (RedisInfo) o;
    if (hostAndPorts != null ? !hostAndPorts.equals(that.hostAndPorts) : that.hostAndPorts != null) {
      return false;
    }
    if (password != null ? !password.equals(that.password) : that.password != null) {
      return false;
    }
    if (clusterMode != that.clusterMode) {
      return false;
    }
    if (sentinelMode != that.sentinelMode) {
      return false;
    }
    if (masterName != null ? !masterName.equals(that.masterName) : that.masterName != null) {
      return false;
    }

    return true;
  }
}
