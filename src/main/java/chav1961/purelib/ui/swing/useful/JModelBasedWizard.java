package chav1961.purelib.ui.swing.useful;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

import javax.swing.JComponent;
import javax.swing.JDialog;

import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.LocalizerOwner;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.interfaces.ErrorProcessing;
import chav1961.purelib.ui.interfaces.WizardStep;

public class JModelBasedWizard<Common, E extends Enum<?>, Comp extends JComponent> extends JDialogContainer<Common, E, Comp> implements LocalizerOwner, NodeMetadataOwner {
	private static final long serialVersionUID = 3281077715206149792L;

	private final Localizer					localizer;
	private final ContentNodeMetadata		meta;
	private final Function<Class<?>,Object>	producer;

	public JModelBasedWizard(final Localizer localizer, final LoggerFacade logger, final JDialog parent, final ContentNodeMetadata meta, final Common instance, final ErrorProcessing<Common, E> err) {
		this(localizer, logger, parent, meta, instance, err, new DefaultProducer(meta,logger));
	}
	
	public JModelBasedWizard(final Localizer localizer, final LoggerFacade logger, final JDialog parent, final ContentNodeMetadata meta, final Common instance, final ErrorProcessing<Common, E> err, final Function<Class<?>,Object> producer) {
		super(localizer, parent, instance, err, buildWizardSteps(meta, producer));
		if (producer == null) {
			throw new NullPointerException("Produced can't be null");
		}
		else {
			this.meta = meta;
			this.localizer = localizer;
			this.producer = producer;
		}
	}

	@Override
	public ContentNodeMetadata getNodeMetadata() {
		return meta;
	}

	@Override
	public Localizer getLocalizer() {
		return localizer;
	}

	private static <Common, E extends Enum<?>, Comp extends JComponent> WizardStep<Common, E, Comp>[] buildWizardSteps(final ContentNodeMetadata meta, final Function<Class<?>,Object> producer) {
		return null;
	}

	static class DefaultProducer implements Function<Class<?>,Object> {
		private final ContentNodeMetadata	root;
		private final LoggerFacade			logger;
		
		DefaultProducer(final ContentNodeMetadata root, final LoggerFacade logger) {
			if (root == null) {
				throw new NullPointerException("Metadata can't be null");
			}
			else if (logger == null) {
				throw new NullPointerException("Logger facade can't be null");
			}
			else {
				this.root = root;
				this.logger = logger;
			}
		}
		
		@Override
		public Object apply(final Class<?> clazz) {
			try{
				return clazz.getDeclaredConstructor(LoggerFacade.class).newInstance(logger);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				throw new PreparationException(e);
			}
		}
	}
}
