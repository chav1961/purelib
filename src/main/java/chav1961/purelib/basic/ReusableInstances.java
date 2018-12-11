package chav1961.purelib.basic;

import java.util.Arrays;

/**
 * <p>This utility class can be used to reduce memory garbage. It contains a cache repository with the reused instances of any referenced class. It also supports two operations on it:</p>
 * <ul>
 * <li><b>allocate</b> new instance of the given class (see {@linkplain #allocate()} method)</li>
 * <li><b>free</b> instance of the given class was allocated earlier (see {@linkplain #free(Object)} method)</li>
 * </ul>
 * <p>When cache repository is empty on allocation request, it <i>creates</i> a new instance of the given class by calling {@linkplain TrueConstructor} functional interface to produce it. When cache repository is not empty
 * on allocation request, it <i>prepares</i> any 'old' instance form the cache repository by calling {@linkplain PseudoConstructor} functional interface and returns it as a 'newly created' instance. Pseudoconstructor must
 * restore all the instance fields into it's initial state. To avoid problems with the restoring, use the final fields in the instance class accurately.</p>
 * <p>All the functionality of the class is similar to the <b>malloc()/free()</b> functions in the standard C language library. The class implements {@linkplain AutoCloseable} interface and we strongly recommend to use it in the 
 * <b>try-with-resource</b> statements. If you doen't need to prepare 'old' instance, don't pass {@linkplain PseudoConstructor} instance in this class - this strongly increases allocation performance.</p>     
 *  
 * <p>This class can be used in the multi-thread environment</p>
 * @param <T> instance class to keep in the cache repository 
 * @see chav1961.purelib.basic JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 */


public class ReusableInstances<T> implements AutoCloseable {
	private static final int			DEFAULT_REPO_SIZE = 64;
	
	private final TrueConstructor<T>	creator;
	private final PseudoConstructor<T>	preparator;
	private Object[]					repoSingle = new Object[DEFAULT_REPO_SIZE];
	private int							repoSize = 0;

	/**
	 * <p>This interface returns newly created instance to use in the cache</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.2
	 */
	@FunctionalInterface
	public interface TrueConstructor<T> {
		/**
		 * <p>Really create new instance of the class</p>
		 * @return instance created
		 */
		T create();
	}
	
	/**
	 * <p>This iterface resets existent instance if the class to the initial state</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.2
	 */
	@FunctionalInterface
	public interface PseudoConstructor<T> {
		/**
		 * <p>Prepare existent instance for reusing</p>
		 * @param instance class instance to prepare 
		 * @return instance prepared. Must be the same as instance parameter 
		 */
		T prepare(final T instance);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param factory factory to create new instances
	 * @throws NullPointerException factory is null
	 */
	public ReusableInstances(final TrueConstructor<T> factory) throws NullPointerException {
		this(factory,null);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param factory factory to create new instances
	 * @param preparator interface to reset existent instance to it's iitial state
	 * @throws NullPointerException any parameter is null
	 */
	public ReusableInstances(final TrueConstructor<T> factory, final PseudoConstructor<T> preparator) throws NullPointerException {
		if (factory == null) {
			throw new NullPointerException("Content class constructor can't be null");
		}
		else {
			this.creator = factory;
			this.preparator = preparator;
		}
	}

	/**
	 * <p>Allocate new instance and pass it to the user</p>
	 * @return instance allocated
	 */
	@SuppressWarnings("unchecked")
	public synchronized T allocate() {
		if (repoSize == 0) {
			return creator.create();
		}
		else {
			return preparator != null ? preparator.prepare((T)repoSingle[--repoSize]) : (T)repoSingle[--repoSize]; 
		}
	}

	/**
	 * <p>Free instance allocated and return it to cache</p>
	 * @param instance instance to free
	 * @throws NullPointerException instance is null
	 */
	public synchronized void free(final T instance) throws NullPointerException {
		if (instance == null) {
			throw new NullPointerException("Instance to free can't be null");
		}
		else {
			if (repoSize >= repoSingle.length) {
				repoSingle = Arrays.copyOf(repoSingle,2*repoSingle.length);
			}
			repoSingle[repoSize++] = instance;
		}
	}
	
	@Override
	public synchronized void close() {
		repoSize = 0;
		repoSingle = new Object[DEFAULT_REPO_SIZE];
	}
}
