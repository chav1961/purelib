package chav1961.purelib.streams.char2char;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.LineByLineProcessor;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.growablearrays.GrowableCharArray;
import chav1961.purelib.basic.interfaces.LineByLineProcessorCallback;
import chav1961.purelib.cdb.SyntaxNode;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.FieldFormat.PrintMode;
import chav1961.purelib.sql.SQLUtils;

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
public class SimpleReportWriter extends Writer {
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
	
	public enum WriterContent {
		CSV_CONTENT, JSON_CONTENT
	}

	private final Writer					nested;
	private final EnumMap<Option,Object>	props = new EnumMap<>(Option.class);
	private final PrintManager				mgr; 
	
	
	public SimpleReportWriter(final Writer nested, final WriterContent content, final Reader reportDescriptor) throws NullPointerException, IOException, SyntaxException {
		if (nested == null) {
			throw new NullPointerException("Nested writer can't be null");
		}
		else if (content == null) {
			throw new NullPointerException("Writer content can't be null");
		}
		else if (reportDescriptor == null) {
			throw new NullPointerException("Report descriptor can't be null");
		}
		else {
			final List<PartDescriptor>			list = new ArrayList<>();
			final LineByLineProcessorCallback	callback = new LineByLineProcessorCallback() {
													PartDescriptor	desc = null;
													Set<String>		partNames = new HashSet<>();
													
													@Override
													public void processLine(final long displacement, final int lineNo, final char[] data, final int from, final int length) throws IOException, SyntaxException {
														if (lineNo == 1) {
															props.putAll(processFirstLine(lineNo, data, from, length));
														}
														else if (CharUtils.compare(data,from,CAPTION_MARK)) {
															if (desc != null) {
																list.add(desc);
															}
															desc = processPartCaption(lineNo, data, from, length);
															if (!partNames.contains(desc.partName)) {
																partNames.add(desc.partName);
															}
															else {
																throw new SyntaxException(lineNo,0,"Duplcate part name ["+desc.partName+"]");
															}
														}
														else {
															processPartBody(desc,lineNo, data, from, length);
														}
													}

													@Override
													public void terminateProcessing() throws IOException, SyntaxException {
														if (desc != null) {
															list.add(desc);
														}
														else {
															throw new SyntaxException(0,0,"Report template contains no one part");
														}
													}
												}; 			
			
			try(final LineByLineProcessor	lblp = new LineByLineProcessor(callback)) {
				lblp.write(reportDescriptor);
			}

			for (Entry<Option, Object> item : props.entrySet()) {
				switch (item.getKey()) {
					case CrossPage	:
						for (String val : (String[])item.getValue()) {
							if (!partExists(list,val)) {
								throw new SyntaxException(0,0,"Illegal value ["+val+"] for option ["+item.getKey()+"]: part referenced not exists");
							}
						}
						break;
					case FirstPage : case Footer : case Header : case LastPage :
						if (!partExists(list,(String)item.getValue())) {
							throw new SyntaxException(0,0,"Illegal value ["+item.getValue()+"] for option ["+item.getKey()+"]: part referenced not exists");
						}
						break;
					default:
						throw new UnsupportedOperationException("Option ["+item.getKey()+"] is not supported yet");
				}
			}
			this.nested = nested;
			this.mgr = buildInterpreter(list,props);			
		}
	}
	
	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		// TODO Auto-generated method stub
		
	}

	public void write(final ResultSet rs)  throws IOException, NullPointerException, ContentException {
		if (rs == null) {
			throw new NullPointerException("Result set can't be null"); 
		}
		else {
			final DataSource	ds = new DataSource() {
									@Override
									public boolean next() throws IOException {
										try{return rs.next();
										} catch (SQLException e) {
											throw new IOException(e.getLocalizedMessage(),e);
										}
									}
					
									@Override
									public Object getField(final int index) throws IOException {
										try{return rs.getObject(index+1);
										} catch (SQLException e) {
											throw new IOException(e.getLocalizedMessage(),e);
										}
									}
					
									@Override
									public Object getField(final String name) throws IOException {
										try{return rs.getObject(name);
										} catch (SQLException e) {
											throw new IOException(e.getLocalizedMessage(),e);
										}
									}
					
									@Override
									public int fieldIndex(final String name) throws IOException {
										try{return rs.findColumn(name)-1;
										} catch (SQLException e) {
											throw new IOException(e.getLocalizedMessage(),e);
										}
									}
								};
				
			mgr.startDoc(nested);
			while (mgr.detailed(ds,nested)) {
				// nothing to do
			}
			mgr.endDoc(nested);
		}
	}
	
	@Override
	public void flush() throws IOException {
		nested.flush();
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}

	static PrintManager buildInterpreter(final List<PartDescriptor> list, final EnumMap<Option,Object> options) throws SyntaxException {
		// TODO Auto-generated method stub
		final Set<String>		totalFieldList = new HashSet<>();
		final Set<String>		groupingFieldList = new HashSet<>();
		final List<String[]>	partPairs = new ArrayList<>();
		final List<PartStack>	partStack = new ArrayList<>();
		
		collectFieldNames(list,totalFieldList);
loop:	for (PartDescriptor item : list) {
			groupingFieldList.addAll(Arrays.asList(item.fieldNames));
			for (String[] pair : partPairs) {
				if (Arrays.deepEquals(pair,item.fieldNames)) {
					continue loop;
				}
			}
			partPairs.add(item.fieldNames);
		}
		
		for (String[] pair : partPairs) {
			PartDescriptor	before = null, after = null;
			
			for (PartDescriptor item : list) {
				if (Arrays.deepEquals(pair,item.fieldNames)) {
					if (item.isBefore()) {
						if (before == null) {
							before = item;
						}
						else {
							throw new SyntaxException(0,0,"Template has two different parts ["+item.partName+"] and ["+before.partName+"] with type 'before' and identical field lists"); 
						}
					}
					if (item.isAfter()) {
						if (after == null) {
							after = item;
						}
						else {
							throw new SyntaxException(0,0,"Template has two different parts ["+item.partName+"] and ["+after.partName+"] with type 'after' and identical field lists"); 
						}
					}
				}
			}
			partStack.add(new PartStack(pair,before,after));
		}
		final PartStack[]	stack = partStack.toArray(new PartStack[partStack.size()]); 
		
		Arrays.sort(stack,(o1,o2)->o1.fieldNames.length - o2.fieldNames.length);
		
		for (int index = 0, maxIndex = stack.length - 2; index < maxIndex; index++) {
			if (stack[index].fieldNames.length < stack[index+1].fieldNames.length) {
				for (int fieldIndex = 0, maxFieldIndex = stack[index].fieldNames.length; fieldIndex < maxFieldIndex; fieldIndex++) {
					if (!stack[index].fieldNames[fieldIndex].equals(stack[index+1].fieldNames[fieldIndex])) {
						throw new SyntaxException(0,0,"Template structure error in part groups ["+stack[index].toDiagnosticString()+"] and  ["+stack[index].toDiagnosticString()+"]: field list ["+Arrays.toString(stack[index].fieldNames)+"] for second part group must begin with field list of first part group ["+Arrays.toString(stack[index+1].fieldNames)+"]");
					}
				}
			}
			else {
				throw new SyntaxException(0,0,"Template structure error in part groups ["+stack[index].toDiagnosticString()+"] and  ["+stack[index].toDiagnosticString()+"]: field list ["+Arrays.toString(stack[index].fieldNames)+"] for second part group must begin with field list of first part group ["+Arrays.toString(stack[index+1].fieldNames)+"]");
			}
		}

		PartDescriptor	first = null, last = null, detailed = null;
		
loop:	for (PartDescriptor item : list) {
			if (!item.isBefore() && !item.isAfter()) {
				if (options.containsKey(Option.FirstPage) && item.partName.equals(options.get(Option.FirstPage))) {
					first = item;
					continue;
				}
				if (options.containsKey(Option.LastPage) && item.partName.equals(options.get(Option.LastPage))) {
					last = item;
					continue;
				}
				if (options.containsKey(Option.CrossPage)) {
					for (String part : (String[])options.get(Option.CrossPage)) {
						if (part.equals(item.partName)) {
							continue loop;
						}
					}
				}
				if (detailed == null) {
					detailed = item;
				}
				else {
					throw new SyntaxException(0,0,"There are more than one detailed part: ["+detailed.partName+"] and ["+item.partName+"]");
				}
			}
		}
		
		if (detailed == null) {
			throw new SyntaxException(0,0,"There are no any detailed parts in template. Detailed part is a part without before/after and not mentioned in the first report line options");
		}
		else {
			return new PrintManager(totalFieldList, groupingFieldList, first, detailed, last, partStack.toArray(new PartStack[partStack.size()]));
		}
	}

	
	

	/*
	 * '>>' <option> '=' { <name> | '{' <name> [',' ...] '}' } [',' ...]
	 */
	static EnumMap<Option,Object> processFirstLine(final int lineNo, final char[] data, int from, final int length) throws IOException, SyntaxException {
		final int						begin = from;
		final int[]						forNames = new int[2];
		final EnumMap<Option,Object>	result = new EnumMap<>(Option.class);
		
		if (CharUtils.compare(data,from,CAPTION_MARK)) {
			from += CAPTION_MARK.length - 1;	// -1 - to skip ',' in the next loop rounds
			
loop:		do {
				from = CharUtils.skipBlank(data,from+1,true);
				for (Option item : Option.values()) {
					if (CharUtils.compare(data,from,item.getKeyChars())) {
						if (data[from = CharUtils.skipBlank(data,from+item.getKeyChars().length,true)] == '=') {
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
				
				if (data[from = CharUtils.skipBlank(data,from,true)] == ':') {
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
			start = from;
			if (data[from] == '&') {
				final SyntaxNode<SyntaxNodeOperation,SyntaxNode<?,?>>	root = new SyntaxNode<>(lineNo,0,SyntaxNodeOperation.ROOT,0,null);
				FieldFormat	ff;
				Grouping	groupingType = null;
				
				from = CharUtils.skipBlank(data,from+1,true);
				
				if (desc.isAfter()) {
					for (Grouping item : Grouping.values()) {
						if (CharUtils.compare(data,from,item.getKeyChars())) {
							if (data[from = CharUtils.skipBlank(data,from+item.getKeyChars().length,true)] == '(') {
								from = buildExpression(lineNo,data,CharUtils.skipBlank(data,from+1,true),root);
								
								if (data[from = CharUtils.skipBlank(data,from,true)] == ')') {
									groupingType = item;
									from++;
									break;
								}
								else {
									throw new SyntaxException(lineNo,from-begin,"Missing ')'");
								}
							}
							else {
								throw new SyntaxException(lineNo,from-begin,"Missing '('");
							}
						}
					}
					if (groupingType == null) {
						from = buildExpression(lineNo,data,from,root);
					}
				}
				else {
					from = buildExpression(lineNo,data,from,root);
				}
				
				if (data[from = CharUtils.skipBlank(data,from,true)] == ':') {
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

	private static boolean partExists(final List<PartDescriptor> list, final String partName) {
		for (PartDescriptor item : list) {
			if (item.partName.equals(partName)) {
				return true;
			}
		}
		return false;
	}
	
	private static void collectFieldNames(final List<PartDescriptor> list, final Set<String> result) {
		for (PartDescriptor part : list) {
			if (part.fieldNames != null && part.fieldNames.length > 0) {
				result.addAll(Arrays.asList(part.fieldNames));
			}
			for (Object item : part.components) {
				if (item instanceof SyntaxNode) {
					collectFieldNames((SyntaxNode<SyntaxNodeOperation,SyntaxNode<?,?>>)item,result);
				}
				else if (item instanceof GroupingContent) {
					collectFieldNames(((GroupingContent)item).expression,result);
				}
			}
		}
	}
	
	private static void collectFieldNames(final SyntaxNode<SyntaxNodeOperation, SyntaxNode<?, ?>> node, final Set<String> result) {
		if (node.getType() == SyntaxNodeOperation.GET_FIELD) {
			result.add((String)node.cargo);
		}
		else if (node.cargo instanceof SyntaxNode) {
			collectFieldNames((SyntaxNode)node.cargo,result);
		}
		if (node.children != null) {
			for (SyntaxNode item : node.children) {
				collectFieldNames(item,result);
			}
		}
	}


	private interface DataSource {
		boolean next() throws IOException;
		Object getField(int index) throws IOException;
		Object getField(String name) throws IOException;
		int fieldIndex(String name) throws IOException;
	}
	
	private static class PrintManager {
		private final PartDescriptor	startPart;
		private final PartDescriptor	endPart;
		private final PartDescriptor	detailedPart;
		private final PartStack[]		partStack;
		private final int[]				fieldIndices;
		private final String[]			fieldNames;
		private final int[]				groupingIndices;
		private final String[]			groupingNames;
		private final Object[]			currentFieldValues;
		private final Object[]			newFieldValues;
		private boolean					firstDetailed = true;
		
		PrintManager(final Set<String> allFields, final Set<String> groupingFields, final PartDescriptor startPart, final PartDescriptor detailedPart, final PartDescriptor endPart, final PartStack[] partStack) {
			this.fieldNames = allFields.toArray(new String[allFields.size()]);
			this.fieldIndices = new int[this.fieldNames.length];
			this.currentFieldValues = new Object[this.fieldNames.length];
			this.newFieldValues = new Object[this.fieldNames.length];
			this.groupingNames = groupingFields.toArray(new String[groupingFields.size()]);
			this.groupingIndices = new int[this.groupingNames.length];
			this.startPart = startPart;
			this.detailedPart = detailedPart;
			this.endPart = endPart;
			this.partStack = partStack;
		}
		
		void startDoc(final Writer writer) throws IOException, SyntaxException {
			print(startPart,writer);
		}

		boolean detailed(final DataSource ds, final Writer writer) throws ContentException, IOException {
			if (ds.next()) {
				if (firstDetailed) {
					prepareIndices(ds);
				}
				fillNewValues(ds);
				
				final int	delta = compareGroupingValues();
				
				if (delta != 0) {
					if (!firstDetailed) {
						completeGrouping(delta);
						printAfter(delta,writer);
					}
					System.arraycopy(newFieldValues,0,currentFieldValues,0,currentFieldValues.length);
					prepareGrouping(delta);
					printBefore(delta,writer);
				}
				else {
					System.arraycopy(newFieldValues,0,currentFieldValues,0,currentFieldValues.length);
				}
				print(detailedPart,writer);
				collectGrouping();
				firstDetailed = false;
				return true;
			}
			else {
				completeGrouping(0xFFFFFFFF);
				printAfter(0xFFFFFFFF,writer);
				return false;
			}
		}
		
		private void prepareGrouping(int delta) {
			for (int index = 0; index < partStack.length; index++) {
				if ((delta & (1 << index)) != 0) {
					prepareGrouping(partStack[index].afterPart);
				}
			}
		}

		private void collectGrouping() throws ContentException {
			for (PartStack item : partStack) {
				collectGrouping(item.afterPart);
			}
		}

		private void completeGrouping(int delta) {
			for (int index = partStack.length - 1; index >= 0; index--) {
				if ((delta & (1 << index)) != 0) {
					completeGrouping(partStack[index].afterPart);
				}
			}
		}

		private void fillNewValues(final DataSource ds) throws IOException {
			for (int index = 0; index < fieldIndices.length; index++) {
				if (fieldIndices[index] < 0) {
					newFieldValues[index] = ds.getField(fieldNames[index]);
				}
				else {
					newFieldValues[index] = ds.getField(fieldIndices[index]);
				}
			}
		}

		private int compareGroupingValues() {
			for (int index = 0, mask = 0xFFFFFFFF; index < groupingIndices.length; index++, mask <<= 1) {
				if (!Objects.equals(currentFieldValues[groupingIndices[index]],newFieldValues[groupingIndices[index]])) {
					return mask;
				}
			}
			return 0;
		}
		
		private void prepareIndices(final DataSource ds) throws IOException {
			for (int index = 0; index < fieldNames.length; index++) {
				fieldIndices[index] = ds.fieldIndex(fieldNames[index]);
			}
			for (int index = 0; index < groupingNames.length; index++) {
				groupingIndices[index] = ds.fieldIndex(groupingNames[index]);
			}
			if (startPart != null) {
				startPart.preparePart(fieldNames,fieldIndices);
			}
			if (endPart != null) {
				endPart.preparePart(fieldNames,fieldIndices);
			}
			if (detailedPart != null) {
				detailedPart.preparePart(fieldNames,fieldIndices);
			}
			for (PartStack item : partStack) {
				if (item.beforePart != null) {
					item.beforePart.preparePart(fieldNames,fieldIndices);
				}
				if (item.afterPart != null) {
					item.afterPart.preparePart(fieldNames,fieldIndices);
				}
			}
		}

		void printBefore(final int delta, final Writer writer) throws IOException, SyntaxException {
			for (int index = 0; index < partStack.length; index++) {
				if ((delta & (1 << index)) != 0) {
					print(partStack[index].beforePart,writer);
				}
			}
		}
		
		void printAfter(final int delta, final Writer writer) throws IOException, SyntaxException {
			for (int index = partStack.length - 1; index >= 0; index--) {
				if ((delta & (1 << index)) != 0) {
					print(partStack[index].afterPart,writer);
				}
			}
		}
		
		void endDoc(final Writer writer) throws IOException, SyntaxException {
			print(endPart,writer);
		}
		
		void prepareGrouping(PartDescriptor desc) {
			if (desc != null) {
				desc.prepareGrouping();
			}
		}
		
		void collectGrouping(PartDescriptor desc) throws ContentException {
			if (desc != null) {
				desc.collectGrouping(currentFieldValues);
			}
		}

		void completeGrouping(PartDescriptor desc) {
			if (desc != null) {
				desc.completeGrouping();
			}
		}

		void print(PartDescriptor desc, final Writer writer) throws IOException, SyntaxException {
			if (desc != null) {
				desc.print(currentFieldValues,writer);
			}
		}
	}

	private static class GroupingContent {
		private final SyntaxNode<SyntaxNodeOperation,SyntaxNode<?,?>> expression;
		private final Grouping	grouping;
		
		private Object			value;
		private long			counter;
		
		GroupingContent(final SyntaxNode<SyntaxNodeOperation,SyntaxNode<?,?>> expression, final Grouping grouping) {
			this.expression = expression;
			this.grouping = grouping;
		}
		
		void prepare() {
			switch (grouping) {
				case Avg	:
					value = new BigDecimal(0);
					counter = 0;
					break;
				case Count	:
					counter = 0;
					break;
				case Max	:
					value = null;
					break;
				case Min	:
					value = null;
					break;
				case Sum	:
					value = new BigDecimal(0);
					break;
				default:
					throw new UnsupportedOperationException("Grouping type ["+grouping+"] is not supported yet"); 
			}
		}
		
		void collect(final Object newValue) throws ContentException {
			switch (grouping) {
				case Avg	:
					value = ((BigDecimal)value).add(SQLUtils.convert(BigDecimal.class,newValue));
					counter++;
					break;
				case Count	:
					counter++;
					break;
				case Max	:
					if (value == null) {
						value = newValue;
					}
					else if ((newValue instanceof Comparable) && ((Comparable)newValue).compareTo(value) > 0) {
						value = newValue;
					}
					break;
				case Min	:
					if (value == null) {
						value = newValue;
					}
					else if ((newValue instanceof Comparable) && ((Comparable)newValue).compareTo(value) < 0) {
						value = newValue;
					}
					break;
				case Sum	:
					value = ((BigDecimal)value).add(SQLUtils.convert(BigDecimal.class,newValue));
					break;
				default:
					throw new UnsupportedOperationException("Grouping type ["+grouping+"] is not supported yet"); 
			}
		}
	
		void complete() {
			switch (grouping) {
				case Avg	:
					if (counter == 0) {
						value = new BigDecimal(0);
					}
					else {
						value = ((BigDecimal)value).divide(new BigDecimal(counter));
					}
					break;
				case Count	:
					break;
				case Max	:
					break;
				case Min	:
					break;
				case Sum	:
					break;
				default:
					throw new UnsupportedOperationException("Grouping type ["+grouping+"] is not supported yet"); 
			}
		}
	}
	
	static class PartDescriptor {
		final String			partName;
		final boolean			before;
		final boolean			after;
		final String[]			fieldNames;
		final List<Object>		components = new ArrayList<>();
		final List<GroupingContent>	groupingContent = new ArrayList<>();
		
		PartDescriptor(final String partName) {
			this.partName = partName;
			this.before = false;
			this.after = false;
			this.fieldNames = new String[0];
		}

		PartDescriptor(final String partName, final boolean before, final boolean after, final String... fieldNames) {
			this.partName = partName;
			this.before = before;
			this.after = after;
			this.fieldNames = fieldNames;
		}

		public boolean isBefore() {
			return before;
		}

		public boolean isAfter() {
			return after;
		}
		
		public void appendText(final char[] content, final int from, final int to) {
			components.add(Arrays.copyOfRange(content, from, to));
		}
		
		public void appendOrdinalExpression(final SyntaxNode<SyntaxNodeOperation,SyntaxNode<?,?>> expression, final FieldFormat format) {
			components.add(expression);
			components.add(format);
		}
		
		public void appendGroupExpression(final Grouping grouping, final SyntaxNode<SyntaxNodeOperation,SyntaxNode<?,?>> expression, final FieldFormat format) {
			final GroupingContent	content = new GroupingContent(expression,grouping); 
			
			components.add(content);
			components.add(format);
			groupingContent.add(content);
		}

		public void preparePart(final String[] fieldNames, final int[] fieldIndices) {
			for (Object item : components) {
				if (item instanceof SyntaxNode) {
					preparePart((SyntaxNode)item,fieldNames,fieldIndices);
				}
				else if (item instanceof GroupingContent) {
					preparePart(((GroupingContent)item).expression,fieldNames,fieldIndices);
				}
			}
		}
		
		public void prepareGrouping() {
			for(GroupingContent item : groupingContent) {
				item.prepare();
			}
		}

		public void collectGrouping(final Object[] content) throws ContentException {
			for(GroupingContent item : groupingContent) {
				item.collect(extractValue(content,item.expression));
			}
		}

		public void completeGrouping() {
			for(GroupingContent item : groupingContent) {
				item.complete();
			}
		}
		
		public void print(final Object[] content, final Writer writer) throws IOException, SyntaxException {
			Object	value = null;
			
			for (Object item : components) {
				if (item instanceof char[]) {
					writer.write((char[])item);
				}
				else if (item instanceof SyntaxNode) {
					value = extractValue(content,(SyntaxNode)item);
				}
				else if (item instanceof GroupingContent) {
					value = ((GroupingContent)item).value;
				}
				else if (item instanceof FieldFormat) {
					writer.write(((FieldFormat)item).print(value,PrintMode.SINGLE_TEXT));
				}
			}
		}
		
		public Object extractValue(final Object[] content, final SyntaxNode<SyntaxNodeOperation,SyntaxNode<?,?>> node) {
			switch (node.getType()) {
				case GET_FIELD	:
					return node.value < 0 ? null : content[(int) node.value]; 
				default:
					throw new UnsupportedOperationException("Node type ["+node.getType()+"] is not supportedf yet");
			}
		}

		private void preparePart(final SyntaxNode<SyntaxNodeOperation,SyntaxNode<?,?>> node, final String[] fieldNames, final int[] fieldIndices) {
			if (node.getType() == SyntaxNodeOperation.GET_FIELD) {
				node.value = -1;
				for (int index = 0, maxIndex = fieldNames.length; index < maxIndex; index++) {
					if (fieldNames[index].equals(node.cargo)) {
						node.value = index;
						break;
					}
				}
			}
			if (node.cargo instanceof SyntaxNode) {
				preparePart((SyntaxNode)node.cargo,fieldNames, fieldIndices);
			}
			if (node.children != null && node.children.length > 0) {
				for (SyntaxNode item : node.children) {
					preparePart(item,fieldNames, fieldIndices);
				}
			}
		}

	}
	
	private static class PartStack {
		final String[]			fieldNames;
		final PartDescriptor	beforePart;
		final PartDescriptor	afterPart;
		
		PartStack(String[] fieldNames, PartDescriptor beforePart, PartDescriptor afterPart) {
			this.fieldNames = fieldNames;
			this.beforePart = beforePart;
			this.afterPart = afterPart;
		}
		
		public String toDiagnosticString() {
			return "fieldList="+Arrays.toString(fieldNames)+",beforePart="+(beforePart != null ? beforePart.partName : "null")+",afterPart="+(afterPart != null ? afterPart.partName : "null");
		}
	}

}
