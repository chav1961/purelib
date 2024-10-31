/**
 * <p>This module contains Pure Library project content.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @see <a href="http://github.com/chav1961/purelib">Pure Library</a> project
 * @since 0.0.4
 * @last.update 0.0.7
 */
module chav1961.purelib {
	requires transitive java.desktop;
	requires transitive java.scripting;
	requires java.xml;
	requires java.logging;
	requires transitive jdk.jdi;
	requires jdk.unsupported;
	requires transitive java.sql;
	requires transitive java.rmi;
	requires transitive java.management;
	requires transitive java.base;
	requires jdk.compiler;
	requires transitive java.naming;
	requires java.datatransfer;

	exports chav1961.purelib.basic; 
	exports chav1961.purelib.basic.annotations; 
	exports chav1961.purelib.basic.exceptions; 
	exports chav1961.purelib.basic.growablearrays; 
	exports chav1961.purelib.basic.interfaces; 
	exports chav1961.purelib.basic.subscribable; 
	exports chav1961.purelib.basic.xsd;
	exports chav1961.purelib.cdb;
	exports chav1961.purelib.cdb.interfaces;
	exports chav1961.purelib.concurrent;
	exports chav1961.purelib.concurrent.interfaces;
	exports chav1961.purelib.enumerations;
	exports chav1961.purelib.fsys;
	exports chav1961.purelib.fsys.interfaces;
	exports chav1961.purelib.i18n;
	exports chav1961.purelib.i18n.interfaces;
	exports chav1961.purelib.json;
	exports chav1961.purelib.json.interfaces;
	exports chav1961.purelib.math;
	exports chav1961.purelib.matrix;
	exports chav1961.purelib.matrix.interfaces;
	exports chav1961.purelib.model;
	exports chav1961.purelib.model.interfaces;
	exports chav1961.purelib.monitoring;
	exports chav1961.purelib.net;
	exports chav1961.purelib.net.interfaces;
	exports chav1961.purelib.net.fsys;
	exports chav1961.purelib.net.root;
	exports chav1961.purelib.net.self;
	exports chav1961.purelib.parsers;
	exports chav1961.purelib.sql;
	exports chav1961.purelib.sql.content;
	exports chav1961.purelib.sql.interfaces;
	exports chav1961.purelib.sql.junit;
	exports chav1961.purelib.sql.junit.interfaces;
	exports chav1961.purelib.sql.model;
	exports chav1961.purelib.sql.model.interfaces;
	exports chav1961.purelib.sql.util; 
	exports chav1961.purelib.streams;
	exports chav1961.purelib.streams.byte2byte;
	exports chav1961.purelib.streams.byte2char;
	exports chav1961.purelib.streams.char2byte;
	exports chav1961.purelib.streams.char2byte.asm;
	exports chav1961.purelib.streams.char2byte.asm.macro;
	exports chav1961.purelib.streams.char2char;
	exports chav1961.purelib.streams.charsource;
	exports chav1961.purelib.streams.chartarget;
	exports chav1961.purelib.streams.interfaces;
	exports chav1961.purelib.testing;
	exports chav1961.purelib.ui;
	exports chav1961.purelib.ui.interfaces;
	exports chav1961.purelib.ui.swing;
	exports chav1961.purelib.ui.swing.interfaces;
	exports chav1961.purelib.ui.swing.terminal;
	exports chav1961.purelib.ui.swing.useful;
	exports chav1961.purelib.ui.swing.useful.interfaces;
	exports chav1961.purelib.ui.swing.useful.svg;
	
	uses chav1961.purelib.fsys.interfaces.FileSystemInterface;
	provides chav1961.purelib.fsys.interfaces.FileSystemInterface with chav1961.purelib.fsys.FileSystemOnFile, chav1961.purelib.fsys.FileSystemOnFileSystem, chav1961.purelib.fsys.FileSystemOnXMLReadOnly, chav1961.purelib.fsys.FileSystemOnRMI, chav1961.purelib.fsys.FileSystemInMemory;

	uses chav1961.purelib.fsys.interfaces.FileSystemInterfaceDescriptor;
	provides chav1961.purelib.fsys.interfaces.FileSystemInterfaceDescriptor with chav1961.purelib.fsys.FileSystemOnFile, chav1961.purelib.fsys.FileSystemOnFileSystem, chav1961.purelib.fsys.FileSystemOnXMLReadOnly, chav1961.purelib.fsys.FileSystemOnRMI, chav1961.purelib.fsys.FileSystemInMemory;
	
	uses chav1961.purelib.i18n.interfaces.Localizer;
	provides chav1961.purelib.i18n.interfaces.Localizer with chav1961.purelib.i18n.PropertiesLocalizer
			, chav1961.purelib.i18n.XMLLocalizer
			, chav1961.purelib.i18n.MutableJsonLocalizer;

	uses chav1961.purelib.i18n.interfaces.DefaultLocalizerProvider;
	provides chav1961.purelib.i18n.interfaces.DefaultLocalizerProvider with chav1961.purelib.basic.PureLibLocalizerProvider ; 	
	
	uses chav1961.purelib.sql.interfaces.ResultSetContentParser;
	provides chav1961.purelib.sql.interfaces.ResultSetContentParser with chav1961.purelib.sql.content.CsvContentParser, chav1961.purelib.sql.content.XMLContentParser, chav1961.purelib.sql.content.JsonContentParser;	

	uses chav1961.purelib.ui.interfaces.UIServer;
	provides chav1961.purelib.ui.interfaces.UIServer with chav1961.purelib.ui.swing.SwingUIServerImpl;	

	uses java.net.spi.URLStreamHandlerProvider;
	provides java.net.spi.URLStreamHandlerProvider with chav1961.purelib.net.fsys.FSysHandlerProvider, chav1961.purelib.net.root.RootHandlerProvider
			, chav1961.purelib.net.self.SelfHandlerProvider, chav1961.purelib.net.playback.PlaybackHandlerProvider
			, chav1961.purelib.net.capture.CaptureHandlerProvider, chav1961.purelib.net.namingrepo.NamingRepoHandlerProvider;
	
	uses javax.script.ScriptEngineFactory;
	provides javax.script.ScriptEngineFactory with chav1961.purelib.basic.AsmScriptEngineFactory;
	
	uses java.nio.file.spi.FileSystemProvider;
	provides java.nio.file.spi.FileSystemProvider with chav1961.purelib.fsys.PureLibFileSystemProvider;
	
	uses chav1961.purelib.ui.swing.interfaces.SwingItemRenderer;
	provides chav1961.purelib.ui.swing.interfaces.SwingItemRenderer with 
			  chav1961.purelib.ui.swing.useful.renderers.EnumRenderer
			, chav1961.purelib.ui.swing.useful.renderers.FilterCallbackRenderer
			, chav1961.purelib.ui.swing.useful.renderers.IconAndTextKeeperRenderer
			, chav1961.purelib.ui.swing.useful.renderers.JFileItemDescriptorRenderer
			, chav1961.purelib.ui.swing.useful.renderers.NumericRenderer
			, chav1961.purelib.ui.swing.useful.renderers.ItemAndSelectionRenderer
			, chav1961.purelib.ui.swing.useful.renderers.ReferenceAndCommentRenderer
			, chav1961.purelib.ui.swing.useful.renderers.LongItemAndReferenceRenderer			
			, chav1961.purelib.ui.swing.useful.renderers.ContentNodeMetadataRenderer			
			, chav1961.purelib.ui.swing.useful.renderers.StringRenderer;

	uses chav1961.purelib.ui.swing.interfaces.SwingItemEditor;
	provides chav1961.purelib.ui.swing.interfaces.SwingItemEditor with 
	  		  chav1961.purelib.ui.swing.useful.editors.JFileItemDescriptorEditor
			, chav1961.purelib.ui.swing.useful.editors.NumericEditor
			, chav1961.purelib.ui.swing.useful.editors.StringEditor;
	
	uses chav1961.purelib.basic.interfaces.LoggerFacade;
	provides chav1961.purelib.basic.interfaces.LoggerFacade with
			 chav1961.purelib.basic.StringLoggerFacade;
	
	uses java.sql.Driver;
	
	uses chav1961.purelib.sql.model.interfaces.DatabaseModelAdapter;
	provides chav1961.purelib.sql.model.interfaces.DatabaseModelAdapter with chav1961.purelib.sql.model.internal.PostgreSQLDatabaseModelAdapter
			, chav1961.purelib.sql.model.internal.SqliteDatabaseModelAdapter
			, chav1961.purelib.sql.model.internal.DefaultDatabaseModelAdapter;
	
	uses javax.naming.spi.InitialContextFactory;
	provides javax.naming.spi.InitialContextFactory with chav1961.purelib.basic.SimpleInitialContextFactory;

	uses chav1961.purelib.matrix.interfaces.MatrixFactory;
	provides chav1961.purelib.matrix.interfaces.MatrixFactory with chav1961.purelib.matrix.internal.OrdinalMatrixFactory;
}
