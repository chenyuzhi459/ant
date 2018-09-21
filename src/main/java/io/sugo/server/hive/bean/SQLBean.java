package io.sugo.server.hive.bean;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by chenyuzhi on 18-5-9.
 */
public class SQLBean {
	@JsonProperty
	private String queryId;
	@JsonProperty
	private String sql;
	@JsonProperty
	private List params;

	public String getSql() {
		return sql;
	}

	public SQLBean setSql(String sql) {
		this.sql = sql;
		return this;
	}

	public List getParams() {
		return params;
	}

	public SQLBean setParams(List params) {
		this.params = params;
		return this;
	}

	public String getQueryId() {
		return queryId;
	}

	public SQLBean setQueryId(String queryId) {
		this.queryId = queryId;
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.queryId == null) ? 0 : this.queryId.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final SQLBean other = (SQLBean) obj;
		if (this.queryId == null) {
			if (other.queryId != null) {
				return false;
			}
		} else if (!this.queryId.equals(other.queryId)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "SQLBean{" +
				"queryId='" + queryId + '\'' +
				", sql='" + sql + '\'' +
				", params=" + params +
				'}';
	}
}
