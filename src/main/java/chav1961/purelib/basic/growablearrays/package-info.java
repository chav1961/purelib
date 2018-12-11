/**
 * <p>This package contains a set of growable arrays for all primitive types.</p>
 * <p>Using of these arrays excludes wraping/unwrapping for primitive data, radically reduces memory requirements and speed up performance</p>
 * <p>All classes in the package have similar set of constructors and methods (differs with the type of primitive data only). 
 * All classes in the package are not thread-safe.
 * All classes in the package use two models to keep data:</p>
 * <ul>
 * <li>plain model - contiguous array of the data, that grows twice on overflow and shrinks twice or truncation (the same as in the {@link java.io.ByteArrayOutputStream})</li>
 * <li>sliced model - two-dimensional array ('root'). that has 'leafs' with identical size</li>
 * </ul>
 * <p>The first model guarantees maximal speed for inserting and reading, but can duplicate memory requirements to store data. The second one
 * restricts memory requirements with the one empty 'leaf' in worst case, but slows a bit down performance.</p>
 * <p>To exclude extra moving data and allocating memory, all these classes allow direct access to their internal arrays, so they are not immutable.
 * Method toArray() for all these classes allow access to 'raw' data, without cloning them. Length of returned array in almost all cases will be 
 * greater than really used, so always use only length() method instead of array.length variable!</p>
 * <p>A specific method length(int) is now available in all the classes since 0.0.2. It can be used to expand or shrink array content. When array is expanding, 
 * the content of space appended is unpredicatable in general. To initialize it, you can use toArray() method of all the classes to get direct access to their's
 * internal buffer.</p>  
 * 
 * <p>All the classes in the package are not thread-safe</p> 
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1, last change at 0.0.2
 */
package chav1961.purelib.basic.growablearrays;
