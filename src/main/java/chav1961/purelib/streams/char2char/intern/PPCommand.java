package chav1961.purelib.streams.char2char.intern;

import java.util.Arrays;
import java.util.List;

class PPCommand implements Substituter {
	private final boolean	useX;
	private final PPItem[]	left, right;
	private final String	format;
	private final int		nameCount;
	private String[]		parsed = null;
		
	public PPCommand(final boolean useX, final int nameCount, final List<PPItem> left, final List<PPItem> right, final String format) {
		if (nameCount <= 0) {
			throw new IllegalArgumentException("Name count need be positive");
		}
		else if (left == null || left.size() == 0) {
			throw new IllegalArgumentException("Left list can't be null or empty");
		} 
		else if (right == null || right.size() == 0) {
			throw new IllegalArgumentException("Right list can't be null or empty");
		}
		else if (format == null || format.isEmpty()) {
			throw new IllegalArgumentException("Format can't be null or empty");
		}
		else {
			this.useX = useX;		this.nameCount = nameCount;
			this.left = left.toArray(new PPItem[left.size()]);
			this.right = right.toArray(new PPItem[right.size()]);
			this.format = format;
		}
	}

	@Override
	public int match(final char[] data, int from) {
		final int	len;
		
		if (data == null || (len = data.length) == 0) {
			throw new IllegalArgumentException("Data can't be null or empty array"); 
		}
	 	else if (from < 0 || from >= len) {
			throw new IllegalArgumentException("From position ["+from+"] outside the range 0.."+len); 
		}
		else {
			parsed = new String[nameCount];
			return parse(data,from,left);
		}
	}

	@Override
	public String substitute() {
		if (parsed == null) {
			throw new IllegalStateException("Substitute without match! Call match firstly");
		}
		else {
			final String[]	parm = new String[right.length];
			
			for (int index = 0; index < right.length; index++) {
				parm[index] = right[index].convert(parsed[right[index].nameId]);
			}
			parsed = null;
			return String.format(format,(Object[])parm);
		}
	}
	
	@Override
	public String toString() {
		return "PPCommand [useX=" + useX + ", left=" + Arrays.toString(left) + ", right=" + Arrays.toString(right) + ", format=" + format + ", nameCount=" + nameCount + ", parsed=" + Arrays.toString(parsed) + "]";
	}

	private int parse(final char[] data, int from, final PPItem[] items) {
		final int	endData = data.length;
		int			processed;

loop:	for (PPItem item : items) {
			from = Util.skipBlank(data,from);
			
			switch (item.type) {
				case Literal	:
					if ((processed = Util.compare(data,from,item.template,useX)) < 0) {
						return -1;
					}
					else {
						from = processed;
					}
					break;
				case Option		:
					if ((processed = parse(data,from,item.options)) > 0) {
						from = processed;
					}
					break;
				case Regular	:
					final int	startName = from, endName = from = Util.skipName(data,from);
					
					parsed[item.nameId] = new String(data,startName,endName-startName);
					break;
				case List		:
					final int	startList = from;
					int			endList = from;
					
					from--;
					do {if (++from < endData && (data[from] == '(' || data[from] == '{' || data[from] == '[' || data[from] == '&')) {
							endList = Util.skipColon(data,from);
						}
						else {
							endList = Util.skipName(data,from);
						}
						from = Util.skipBlank(data,endList);
					} while (from < endData && data[from] == ',');
					
					parsed[item.nameId] = new String(data,startList,endList-startList);
					break;
				case Restricted	:
					if (from < endData && data[from] == '&') {
						final int	startExpr = from, endExpr = Util.skipExpression(data,from+1);
						
						for (char[] canBe : item.available) {
							if (canBe.length > 0 && canBe[0] == '&') {
								parsed[item.nameId] = new String(data,startExpr,endExpr-startExpr);
								break loop;
							}
						}
					}
					else {
						for (char[] canBe : item.available) {
							if ((processed = Util.compare(data,from,canBe,true)) > 0) {
								parsed[item.nameId] = new String(canBe);
								from = processed;
								break loop;
							}								
						}							
					}
					return -1;
				case Extended	:
					final int	startExpr = from, endExpr = from = Util.skipExpression(data,from);
					
					parsed[item.nameId] = new String(data,startExpr,endExpr-startExpr);
					break;
				case Tail		:
					parsed[item.nameId] = new String(data,from,data.length-from);
					return data.length;
				default : return -1;
			}
		}
		return from;
	}
}