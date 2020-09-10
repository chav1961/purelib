package chav1961.purelib.streams.char2char.intern;

import java.util.Arrays;
import java.util.List;

class PPItem {
	final PPItemType	type;
	final int			nameId;
	final char[]		template;
	final char[][]		available;
	final PPItem[]		options;	
	
	public PPItem(final char[] cmd, final int from, final int to) {
		final int		len;
		
		if (cmd == null || (len = cmd.length) == 0) {
			throw new IllegalArgumentException("Command can't be null or empty array");
		}
		else if (from < 0 || from >= len) {
			throw new IllegalArgumentException("From position ["+from+"] outside the range 0.."+len);
		}
		else if (to < from || to+from > len) {
			throw new IllegalArgumentException("To position ["+to+"] outside the range "+from+".."+len);
		}
		else {
			this.type = PPItemType.Literal;			this.nameId = -1;
			this.available = null;					this.options = null;
			this.template = new char[to-from];		System.arraycopy(cmd,from,template,0,to-from);
		}
	}

	public PPItem(final int nameId, final List<char[]> available) {
		if (nameId < 0) {
			throw new IllegalArgumentException("Name id can't be negative");
		}
		else if (available == null || available.size()== 0) {
			throw new IllegalArgumentException("Available collection can't be null or empty");
		}
		else {
			this.type = PPItemType.Restricted;		this.template = null;
			this.nameId = nameId;					this.available = available.toArray(new char[available.size()][]);
			this.options = null;
		}
	}

	public PPItem(final PPItemType type, final int nameId) {
		if (type == null) {
			throw new IllegalArgumentException("Type can't be null!");
		}
		else if (nameId < 0) {
			throw new IllegalArgumentException("Name id can't be negative");
		}
		else {
			this.type = type;						this.template = null;
			this.nameId = nameId;					this.available = null;
			this.options = null;
		}
	}

	public PPItem(final PPItem... options) {
		this.type = PPItemType.Option;				this.template = null;
		this.nameId = -1;							this.available = null;
		this.options = options;
	}

	
	public String convert(final String source) {
		switch (type) {
			case Regular : case Tail : return (source == null || source.trim().isEmpty()) ? "" : source;
			case Extended	: return "("+source+")";
			case Quoted		: return '\"'+source+'\"';
			case Blocked	: return "{||"+source+"}";
			case Logical	: return (source == null || source.trim().isEmpty()) ? ".F." : ".T.";
			case Dumb		: return (source == null || source.trim().isEmpty()) ? "\"\"" : '\"'+source+'\"';
			default : return source;
		}
	}

	@Override
	public String toString() {
		return "Item [type=" + type + ", nameId=" + nameId + ", template=" + Arrays.toString(template) + ", available=" + Arrays.toString(available) + ", options=" + Arrays.toString(options) + "]";
	}
}