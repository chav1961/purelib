package chav1961.purelib.ui.swing.useful;

import org.junit.jupiter.api.Test;

import chav1961.purelib.ui.swing.interfaces.Undoable.UndoEventType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import org.junit.Assert;

public class CommandHistoryTest {
	@Test
	public void basicTest() {
		final CommandHistory	ch = new CommandHistory(0);
		
		Assert.assertFalse(ch.canUndo());
		Assert.assertFalse(ch.canRedo());

		try{ch.undo();
			Assert.fail("Manatory exception was not detected (can invoke undo())");
		} catch (IllegalStateException exc) {
		}
		try{ch.redo();
			Assert.fail("Manatory exception was not detected (can invoke redo())");
		} catch (IllegalStateException exc) {
		}
		try{ch.getCurrentItem();
			Assert.fail("Manatory exception was not detected (can invoke redo())");
		} catch (IllegalStateException exc) {
		}
		
		ch.appendUndo("test");
		Assert.assertFalse(ch.canUndo());
		Assert.assertFalse(ch.canRedo());

		Assert.assertEquals("test", ch.getCurrentItem());
		
		try{ch.appendUndo(null);
			Assert.fail("Manatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{ch.appendUndo("");
			Assert.fail("Manatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		final CommandHistory	ch1 = new CommandHistory(1);
		
		Assert.assertFalse(ch1.canUndo());
		Assert.assertFalse(ch1.canRedo());
		
		ch1.appendUndo("1");
		Assert.assertFalse(ch1.canUndo());
		Assert.assertFalse(ch1.canRedo());

		ch1.appendUndo("2");
		Assert.assertFalse(ch1.canUndo());
		Assert.assertFalse(ch1.canRedo());
	}
	
	@Test
	public void lifeCycleTest() {
		final CommandHistory	ch = new CommandHistory(0);
		
		Assert.assertFalse(ch.canUndo());
		Assert.assertFalse(ch.canRedo());

		ch.appendUndo("test1");
		Assert.assertFalse(ch.canUndo());
		Assert.assertFalse(ch.canRedo());

		ch.appendUndo("test2");
		Assert.assertTrue(ch.canUndo());
		Assert.assertFalse(ch.canRedo());
		
		Assert.assertEquals("test1", ch.undo());
		Assert.assertFalse(ch.canUndo());
		Assert.assertTrue(ch.canRedo());
		
		Assert.assertEquals("test2", ch.redo());
		Assert.assertTrue(ch.canUndo());
		Assert.assertFalse(ch.canRedo());
		
		ch.clearUndo();
		Assert.assertFalse(ch.canUndo());
		Assert.assertFalse(ch.canRedo());
	}

	@Test
	public void listenersTest() {
		final CommandHistory	ch = new CommandHistory(0);
		final UndoEventType[]	content = new UndoEventType[1];
		
		ch.addUndoListener((e)->content[0] = e.getUndoEventType());

		content[0] = null;
		ch.appendUndo("test1");
		Assert.assertEquals(UndoEventType.APPEND_UNDO, content[0]);

		content[0] = null;
		ch.appendUndo("test2");
		ch.undo();
		Assert.assertEquals(UndoEventType.CHANGE_UNDO, content[0]);

		content[0] = null;
		ch.redo();
		Assert.assertEquals(UndoEventType.CHANGE_UNDO, content[0]);
		
		content[0] = null;
		ch.clearUndo();
		Assert.assertEquals(UndoEventType.CLEAR_UNDO, content[0]);
	}

	@Test
	public void serializationTest() throws IOException {
		final CommandHistory	ch1 = new CommandHistory(0);
		final CommandHistory	ch2 = new CommandHistory(0);
		
		ch1.appendUndo("test1");
		ch1.appendUndo("test2");
		
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
			try(final Writer	wr = new OutputStreamWriter(baos)) {
				ch1.store(wr);
			}
			
			try(final ByteArrayInputStream	bais = new ByteArrayInputStream(baos.toByteArray());
				final Reader	rdr = new InputStreamReader(bais)) {

				ch2.load(rdr);
			}
			Assert.assertEquals("test2", ch2.getCurrentItem());
			Assert.assertEquals("test1", ch2.undo());
		}
	}
}
