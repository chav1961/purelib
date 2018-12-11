package chav1961.purelib.streams.char2byte.asm;

import java.util.ArrayList;
import java.util.List;

import chav1961.purelib.basic.exceptions.ContentException;

class TryManagerRecord extends NestedEntity {
	private final AbstractMethodBody	amb;
	private final short 				codeStart;
	private final long 					labelId;
	private final List<short[]> 		catches = new ArrayList<short[]>();
	private byte[]						exceptionIndex;
	private short						codeEnd = 0;
	private boolean 					labelWasPut = false;

	TryManagerRecord(final AbstractMethodBody amb) {
		if (amb == null) {
			throw new IllegalArgumentException("Method body can't be null");
		}
		else {
			this.amb = amb;
			this.codeStart = amb.getPC();
			this.labelId = amb.getUniqueLabelId();
		}
	}
	
	void processCatch(final short... classes) throws ContentException {
		if (classes == null || classes.length == 0) {
			throw new IllegalArgumentException("Classes list can't be null or empty array");
		}
		else {
			if (exceptionIndex == null) {
				exceptionIndex = new byte[8192];
			}
			short	actualClass;
			for (int index = 0, maxIndex = classes.length; index < maxIndex; index++) {
				if ((exceptionIndex[(actualClass = classes[index]) >> 3] & (1 << (actualClass & 0x07))) != 0) {
					throw new ContentException("One of the exceptions was already assigned to process in the early or the same '.catch' directive");
				}
				else {
					exceptionIndex[actualClass >> 3] |= (1 << (actualClass & 0x07));
				}
			}
			final short[]	catchList = new short[classes.length+1];
			
			System.arraycopy(classes,0,catchList,1,classes.length);
			amb.putCommand(0,(byte)0xC7,(byte)0,(byte)0);
			amb.registerBrunch(labelId,true);
			catchList[0] = amb.getPC();
			catches.add(catchList);
			if (codeEnd == 0) {
				codeEnd = amb.getPC(); 
			}
		}
	}
	
	void processFinally() throws ContentException {
		if (labelWasPut) {
			throw new ContentException("Duplicate '.finally' for this try block was detected");
		}
		else {
			amb.putLabel(labelId);
			labelWasPut = true;
			amb.putCommand(0,(byte)0xC7,(byte)0,(byte)0);
			amb.registerBrunch(labelId,true);
			catches.add(new short[]{amb.getPC(),0});
			if (codeEnd == 0) {
				codeEnd = amb.getPC(); 
			}
		}
	}
	
	short[][] processEnd() throws ContentException {
		if (catches.size() == 0) {
			throw new ContentException("Neither '.catch' nor '.finally' for this try block was detected");
		}
		else {
			int	count = 0, index = 0;
			
			for (short[] item : catches) {
				count += item.length-1;
			}
			
			final short[][]	result = new short[count][];
			
			for (short[] item : catches) {
				for (int classIndex = 1, maxClassIndex = item.length; classIndex < maxClassIndex; classIndex++) {
					result[index++] = new short[]{codeStart,codeEnd,item[classIndex],item[0]};
				}
			}
			catches.clear();
			return result;
		}
	}	
}