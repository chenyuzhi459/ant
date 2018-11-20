package io.sugo.dgraph.bean;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.sugo.dgraph.query.Query;

import java.util.List;

/**
 * Created by chenyuzhi on 18-11-16.
 */
public class ChainQuery {
	private final List<Query> queries;

	@JsonCreator
	public ChainQuery(
			@JsonProperty("queries") List<Query> queries)
	{
		this.queries = queries;
	}

	@JsonProperty("queries")
	public List<Query> getQueries() {
		return queries;
	}

	public String getSQL(){
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("{\n");
		for(Query q : queries){
			sqlBuilder.append(q.toSQL()).append("\n");
		}
		sqlBuilder.append("}\n");
		return sqlBuilder.toString();
	}
}
