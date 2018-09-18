package io.sugo.server.http.jetty;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import java.util.EnumSet;
import java.util.Map;

/**
 * Created by chenyuzhi on 18-9-18.
 */
public interface ServletFilterHolder {

	/**
	 * Get the Filter object that should be added to the servlet.
	 *
	 * This method is considered "mutually exclusive" from the getFilterClass method.
	 * That is, one of them should return null and the other should return an actual value.
	 *
	 * @return The Filter object to be added to the servlet
	 */
	public Filter getFilter();

	/**
	 * Get the class of the Filter object that should be added to the servlet.
	 *
	 * This method is considered "mutually exclusive" from the getFilter method.
	 * That is, one of them should return null and the other should return an actual value.
	 *
	 * @return The class of the Filter object to be added to the servlet
	 */
	public Class<? extends Filter> getFilterClass();

	/**
	 * Get Filter Initialization parameters.
	 *
	 * @return a map containing all the Filter Initialization
	 * parameters
	 */
	public Map<String,String> getInitParameters();

	/**
	 * The path that this Filter should apply to
	 *
	 * @return the path that this Filter should apply to
	 */
	public String getPath();

	/**
	 * The dispatcher type that this Filter should apply to
	 *
	 * @return the enumeration of DispatcherTypes that this Filter should apply to
	 */
	public EnumSet<DispatcherType> getDispatcherType();
}