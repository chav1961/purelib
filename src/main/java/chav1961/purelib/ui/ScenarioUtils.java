package chav1961.purelib.ui;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.cdb.AbstractExpressionWatcher;
import chav1961.purelib.cdb.AbstractExpressionWatcher.WatcherGetter;
import chav1961.purelib.cdb.SyntaxNode;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;

public class ScenarioUtils {
	public enum ScenarioAction {
		OK, CANCEL
	}
	
	public interface ModelManipulator {
		ContentNodeMetadata getModel(URI modelURI);
		Object newInstance(ContentNodeMetadata meta);
		Object getInstance(ContentNodeMetadata meta);
	}
	
	public interface MVCStub {
		
	}
	
	public static ScenarioAction show(final FileSystemInterface fsi, final String path, final ModelManipulator mm, final MVCStub stub, final SubstitutableProperties props) throws SyntaxException, IllegalArgumentException, NullPointerException, ContentException, IOException{
		if (fsi == null) {
			throw new NullPointerException("File system interface can't be null");
		}
		else if (path == null || path.isEmpty()) {
			throw new IllegalArgumentException("Path can't be null or empty");
		}
		else if (mm == null) {
			throw new NullPointerException("Model manuipulator can't be null");
		}
		else if (stub == null) {
			throw new NullPointerException("MVC stub can't be null");
		}
		else if (props == null) {
			throw new NullPointerException("Properties can't be null");
		}
		else {
			try (final FileSystemInterface	local = fsi.clone().open(path)) {
				if (!local.exists()) {
					throw new ContentException("Path ["+path+"] not found in the file system ["+fsi.getAbsoluteURI()+"]");
				}
				else {
					try(final Reader	rdr = local.charRead()) {
						return show(rdr,mm,stub,props);
					}
				}
			}
		}
	}

	private static ScenarioAction show(final Reader rdr, final ModelManipulator mm, final MVCStub stub, final SubstitutableProperties props) throws IOException, SyntaxException, ContentException {
		final ScenarioExecutor	ex = new ScenarioExecutor(LexType.class, mm, stub, props);
		final Accessor			acc = new Accessor(mm,  stub, props);
		
		return (ScenarioAction)ex.calculate(CharUtils.terminateAndConvert2CharArray(Utils.fromResource(rdr),'\0'), 0, acc);
	}
	
	private enum LexType {
		
	}
	
	private static class Accessor implements WatcherGetter<LexType, SyntaxNode<LexType,?>> {
		public Accessor(final ModelManipulator mm, final MVCStub stub, final SubstitutableProperties props) {
			// TODO Auto-generated constructor stub
		}
		
		@Override
		public Object extractVariable(final SyntaxNode<LexType, ?> item) throws ContentException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object extractField(final SyntaxNode<LexType, ?> item, final Object value) throws ContentException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object extractIndex(final SyntaxNode<LexType, ?> item, final Object value, final int index) throws ContentException {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	private static class ScenarioExecutor extends AbstractExpressionWatcher<LexType, SyntaxNode<LexType,?>> {
		protected ScenarioExecutor(final Class<LexType> lexClass, final ModelManipulator mm, final MVCStub stub, final SubstitutableProperties props) {
			super(lexClass);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected Object calculate(SyntaxNode<LexType, ?> root, WatcherGetter<LexType, SyntaxNode<LexType, ?>> wg) throws ContentException, NullPointerException, IllegalArgumentException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		protected int parse(char[] expression, int from, SyntaxNode<LexType, ?> root) throws SyntaxException, NullPointerException, IllegalArgumentException {
			// TODO Auto-generated method stub
			return 0;
		}
	}
}
