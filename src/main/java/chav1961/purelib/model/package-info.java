/**
 * <p>This package contains an API to use with models. <b>Model</b> is a container with {@linkplain chav1961.purelib.model.interfaces.ContentMetadataInterface ContentMetadataInterface}
 * type. It contains a tree of {@linkplain chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata ContentNodeMetadata} nodes. Every node describes an entity
 * in the model. This package has a set of classes to manage this model at the runtime:</p>
 * <ul>
 * <li>{@linkplain chav1961.purelib.model.SimpleContentMetadata} class, implemented {@linkplain chav1961.purelib.model.interfaces.ContentMetadataInterface ContentMetadataInterface}</li>
 * <li>{@linkplain chav1961.purelib.model.MutableContentNodeMetadata} class, implemented {@linkplain chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata ContentNodeMetadata}</li>
 * <li>{@linkplain chav1961.purelib.model.ContentModelFactory ContentModelFactory} class to load model from external or to build model by existent entities</li>
 * <li>{@linkplain chav1961.purelib.model.FieldFormat FieldFormat} class to describe format associated with the model entity</li>
 * <li>{@linkplain chav1961.purelib.model.ModelUtils ModelUtils} utility class contains set of useful methods to manipulate with models</li>
 * </ul>
 * <p>Model and it's API is a base for almost all the Pure Library infrastructure.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 * @lastUpdate 0.0.4
 */
package chav1961.purelib.model;