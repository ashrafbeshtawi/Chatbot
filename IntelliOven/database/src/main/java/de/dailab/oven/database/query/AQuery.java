package de.dailab.oven.database.query;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.dailab.oven.database.configuration.Graph;

public abstract class AQuery {

	@Nonnull
	protected static final Logger ALOG = Logger.getLogger(AQuery.class.getName());
	@Nonnull
	protected static final ForkJoinPool POOL = ForkJoinPool.commonPool();
	@Nullable 
	protected Graph graph;
	@Nonnull
	private final Set<AQuery> queries = new HashSet<>();
	
	/**
	 * Initialize empty to set Graph later on
	 */
	public AQuery() {}
	
	/**
	 * Initialize Query with the graph to query
	 * @param graph	The graph to query
	 */
	public AQuery(@Nullable Graph graph) {
		setGraph(graph);
	}
	
	/**
	 * Sets the used graph to the new graph instance
	 * @param graph The graph to query in future
	 */
	public void setGraph(@Nullable Graph graph) {
		this.graph = graph;
		this.queries.forEach(q -> q.setGraph(graph));
	}

	/**
	 * @return The currently used graph instance
	 */
	@Nullable
	public Graph getGraph() {
		return this.graph;
	}
	
	/**
	 * Closes the current graph connection
	 */
	public void close() {
		this.graph.close();
		this.queries.forEach(AQuery::close);
	}
	
	/**
	 * Add a query to the set of queries
	 * @param query	The {@link AQuery to add}
	 */
	public void addQuery(AQuery query) {this.queries.add(query);}
	
	/**
	 * @return The set of {@link AQuery}s
	 */
	public Set<AQuery> getQueries() {return this.queries;}
	
	/**
	 * Instantiates the class if necessary with the current used graph.
	 * Adds it if necessary to the set of used queries.
	 * @param queryClass The {@link AQuery}-subclass of interest
	 * @return A new instance or the already used instance
	 */
	@Nullable
	public AQuery getQuery(Class<?> queryClass) {
				
		if(!queryClass.getSuperclass().equals(AQuery.class)) return null;
				
		for(AQuery q : this.queries) {
			if(q.getClass().equals(queryClass)) return q;
		}
		
		try {
			Object object = queryClass.newInstance();
			AQuery aQuery = (AQuery) object;
			aQuery.setGraph(this.graph);
			this.queries.add(aQuery);
			return aQuery;
		} catch (InstantiationException | IllegalAccessException e) {
			ALOG.log(Level.INFO, "Could not instantiate {0}", queryClass.getName());
			return null;
		}
	}
	
	/**
	 * Logs that connection to database is lost
	 */
	protected void logLostConnection(@Nonnull Logger logger) {
		logger.log(Level.WARNING, "Lost database connection. Querying is skipped");
	}
	
	/**
	 * @param str String which shall be used as variable
	 * @return String adjusted so that there are just legal characters
	 */
	protected String adjustToVariable(@Nonnull String str) {
		return str.replaceAll("[^a-zA-Z]", "").toLowerCase();
	}
}
