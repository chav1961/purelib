package chav1961.purelib.nanoservice.interfaces;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <p>This annotation marks request URI path pattern matches to the given method call, and query type to accept. Path pattern is a string that can contains a special
 * template <code>{&lt;name&gt;</code>, that matches to any path component, for example:</p>
 * <code>@Path("/myPath/subpath1/{Id}/subpath2"</code>
 * <p>Name in the path can be extracted to method call parameter by the {@linkplain FromPath} annotation</p>
 * @see RootPath
 * @see FromPath
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface Path {
	/**
	 * @return path pattern
	 */
	String value();
	/**
	 * @return HTTP query types to process
	 */
	QueryType[] type() default {QueryType.GET};
}
