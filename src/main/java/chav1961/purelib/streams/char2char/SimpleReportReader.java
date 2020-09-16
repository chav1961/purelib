package chav1961.purelib.streams.char2char;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.LineByLineProcessor;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.growablearrays.GrowableCharArray;
import chav1961.purelib.basic.interfaces.LineByLineProcessorCallback;
import chav1961.purelib.cdb.SyntaxNode;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.FieldFormat.PrintMode;

/*
 * <template>::=<template_captions><part>...
 * <template_caption>::='>>'<key>'='<value>,... 
 * <key>::=<name>
 * <value>::={ '{'<value>,...'}' | <name> }
 * <part>::=<part_caption><part_body>
 * <part_caption>::='>>'<part_name>[':'{'before'|'after'}<field_name>[,...] - changing group of fields
 * <part_name>::=<name>
 * <part_body>::=<text><mark>...
 * <mark>::='&'<expression>':'<format>';'
 * <format>::=@see FieldFormat
 * <expression>::={<ordinal_expression>|<group_expression>}
 * <group_expression>::={'sum'|'count'|'avg'|'min'|'max'}'('<ordinal_expression>')'
 */
public class SimpleReportReader<T> extends Reader {
	private enum Option {
		FirstPage("firstPage",false),
		LastPage("lastPage",false),
		Header("header",false),
		Footer("footer",false),
		CrossPage("crossPage",true);

		private final char[]	keyChars;
		private final boolean	hasList;
		
		Option(final String keyName, final boolean hasList) {
			this.keyChars = keyName.toCharArray();
			this.hasList = hasList;
		}
		
		public char[] getKeyChars() {
			return keyChars;
		}
		
		public boolean hasList() {
			return hasList;
		}
	}

	private enum Grouping {
		Max("max"),
		Min("min"),
		Sum("sum"),
		Count("count"),
		Avg("avg");
		
		private final char[]	keyChars;
		
		Grouping(final String keyName) {
			this.keyChars = keyName.toCharArray();
		}
		
		public char[] getKeyChars() {
			return keyChars;
		}
	}

	private enum SyntaxNodeOperation {
		ROOT, GET_FIELD
	}
	
	private static final char[]		CAPTION_MARK = ">>".toCharArray();
	private static final char[]		BEFORE_MARK = "before ".toCharArray();
	private static final char[]		AFTER_MARK = "after ".toCharArray();
	
	public enum ReaderContent {
		CSV_CONTENT, JSON_CONTENT
	}

	public interface DataSource {
		boolean next() throws ContentException;
		Object getValue(String name) throws ContentException;
	}
	
	private final DataSource				ds;
	private final EnumMap<Option,Object>	props = new EnumMap<>(Option.class);
	private int								partProcessStep = 0;
	
	
	public SimpleReportReader(final DataSource ds, final Reader reportDescriptor) throws NullPointerException, IOException, SyntaxException {
		if (ds == null) {
			throw new NullPointerException("Data source can't be null");
		}
		else if (reportDescriptor == null) {
			throw new NullPointerException("Report descriptor can't be null");
		}
		else {
			final List<PartDescriptor>			list = new ArrayList<>();
			final LineByLineProcessorCallback	callback = new LineByLineProcessorCallback() {
													PartDescriptor	desc = null;
													
													@Override
													public void processLine(final long displacement, final int lineNo, final char[] data, final int from, final int length) throws IOException, SyntaxException {
														if (lineNo == 0) {
															props.putAll(processFirstLine(lineNo, data, from, length));
														}
														else if (CharUtils.compare(data,from,CAPTION_MARK)) {
															if (desc != null) {
																list.add(desc);
															}
															desc = processPartCaption(lineNo, data, from, length);
														}
														else {
															processPartBody(desc,lineNo, data, from, length);
														}
													}

													@Override
													public void terminateProcessing() {
														list.add(desc);
													}
												}; 			
			
			try(final LineByLineProcessor	lblp = new LineByLineProcessor(callback)) {
				lblp.write(reportDescriptor);
			}

			this.ds = ds;
			buildInterpreter(list);
			
		}
	}
	
	private void buildInterpreter(List<PartDescriptor> list) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}

	private boolean processPart() throws ContentException {
		switch (partProcessStep) {
			case 0 :
				processFirstPart();
				partProcessStep = 1;
				return true;
			case 1 :
				if (!processDetailedPart()) {
					partProcessStep = 2;
				}
				return true;
			case 2 :
				processLastPart();
				partProcessStep = 3;
				return true;
			default :
				return true;
		}
	}

	private void processFirstPart() throws ContentException {
		processPart(ds,(String)props.get(Option.FirstPage));
	}
	
	private boolean processDetailedPart() throws ContentException {
		// TODO Auto-generated method stub
		if (!ds.next()) {
			
			return false;
		}
		else {
			return true;
		}
	}

	private void processLastPart() throws ContentException {
		processPart(ds,(String)props.get(Option.LastPage));
	}
	
	private void processPart(final DataSource ds, final String partName) throws ContentException {
		// TODO Auto-generated method stub
		
	}


	public static DataSource forResultSet(final ResultSet rs) {
		return null;
	}

	public static DataSource forReader(final Reader source, final ReaderContent content) {
		return null;
	}
	
	/*
	 * '>>' <option> '=' { <name> | '{' <name> [',' ...] '}' } [',' ...]
	 */
	static EnumMap<Option,Object> processFirstLine(final int lineNo, final char[] data, int from, final int length) throws IOException, SyntaxException {
		final int						begin = from;
		final int[]						forNames = new int[2];
		final EnumMap<Option,Object>	result = new EnumMap<>(Option.class);
		
		if (CharUtils.compare(data,from,CAPTION_MARK)) {
loop:		do {
				from = CharUtils.skipBlank(data,from+CAPTION_MARK.length,true);
				for (Option item : Option.values()) {
					if (CharUtils.compare(data,from,item.getKeyChars())) {
						if (data[from = CharUtils.skipBlank(data,from+CAPTION_MARK.length,true)] == '=') {
							from = CharUtils.skipBlank(data,from+1,true);
							
							if (item.hasList) {
								if (data[from] == '{') {
									final List<String>	values = new ArrayList<>();
									
									do {from = CharUtils.skipBlank(data,from+1,true);
										if (Character.isJavaIdentifierStart(data[from])) {
											from = CharUtils.parseName(data,from,forNames);
											values.add(new String(data,forNames[0],forNames[1]-forNames[0]+1));
										}
										else {
											throw new SyntaxException(lineNo,from-begin,"Missing option value");
										}	
									} while (data[from = CharUtils.skipBlank(data,from,true)] == ',');
									
									if (data[from] == '}') {
										from = CharUtils.skipBlank(data,from+1,true);
										result.put(item,values.toArray(new String[values.size()]));
									}
									else {
										throw new SyntaxException(lineNo,from-begin,"Missing '}'");
									}
								}
								else if (Character.isJavaIdentifierStart(data[from])) {
									from = CharUtils.parseName(data,from,forNames);
									result.put(item,new String(data,forNames[0],forNames[1]-forNames[0]+1));
								}
								else {
									throw new SyntaxException(lineNo,from-begin,"Missing option value");
								}
							}
							else if (Character.isJavaIdentifierStart(data[from])) {
								from = CharUtils.parseName(data,from,forNames);
								result.put(item,new String(data,forNames[0],forNames[1]-forNames[0]+1));
							}
							else {
								throw new SyntaxException(lineNo,from-begin,"Missing option value");
							}
							continue loop;
						}
						else {
							throw new SyntaxException(lineNo,from-begin,"Missing '='");
						}
					}
				}
				
				if (data[from] == '\r' || data[from] == '\n') {
					break;
				}
				else {
					throw new SyntaxException(lineNo,from-begin,"Unknown option key (options are case-sensitive). Valid option keys are "+Arrays.toString(Option.values()));
				}
			} while (data[from = CharUtils.skipBlank(data,from,true)] == ',');
			
			return result;
		}
		else {
			throw new SyntaxException(lineNo,from-begin,"Missing part mark '>>'");
		}
	}

	/*
	 * '>>' <name> [ ':' { 'before' | 'after' } <name> [ ',' ...]
	 */
	static PartDescriptor processPartCaption(final int lineNo, final char[] data, int from, final int length) throws IOException, SyntaxException {
		final int		begin = from;
		final int[]		forNames = new int[2];
		final String	partName;
		boolean			beforeMark = false, afterMark = false;
		
		if (CharUtils.compare(data,from,CAPTION_MARK)) {
			if (Character.isJavaIdentifierStart(data[from = CharUtils.skipBlank(data,from+CAPTION_MARK.length,true)])) {
				from = CharUtils.parseName(data,from,forNames);
				partName = new String(data,forNames[0],forNames[1]-forNames[0]+1);
				
				if (data[from = CharUtils.skipBlank(data,from+CAPTION_MARK.length,true)] == ':') {
					final List<String>	names = new ArrayList<>();
					
					if (CharUtils.compare(data,from = CharUtils.skipBlank(data,from+1,true),AFTER_MARK)) {
						afterMark = true;
						from += AFTER_MARK.length-1;
					}
					else if (CharUtils.compare(data,from = CharUtils.skipBlank(data,from+1,true),BEFORE_MARK)) {
						beforeMark = true;
						from += BEFORE_MARK.length-1;
					}
					else {
						throw new SyntaxException(lineNo,from-begin,"Missing 'before' or 'after' clause (clauses are case-sensitive)");
					}
					do {
						from = CharUtils.skipBlank(data,from+1,true);
						if (Character.isJavaIdentifierStart(data[from])) {
							from = CharUtils.parseName(data,from,forNames);
							names.add(new String(data,forNames[0],forNames[1]-forNames[0]+1));
						}
					} while (data[from = CharUtils.skipBlank(data,from,true)] == ',');
					
					return new PartDescriptor(partName,beforeMark,afterMark,names.toArray(new String[names.size()]));
				}
				else {
					return new PartDescriptor(partName);
				}
			}
			else {
				throw new SyntaxException(lineNo,from-begin,"Missing option value");
			}
		}
		else {
			throw new SyntaxException(lineNo,from-begin,"Missing part mark '>>'");
		}
	}

	/*
	 * [<text>] '&' <expression> ':' <format> [...]
	 * 
	 */
	static void processPartBody(final PartDescriptor desc, final int lineNo, final char[] data, int from, final int length) throws IOException, SyntaxException {
		final int		begin = from;
		int				start = from;

		while (data[from] != '\n' && data[from] != '\r') {
			while (data[from] != '\n' && data[from] != '\r' && data[from] != '&') {
				from++;
			}
			desc.appendText(data,start,from-1);
			if (data[from] == '&') {
				final SyntaxNode<SyntaxNodeOperation,SyntaxNode<?,?>>	root = new SyntaxNode<>(lineNo,0,SyntaxNodeOperation.ROOT,0,null);
				FieldFormat	ff;
				Grouping	groupingType = null;
				
				from = CharUtils.skipBlank(data,from+1,true);
				
				if (desc.isAfter()) {
					for (Grouping item : Grouping.values()) {
						if (CharUtils.compare(data,from,item.getKeyChars())) {
							from = buildExpression(lineNo,data,from+item.getKeyChars().length,root);
							groupingType = item;
							break;
						}
					}
					if (groupingType == null) {
						from = buildExpression(lineNo,data,from,root);
					}
				}
				else {
					from = buildExpression(lineNo,data,from,root);
				}
				
				if (data[from = CharUtils.skipBlank(data,from+1,true)] == ':') {
					start = ++from;
					
					while (data[from] != '\n' && data[from] != '\r' && data[from] != ';') {
						from++;
					}
					if (data[from] == ';') {
						ff = new FieldFormat(Object.class,new String(data,start,from-start));
						from++;
					}
					else {
						throw new SyntaxException(lineNo,from-begin,"Missing ';'");
					}
				}
				else {
					throw new SyntaxException(lineNo,from-begin,"Missing ':'");
				}
				
				if (groupingType != null) {
					desc.appendGroupExpression(groupingType,root,ff);
				}
				else {
					desc.appendOrdinalExpression(root,ff);
				}
				start = from;
			}
		}
		if (from > start) {
			desc.appendText(data,start,from-1);
		}
	}

	static int buildExpression(final int lineNo, final char[] data, int from, final SyntaxNode<SyntaxNodeOperation, SyntaxNode<?, ?>> node) throws SyntaxException {
		final int		begin = from;
		final int[]		forNames = new int[2];
		
		if (Character.isJavaIdentifierStart(data[from])) {
			from = CharUtils.parseName(data,from,forNames);
			node.type = SyntaxNodeOperation.GET_FIELD;
			node.cargo = new String(data,forNames[0],forNames[1]-forNames[0]+1);
			return from;
		}
		else {
			throw new SyntaxException(lineNo,from-begin,"Missing name");
		}
	}


	private abstract static class ItemPrinter {
		public void before(final DataSource ds, final GrowableCharArray<GrowableCharArray<?>> gca) throws ContentException {}
		public abstract void step(final DataSource ds, final GrowableCharArray<GrowableCharArray<?>> gca) throws ContentException;
		public void after(final DataSource ds, final GrowableCharArray<GrowableCharArray<?>> gca) throws ContentException {}
	}
	
	private static class TextItemPrinter extends ItemPrinter {
		private final char[]	content;
		
		TextItemPrinter(final char[] content, final int from, final int to) {
			this.content = Arrays.copyOfRange(content,from,to);
		}

		@Override
		public void step(final DataSource ds, final GrowableCharArray<GrowableCharArray<?>> gca) throws ContentException {
			gca.append(content);
		}

		@Override
		public String toString() {
			return "TextItemPrinter [content=" + new String(content) + "]";
		}
	}

	private static class ExpressionItemPrinter extends ItemPrinter {
		private final SyntaxNode<SyntaxNodeOperation,SyntaxNode<?,?>>	expression;
		private final FieldFormat										format;
		
		ExpressionItemPrinter(final SyntaxNode<SyntaxNodeOperation,SyntaxNode<?,?>> expression, final FieldFormat format) {
			this.expression = expression;
			this.format = format;
		}

		@Override
		public void step(final DataSource ds, final GrowableCharArray<GrowableCharArray<?>> gca) throws ContentException {
			gca.append(getFormat().print(calculate(ds),PrintMode.SINGLE_TEXT));
		}

		protected Object calculate(final DataSource ds) throws ContentException {
			return calculate(ds,expression);
		}

		protected FieldFormat getFormat() {
			return format;
		}
		
		private Object calculate(final DataSource ds, final SyntaxNode<SyntaxNodeOperation,SyntaxNode<?,?>> node) throws ContentException {
			switch (node.getType()) {
				case GET_FIELD	:
					return ds.getValue(node.cargo.toString());
				default	:
					throw new UnsupportedOperationException("Node type ["+node.getType()+"] is not supported yet"); 
			}
		}

		@Override
		public String toString() {
			return "ExpressionItemPrinter [format=" + format + "]";
		}
	}	

	private static class GroupExpressionItemPrinter extends ExpressionItemPrinter {
		private final Grouping	grouping;

		private long			count;
		private Object			grouped; 

		GroupExpressionItemPrinter(final Grouping grouping, final SyntaxNode<SyntaxNodeOperation, SyntaxNode<?, ?>> expression, final FieldFormat format) {
			super(expression, format);
			this.grouping = grouping;
		}

		@Override
		public void step(final DataSource ds, final GrowableCharArray<GrowableCharArray<?>> gca) throws ContentException {
			final Object	obj = calculate(ds);
			
			switch (grouping) {
				case Avg	:
					count++;
					grouped = ((BigDecimal)grouped).add(new BigDecimal(obj.toString()));
					break;
				case Count	:
					count++;
					break;
				case Max	:
					if (grouped == null) {
						grouped = obj;
					}
					else if (grouped instanceof Comparable) {
						if (((Comparable)grouped).compareTo(obj) < 0) {
							grouped = obj;
						}
					}
					else {
						throw new ContentException();
					}
					break;
				case Min	:
					if (grouped == null) {
						grouped = obj;
					}
					else if (grouped instanceof Comparable) {
						if (((Comparable)grouped).compareTo(obj) > 0) {
							grouped = obj;
						}
					}
					else {
						throw new ContentException();
					}
					break;
				case Sum	:
					grouped = ((BigDecimal)grouped).add(new BigDecimal(calculate(ds).toString()));
					break;
				default :
					throw new UnsupportedOperationException("Grouping ["+grouping+"] is not supported yet");
			}
		}
		
		@Override
		public void before(final DataSource ds, final GrowableCharArray<GrowableCharArray<?>> gca) throws ContentException {
			switch (grouping) {
				case Avg	:
					count = 0;
					grouped = new BigDecimal(0);
					break;
				case Count	:
					count = 0;
					break;
				case Max	:
					grouped = null;
					break;
				case Min	:
					grouped = null;
					break;
				case Sum	:
					grouped = new BigDecimal(0);
					break;
				default :
					throw new UnsupportedOperationException("Grouping ["+grouping+"] is not supported yet");
			}
		}

		@Override
		public void after(final DataSource ds, final GrowableCharArray<GrowableCharArray<?>> gca) throws ContentException {
			switch (grouping) {
				case Avg	:
					if (count > 0) {
						gca.append(getFormat().print(((BigDecimal)grouped).divide(new BigDecimal(count)),PrintMode.SINGLE_TEXT));
					}
					else {
						gca.append(getFormat().print(new BigDecimal(0),PrintMode.SINGLE_TEXT));
					}
					break;
				case Count	:
					gca.append(getFormat().print(count,PrintMode.SINGLE_TEXT));
					break;
				case Max	:
					gca.append(getFormat().print(grouped,PrintMode.SINGLE_TEXT));
					break;
				case Min	:
					gca.append(getFormat().print(grouped,PrintMode.SINGLE_TEXT));
					break;
				case Sum	:
					gca.append(getFormat().print(((BigDecimal)grouped),PrintMode.SINGLE_TEXT));
					break;
				default :
					throw new UnsupportedOperationException("Grouping ["+grouping+"] is not supported yet");
			}
		}

		@Override
		public String toString() {
			return "GroupExpressionItemPrinter [grouping=" + grouping + ", super=" + super.toString() + "]";
		}
	}	
	
	private static class PartDescriptor {
		final String			partName;
		final boolean			before;
		final boolean			after;
		final String[]			fieldNames;
		final List<ItemPrinter>	components = new ArrayList<>();
		
		PartDescriptor(final String partName) {
			this.partName = partName;
			this.before = false;
			this.after = false;
			this.fieldNames = new String[0];
		}

		PartDescriptor(final String partName, final boolean before, final boolean after, final String... fieldNames) {
			this.partName = partName;
			this.before = false;
			this.after = false;
			this.fieldNames = fieldNames;
		}

		public boolean isBefore() {
			return before;
		}

		public boolean isAfter() {
			return after;
		}
		
		public void appendText(final char[] content, final int from, final int to) {
			components.add(new TextItemPrinter(content, from, to));
		}
		
		public void appendOrdinalExpression(final SyntaxNode<SyntaxNodeOperation,SyntaxNode<?,?>> expression, final FieldFormat format) {
			components.add(new ExpressionItemPrinter(expression,format));
		}
		
		public void appendGroupExpression(final Grouping grouping, final SyntaxNode<SyntaxNodeOperation,SyntaxNode<?,?>> expression, final FieldFormat format) {
			components.add(new GroupExpressionItemPrinter(grouping,expression,format));
		}
	}
}
