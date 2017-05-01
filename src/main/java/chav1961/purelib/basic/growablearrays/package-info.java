/**
 *<p>This package contains a set of growable arrays for all primitive types (except boolean)
 * Differ to List&lt;?&gt;, these classes are optimized for a special use case:</p>
 * <ul>
 * <li>loading a lot of appropriative primitive type data</li> 
 * <li>use them as a plain array of the primitive type</li> 
 * </ul> 
 * <p>Using of these arrays excludes wraping/unwrapping for primitive data, radically reduces memory requirements and speed up performance</p>
 * <p>All classes in the package have the same set of constructors and methods (differs with the type of primitive data only). 
 * All classes in the package are not thread-safe.
 * All classes in the package use two models to keep data:</p>
 * <ul>
 * <li>plain model - contiguous array of the data, that drows twice on overflow (the same as in the {@link java.io.ByteArrayOutputStream}</li>
 * <li>sliced model - two-dimensional array ('root'). that has 'leafs' with the same size</li>
 * </ul>
 * <p>The first model guarantees maximal speed for inserting and reading, but can duplicate memory requirements to store data. The second one
 * restructs memory reuirements with the one empty 'left' in worst case, but slows down performance.</p>
 * <p>To exclude extra moving data and allocating memory, all these classes allow direct access to their internal arrays, so they are not immutable.
 * Method toArray() for all these classes allow access to 'raw' data, without cloning them. Length of returned array in almost all cases will be 
 * greater than really used, so always use only length() method instead of array.length variable!</p>  
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */
package chav1961.purelib.basic.growablearrays;
import chav1961.purelib.basic.interfaces.LoggerFacade;
