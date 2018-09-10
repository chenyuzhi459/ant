/*
 *
 *  Licensed to Metamarkets Group Inc. (Metamarkets) under one
 *  or more contributor license agreements. See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership. Metamarkets licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package io.sugo.server.redis;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

import io.sugo.server.redis.serderializer.UserGroupSerDeserializer;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.chrono.ISOChronology;

import java.util.Map;

public class RedisDataLoader {
  @JsonProperty
  private final String hostAndPorts;
  @JsonProperty
  private String groupId;
  @JsonProperty
  private boolean clusterMode;
  @JsonProperty
  private boolean sentinelMode;
  @JsonProperty
  private String masterName;
  @JsonProperty
  private String password;

  private final DataIOFactory dataIOFactory;
  private DateTime updateTime;

  public RedisDataLoader(
      @JsonProperty("hostAndPorts") String hostAndPorts,
      @JsonProperty("clusterMode") boolean clusterMode,
      @JsonProperty("sentinelMode") boolean sentinelMode,
      @JsonProperty("masterName") String masterName,
      @JsonProperty("password") String password,
      @JsonProperty("groupId") String groupId
  ) {
    this.hostAndPorts = hostAndPorts;
    this.clusterMode = clusterMode;
    this.sentinelMode = sentinelMode;
    this.masterName = masterName;
    if (sentinelMode) {
      Preconditions.checkArgument(StringUtils.isNotEmpty(masterName), "master name cannot be null in redis sentinel mode");
    }
    this.groupId = groupId;
    this.password = password;
    this.dataIOFactory = new DataRedisIOFactory(hostAndPorts, clusterMode, sentinelMode, masterName, password, groupId);
  }

  public String getGroupId() {
    return groupId;
  }

  public boolean isPeriod() {
    return false;
  }

  public Period getLoadPeriod() {
    return null;
  }

  @JsonProperty
  public DateTime getUpdateTime() {
    return updateTime;
  }

  public void loadData(Map<byte[], Boolean> cache) {
    UserGroupSerDeserializer deserializer = new UserGroupSerDeserializer(dataIOFactory);
    deserializer.deserialize(cache);
    updateTime = DateTime.now(ISOChronology.getInstanceUTC());
  }

  @Override
  public int hashCode() {
    int result = (hostAndPorts != null ? hostAndPorts.hashCode() : 0);
    result = 31 * result + (groupId != null ? groupId.hashCode() : 0);
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
    if (!(o instanceof RedisDataLoader)) {
      return false;
    }

    RedisDataLoader that = (RedisDataLoader) o;

    if (!hostAndPorts.equals(that.hostAndPorts)) {
      return false;
    }
    if (!groupId.equals(that.groupId)) {
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

  public void close() {
    dataIOFactory.close();
  }
}
