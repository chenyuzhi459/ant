package io.sugo.dgraph.query.directives;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by chenyuzhi on 18-11-16.
 */
public class RecurseDirective implements Directive{
	private final int depth;
	private final boolean loop;

	@JsonCreator
	public RecurseDirective(
			@JsonProperty("depth") int depth,
			@JsonProperty(value = "loop", defaultValue = "false") boolean loop)
	{
		this.depth = depth;
		this.loop = loop;
	}

	@JsonProperty("depth")
	public int getDepth() {
		return depth;
	}

	@JsonProperty("loop")
	public boolean isLoop() {
		return loop;
	}

	@Override
	public String buildQueryString() {
		return depth > 0 ?
				String.format("@recurse(depth: %s, loop: %s)", depth, loop) :
				String.format("@recurse(loop: %s)", loop) ;
	}
}
