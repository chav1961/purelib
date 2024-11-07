package chav1961.purelib.matrix;

import java.io.IOException;
import java.lang.ref.Cleaner.Cleanable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.cdb.SyntaxNode;
import chav1961.purelib.matrix.interfaces.Matrix;
import chav1961.purelib.matrix.interfaces.MatrixCalc;
import chav1961.purelib.streams.DataOutputAdapter;

public abstract class AbstractMatrix implements Matrix {
	private static final SyntaxTreeInterface<FunctionType>	FUNCTIONS = new AndOrTree<>();
	private static final char	EOF = '\0';
	
	static {
		for(FunctionType item : FunctionType.values()) {
			FUNCTIONS.placeName((CharSequence)item.getAbbr(), item);
		}
	}
	
	private final Type				type; 
	private final int				rows; 
	private final int				cols;
	private final Cleanable			cleanable = PureLibSettings.getCleaner().register(this, ()->lastCall());
	private boolean					transactionMode = false;

	protected AbstractMatrix(final Type type, final int rows, final int cols) {
		if (type == null) {
			throw new NullPointerException("Matrix type can't be null");
		}
		else if (rows <= 0) {
			throw new IllegalArgumentException("Number of rows ["+rows+"] must be greater than 0");
		}
		else if (cols <= 0) {
			throw new IllegalArgumentException("Number of columns ["+cols+"] must be greater than 0");
		}
		else {
			this.type = type;
			this.rows = rows;
			this.cols = cols;
		}
	}

	protected abstract void lastCall();
	protected abstract MatrixCalc buildMatrixCalc(Command... cmds) throws SyntaxException;
	
	@Override
	public void close() throws RuntimeException {
		cleanable.clean();
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public int numberOfRows() {
		return rows;
	}

	@Override
	public int numberOfColumns() {
		return cols;
	}

	@Override
	public Matrix apply(final Piece piece, final ApplyBit callback) {
		throw new UnsupportedOperationException("This method is not applicable for matrix type ["+getType()+"]");
	}

	@Override
	public Matrix apply(final Piece piece, final ApplyInt callback) {
		throw new UnsupportedOperationException("This method is not applicable for matrix type ["+getType()+"]");
	}

	@Override
	public Matrix apply(final Piece piece, final ApplyLong callback) {
		throw new UnsupportedOperationException("This method is not applicable for matrix type ["+getType()+"]");
	}

	@Override
	public Matrix apply(final Piece piece, final ApplyFloat callback) {
		throw new UnsupportedOperationException("This method is not applicable for matrix type ["+getType()+"]");
	}

	@Override
	public Matrix apply(final Piece piece, final ApplyDouble callback) {
		throw new UnsupportedOperationException("This method is not applicable for matrix type ["+getType()+"]");
	}

	@Override
	public Matrix apply2(final Piece piece, final ApplyFloat2 callback) {
		throw new UnsupportedOperationException("This method is not applicable for matrix type ["+getType()+"]");
	}

	@Override
	public Matrix apply2(final Piece piece, final ApplyDouble2 callback) {
		throw new UnsupportedOperationException("This method is not applicable for matrix type ["+getType()+"]");
	}

	@Override
	public Matrix done() {
		completeTransaction();
		return this;
	}

	@Override
	public boolean areAllAsyncCompleted() {
		return !transactionMode;
	}
	
	@Override
	public String toHumanReadableString() {
		final StringBuilder	sb = new StringBuilder();
		
		sb.append("=== Matrix: type=").append(getType()).append(", size=").append(numberOfRows()).append('x').append(numberOfColumns()).append(":\n");
		try {
			switch (getType()) {
				case BIT	:
					extractInts(new DataOutputAdapter() {
						long	count = 0;
						@Override
						public void writeInt(int v) throws IOException {
							sb.append(String.format("%1$1d ", v));
							if (count++ % numberOfColumns() == 0) {
								sb.append("\n");
							}
						}
					});
					break;
				case COMPLEX_DOUBLE	:
					extractDoubles(new DataOutputAdapter() {
						long	count = 0;
						double	real;
						@Override
						public void writeDouble(double v) throws IOException {
							if (count % 2 == 0) {
								real = v;
							}
							else {
								sb.append(AbstractMatrix.this.toString(real, v));
								if (count++ % numberOfColumns() == 0) {
									sb.append("\n");
								}
							}
						}
					});
					break;
				case COMPLEX_FLOAT	:
					extractDoubles(new DataOutputAdapter() {
						long	count = 0;
						float	real;
						@Override
						public void writeFloat(float v) throws IOException {
							if (count % 2 == 0) {
								real = v;
							}
							else {
								sb.append(AbstractMatrix.this.toString(real, v));
								if (count++ % numberOfColumns() == 0) {
									sb.append("\n");
								}
							}
						}
					});
					break;
				case REAL_DOUBLE	:
					extractDoubles(new DataOutputAdapter() {
						long	count = 0;
						@Override
						public void writeDouble(double v) throws IOException {
							sb.append(String.format("%1$20.15E ", v));
							if (count++ % numberOfColumns() == 0) {
								sb.append("\n");
							}
						}
					});
					break;
				case REAL_FLOAT		:
					extractFloats(new DataOutputAdapter() {
						long	count = 0;
						@Override
						public void writeFloat(float v) throws IOException {
							sb.append(String.format("%1$10.6E ", v));
							if (count++ % numberOfColumns() == 0) {
								sb.append("\n");
							}
						}
					});
					break;
				case REAL_INT		:
					extractInts(new DataOutputAdapter() {
						long	count = 0;
						@Override
						public void writeInt(int v) throws IOException {
							sb.append(String.format("%1$10d ", v));
							if (count++ % numberOfColumns() == 0) {
								sb.append("\n");
							}
						}
					});
					break;
				case REAL_LONG		:
					extractLongs(new DataOutputAdapter() {
						long	count = 0;
						@Override
						public void writeLong(long v) throws IOException {
							sb.append(String.format("%1$20d ", v));
							if (count++ % numberOfColumns() == 0) {
								sb.append("\n");
							}
						}
					});
					break;
				default:
					break;
			}
		} catch (IOException e) {
			sb.append(e.getLocalizedMessage());
		}
		sb.append("\n=== End matrix\n");
		return sb.toString();
	}

	@Override
	public MatrixCalc prepare(final String expression) throws SyntaxException {
		if (Utils.checkEmptyOrNullString(expression)) {
			throw new IllegalArgumentException("Expession string can't be null or empty");
		}
		else {
			final char[]		content = CharUtils.terminateAndConvert2CharArray(expression, EOF);
			final List<Lexema>	lexemas = new ArrayList<>();
			final SyntaxNode<SyntaxNodeType, SyntaxNode<?,?>>	root = new SyntaxNode(0, 0, SyntaxNodeType.ROOT, 0, null);
			
			parseExpression(content, 0, lexemas);
			final Lexema[]		lexArray = lexemas.toArray(new Lexema[lexemas.size()]);
			final int			pos = buildTree(Priority.ADD, lexArray, 0, root);
			
			if (lexArray[pos].type != LexType.EOF) {
				throw new SyntaxException(0, lexArray[pos].pos, "Unparsed tail");
			}
			else {
				final List<Command>			commands = new ArrayList<>();
				final List<ArgumentType>	stack = new ArrayList<>();
				
				buildCommands(root, commands, stack);
				if (stack.size() != 1) {
					throw new SyntaxException(0, 0, "Illegal expression result - exactly one value must retain on the stack, but ["+stack.size()+"] found");
				}
				else {
					return buildMatrixCalc(commands.toArray(new Command[commands.size()]));
				}
			}
		}
	}

	@Override
	public Matrix calculate(final String expression, final Object... parameters) throws SyntaxException, CalculationException {
		if (Utils.checkEmptyOrNullString(expression)) {
			throw new IllegalArgumentException("Expression string can't be null or empty"); 
		}
		else if (parameters == null ||  Utils.checkArrayContent4Nulls(parameters) >= 0) {
			throw new IllegalArgumentException("Parameters list is null or contains nulls inside"); 
		}
		else {
			try (final MatrixCalc	calc = prepare(expression)) {
				return calc.execute(parameters);
			}
		}
	}
	
	protected Piece totalPiece() {
		return Piece.of(0, 0, rows, cols);
	}

	protected boolean isOverlaps(final Piece piece) {
		if (piece.getLeft() >= numberOfColumns()) {
			return true;
		}
		else if (piece.getTop() >= numberOfRows()) {
			return true;
		}
		else if (piece.getLeft() + piece.getWidth() > numberOfColumns()) {
			return true;
		}
		else if (piece.getTop() + piece.getHeight() > numberOfRows()) {
			return true;
		}
		else {
			return false;
		}
	}
	
	protected IllegalArgumentException overlapsError(final Piece piece) {
		if (piece.getLeft() >= numberOfColumns()) {
			return new IllegalArgumentException("Left piece location ["+piece.getLeft()+"] outside number of columns ["+numberOfColumns()+"]");
		}
		else if (piece.getTop() >= numberOfRows()) {
			return new IllegalArgumentException("Top piece location ["+piece.getTop()+"] outside number of rows ["+numberOfRows()+"]");
		}
		else if (piece.getLeft() + piece.getWidth() > numberOfColumns()) {
			return new IllegalArgumentException("Right piece location ["+(piece.getLeft()+piece.getWidth())+"] outside number of columns ["+numberOfColumns()+"]");
		}
		else if (piece.getTop() + piece.getHeight() > numberOfRows()) {
			return new IllegalArgumentException("Bottom piece location ["+(piece.getTop()+piece.getHeight())+"] outside number of rows ["+numberOfRows()+"]");
		}
		else {
			return null;
		}
	}
	
	protected void beginTransaction() {
		transactionMode = true;
	}

	protected void completeTransaction() {
		transactionMode = false;
	}
	
	protected void ensureTransactionCompleted() {
		if (!areAllAsyncCompleted()) {
			throw new IllegalStateException("Attempt to call this method until transaction completed. Call done() before.");
		}
	}
	
	protected String toString(final float real, final float image) {
		if (real == 0) {
			return String.format("%1$10.6E", image);
		}
		else if (image == 0) {
			return String.format("%1$10.6E", real);
		}
		else {
			return String.format("%1$10.6E%1$+10.6E", real, image);
		}
	}
	
	protected String toString(final double real, final double image) {
		if (real == 0) {
			return String.format("%1$20.15E", image);
		}
		else if (image == 0) {
			return String.format("%1$20.15E", real);
		}
		else {
			return String.format("%1$20.15E%1$+20.15E", real, image);
		}
	}

	protected static enum CommandTypes {
		LOAD_MATRIX,
		LOAD_VALUE,
		LOAD_ENUM,
		NEGATE,
		MATRIX_NEGATE,
		ADD,
		MATRIX_ADD,
		MATRIX_VALUE_ADD_LEFT,
		MATRIX_VALUE_ADD_RIGHT,
		SUBTRACT,
		MATRIX_SUBTRACT,
		MATRIX_VALUE_SUBTRACT_LEFT,
		MATRIX_VALUE_SUBTRACT_RIGHT,
		MULTIPLY,
		MATRIX_MUL,
		MATRIX_VALUE_MUL_LEFT,
		MATRIX_VALUE_MUL_RIGHT,
		DIVIDE,
		MATRIX_VALUE_DIV_LEFT,
		MATRIX_VALUE_DIV_RIGHT,
		HADAMARD_MUL,
		KRONEKER_MUL,
		INVERT,
		TRANSPOSE,
		DETERMINANT,
		TRACK,
		CAST,
		CALL_FUNCTION,
	}

	protected static enum FunctionType {
		ABS("abs", ArgumentType.VALUE, ArgumentType.VALUE),
		CONJ("conj", ArgumentType.SAMEAS, ArgumentType.ANY),
		ZERO("zero", ArgumentType.MATRIX, ArgumentType.VALUE, ArgumentType.VALUE),
		IDENTITY("identity", ArgumentType.MATRIX, ArgumentType.VALUE, ArgumentType.VALUE),
		TRAN("tran", ArgumentType.MATRIX, ArgumentType.MATRIX),
		INV("inv", ArgumentType.MATRIX, ArgumentType.MATRIX),
		DET("det", ArgumentType.VALUE, ArgumentType.MATRIX),
		TRACK("track", ArgumentType.VALUE, ArgumentType.MATRIX),
		SIN("sin", ArgumentType.SAMEAS, ArgumentType.ANY),
		COS("cos", ArgumentType.SAMEAS, ArgumentType.ANY),
		TAN("tan", ArgumentType.SAMEAS, ArgumentType.ANY),
		SQRT("sqrt", ArgumentType.SAMEAS, ArgumentType.ANY),
		EXP("exp", ArgumentType.SAMEAS, ArgumentType.ANY),
		LN("ln", ArgumentType.SAMEAS, ArgumentType.ANY),
		SH("sh", ArgumentType.SAMEAS, ArgumentType.ANY),
		CH("ch", ArgumentType.SAMEAS, ArgumentType.ANY),
		TH("th", ArgumentType.SAMEAS, ArgumentType.ANY),
		ARCSIN("arcsin", ArgumentType.SAMEAS, ArgumentType.ANY),
		ARCCOS("arccos", ArgumentType.SAMEAS, ArgumentType.ANY),
		ARCTAN("arctan", ArgumentType.SAMEAS, ArgumentType.ANY),
		ARCSH("arcsh", ArgumentType.SAMEAS, ArgumentType.ANY),
		ARCCH("arcch", ArgumentType.SAMEAS, ArgumentType.ANY),
		ARCTH("arcth", ArgumentType.SAMEAS, ArgumentType.ANY);
		
		private final String			abbr;
		private final ArgumentType[]	args;
		private final ArgumentType		result;
		
		private FunctionType(final String abbr, final ArgumentType result, final ArgumentType... args) {
			this.abbr = abbr;
			this.args = args;
			this.result = result;
		}
		
		public String getAbbr() {
			return abbr;
		}

		public ArgumentType[] getArgs() {
			return args;
		}
		
		public ArgumentType getResult() {
			return result;
		}
	}
	
	protected static class Command {
		public final CommandTypes	cmd;
		public final Object			cargo;

		public Command(final CommandTypes cmd) {
			this.cmd = cmd;
			this.cargo = null;
		}
		
		public Command(final CommandTypes cmd, final Object cargo) {
			this.cmd = cmd;
			this.cargo = cargo;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((cargo == null) ? 0 : cargo.hashCode());
			result = prime * result + ((cmd == null) ? 0 : cmd.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			Command other = (Command) obj;
			if (cargo == null) {
				if (other.cargo != null) return false;
			} else if (cargo.getClass().isArray()) {
				if (!other.cargo.getClass().isArray()) {
					return false;
				}
				else if (cargo.getClass().getComponentType() != other.cargo.getClass().getComponentType()) {
					return false;
				}
				else if (Array.getLength(cargo) != Array.getLength(other.cargo)) {
					return false;
				}
				else {
					for(int index = 0, maxIndex = Array.getLength(cargo); index < maxIndex; index++) {
						if (!Objects.equals(Array.get(cargo, index), Array.get(other.cargo, index))) {
							return false;
						}
					}
				}
			} else if (!cargo.equals(other.cargo)) return false;
			if (cmd != other.cmd) return false;
			return true;
		}

		@Override
		public String toString() {
			return "Command [cmd=" + cmd + ", cargo=" + cargo + "]";
		}
	}
	
	static int parseExpression(final char[] source, int from, final List<Lexema> list) throws SyntaxException {
		final int[]		forInt = new int[2];
		final double[]	forDouble = new double[1];
		
all:	for (;;) {
			while (source[from] <= ' ' && source[from] != '\0') {
				from++;
			}
			int	begin = from;
			
			switch (source[from]) {
				case EOF	:
					list.add(new Lexema(begin, LexType.EOF));
					break all;
				case '(' :
					int		temp = ++from;
					
					while (source[temp] <= ' ' && source[temp] != EOF) {
						temp++;
					}
					if (Character.isLetter(source[temp])) {
						temp = CharUtils.parseName(source, temp, forInt);
						while (source[temp] <= ' ' && source[temp] != EOF) {
							temp++;
						}
						if (source[temp] == ')') {
							temp++;
							final String	cast = new String(source, forInt[0], forInt[1] - forInt[0] + 1);
							
							for(Type type : Type.values()) {
								if (cast.equalsIgnoreCase(type.name())) {
									list.add(new Lexema(begin, LexType.CAST, type));
									from = temp;
									continue all;
								}
							}
						}
					}
					else {
						list.add(new Lexema(begin, LexType.OPEN));
					}
					break;
				case ')' :
					list.add(new Lexema(begin, LexType.CLOSE));
					from++;
					break;
				case '+' :
					list.add(new Lexema(begin, LexType.OPERATOR, '+'));
					from++;
					break;
				case '-' :
					list.add(new Lexema(begin, LexType.OPERATOR, '-'));
					from++;
					break;
				case '*' :
					list.add(new Lexema(begin, LexType.OPERATOR, '*'));
					from++;
					break;
				case '/' :
					list.add(new Lexema(begin, LexType.OPERATOR, '/'));
					from++;
					break;
				case '^' :
					list.add(new Lexema(begin, LexType.OPERATOR, '^'));
					from++;
					break;
				case ',' :
					list.add(new Lexema(begin, LexType.DIV));
					from++;
					break;
				case '%' :
					from = CharUtils.parseInt(source, from+1, forInt, false);
					list.add(new Lexema(begin, LexType.MATRIX_REF, forInt[0]));
					break;
				case '0' : case '1' : case '2' : case '3' : case '4' : case '5' : case '6' : case '7' : case '8' : case '9' :
					from = CharUtils.parseDouble(source, from, forDouble, false);
					while (source[from] <= ' ' && source[from] != EOF) {
						from++;
					}
					if (source[from] == 'i' || source[from] == 'I') {
						from++;
						list.add(new Lexema(begin, LexType.IMAGE_CONSTANT, forDouble[0]));
					}
					else {
						list.add(new Lexema(begin, LexType.REAL_CONSTANT, forDouble[0]));
					}
					break;
				default :
					if (Character.isLetter(source[from])) {
						from = CharUtils.parseName(source, from, forInt);
						final long	id = FUNCTIONS.seekNameI(source, forInt[0], forInt[1]+1);
						
						if (id < 0) {
							if (source[begin] == 'x' && forInt[0] == forInt[1]) {
								list.add(new Lexema(begin, LexType.OPERATOR, 'x'));
							}
							else {
								throw new SyntaxException(0, begin, "Unknown function name");
							}
						}
						else {
							list.add(new Lexema(begin, LexType.FUNCTION, FUNCTIONS.getCargo(id)));
						}
					}
					else {
						throw new SyntaxException(0, begin, "Unknown lexema");
					}
			}
		}
		return from;
	}

	static int buildTree(final Priority prty, final Lexema[] source, int from, final SyntaxNode<SyntaxNodeType, SyntaxNode<?,?>> node) throws SyntaxException {
		switch (prty) {
			case ADD	:
				from = buildTree(Priority.MUL, source, from, node);
				if (source[from].type == LexType.OPERATOR && (source[from].index == '+' || source[from].index == '-')) {
					final List<SyntaxNode<SyntaxNodeType, SyntaxNode<?,?>>>	temp = new ArrayList<>();
					final StringBuilder	sb = new StringBuilder();
					SyntaxNode<SyntaxNodeType, SyntaxNode<?,?>> clone = (SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>) node.clone(); 
					
					temp.add(clone);
					do {sb.append((char)source[from++].index);
						clone = (SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>) node.clone();
						from = buildTree(Priority.MUL, source, from, clone);
						temp.add(clone);
					} while (source[from].type == LexType.OPERATOR && (source[from].index == '+' || source[from].index == '-'));
					node.type = SyntaxNodeType.ADD;
					node.col = source[from].pos;
					node.cargo = sb.toString().toCharArray();
					node.children = temp.toArray(new SyntaxNode[temp.size()]);
				}
				break;
			case MUL	:
				from = buildTree(Priority.UNARY, source, from, node);
				if (source[from].type == LexType.OPERATOR && (source[from].index == '*' || source[from].index == '/' || source[from].index == 'x' || source[from].index == '^')) {
					final List<SyntaxNode<SyntaxNodeType, SyntaxNode<?,?>>>	temp = new ArrayList<>();
					final StringBuilder	sb = new StringBuilder();
					SyntaxNode<SyntaxNodeType, SyntaxNode<?,?>> clone = (SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>) node.clone(); 
					
					temp.add(clone);
					do {sb.append((char)source[from++].index);
						clone = (SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>) node.clone();
						from = buildTree(Priority.UNARY, source, from, clone);
						temp.add(clone);
					} while (source[from].type == LexType.OPERATOR && (source[from].index == '*' || source[from].index == '/' || source[from].index == 'x' || source[from].index == '^'));
					node.type = SyntaxNodeType.MUL;
					node.col = source[from].pos;
					node.cargo = sb.toString().toCharArray();
					node.children = temp.toArray(new SyntaxNode[temp.size()]);
				}
				break;
			case UNARY	:
				if (source[from].type == LexType.OPERATOR && source[from].index == '-') {
					SyntaxNode<SyntaxNodeType, SyntaxNode<?,?>> clone = (SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>) node.clone(); 

					node.col = source[from].pos;
					node.type = SyntaxNodeType.NEG;
					from = buildTree(Priority.CAST, source, from + 1, clone);
					node.cargo = clone;
				}
				else {
					from = buildTree(Priority.CAST, source, from, node);
				}
				break;
			case CAST	:
				if (source[from].type == LexType.CAST) {
					final SyntaxNode<SyntaxNodeType, SyntaxNode<?,?>> clone = (SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>) node.clone();
					final Type	type = (Type)source[from].cargo; 

					node.col = source[from].pos;
					node.type = SyntaxNodeType.CAST;
					from = buildTree(Priority.TERM, source, from + 1, clone);
					node.value = type.ordinal();
					node.cargo = clone;
				}
				else {
					from = buildTree(Priority.TERM, source, from, node);
				}
				break;
			case TERM	:
				switch (source[from].type) {
					case FUNCTION		:
						node.col = source[from].pos;
						node.type = SyntaxNodeType.FUNCTION;
						final FunctionType	type = (FunctionType)source[from++].cargo;
						final List<SyntaxNode<SyntaxNodeType, SyntaxNode<?,?>>>	temp = new ArrayList<>();
						SyntaxNode<SyntaxNodeType, SyntaxNode<?,?>> clone; 
						
						node.cargo = type;
						do {
							clone = (SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>) node.clone();
							from = buildTree(Priority.ADD, source, from + 1, clone);
							temp.add(clone);
						} while (source[from].type == LexType.DIV);
						if (source[from].type == LexType.CLOSE) {
							from++;
							if (temp.size() != type.args.length) {
								throw new SyntaxException(0, node.col, "Illegal number of arguments ("+type.args.length+" awaited but "+temp.size()+" detected)");
							}
							else {
								node.children = temp.toArray(new SyntaxNode[temp.size()]);
							}
						}
						else {
							throw new SyntaxException(0, node.col, "Missing ')'");
						}
						break;
					case MATRIX_REF		:
						node.col = source[from].pos;
						node.type = SyntaxNodeType.MATRIX;
						node.value = source[from++].index;
						break;
					case OPEN			:
						from = buildTree(Priority.ADD, source, from + 1, node);
						if (source[from].type == LexType.CLOSE) {
							from++;
						}
						else {
							throw new SyntaxException(0, node.col, "Missing ')'");
						}
						break;
					case IMAGE_CONSTANT	:
						node.col = source[from].pos;
						node.type = SyntaxNodeType.CONST;
						node.cargo = new double[] {0, source[from++].value};
						break;
					case REAL_CONSTANT	:
						node.col = source[from].pos;
						node.type = SyntaxNodeType.CONST;
						node.cargo = new double[] {source[from++].value, 0};
						break;
					default :
						throw new SyntaxException(0, node.col, "Operand is missing");
				}
				break;
			default:
				throw new UnsupportedOperationException("Priority ["+prty+"] is not supported yet");
		}
		
		return from;
	}

	static void buildCommands(final SyntaxNode<SyntaxNodeType, SyntaxNode<?,?>>	node, final List<Command> commands, final List<ArgumentType> stack) throws SyntaxException {
		switch (node.getType()) {
			case ADD		:
				final char[]	addOper = (char[])node.cargo; 
				
				for(int index = 0, maxIndex = node.children.length; index < maxIndex; index++) {
					buildCommands((SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>) node.children[index], commands, stack);
					if (index > 0) {
						switch (addOper[index-1]) {
							case '+' :
								if (stack.size() >= 2) {
									if (stack.get(0) == ArgumentType.MATRIX && stack.get(1) == ArgumentType.MATRIX) {
										commands.add(new Command(CommandTypes.MATRIX_ADD));
										stack.remove(0);
										stack.remove(0);
										stack.add(0, ArgumentType.MATRIX);
									}
									else if (stack.get(0) == ArgumentType.MATRIX && stack.get(1) == ArgumentType.VALUE) {
										commands.add(new Command(CommandTypes.MATRIX_VALUE_ADD_LEFT));
										stack.remove(0);
										stack.remove(0);
										stack.add(0, ArgumentType.MATRIX);
									}
									else if (stack.get(0) == ArgumentType.VALUE && stack.get(1) == ArgumentType.MATRIX) {
										commands.add(new Command(CommandTypes.MATRIX_VALUE_ADD_RIGHT));
										stack.remove(0);
										stack.remove(0);
										stack.add(0, ArgumentType.MATRIX);
									}
									else {
										commands.add(new Command(CommandTypes.ADD));
										stack.remove(0);
										stack.remove(0);
										stack.add(0, ArgumentType.VALUE);
									}
								}
								else {
									throw new SyntaxException(0, node.col, "Illegal stack content, must contain at least 2 operands");
								}
								break;
							case '-' :
								if (stack.size() >= 2) {
									if (stack.get(0) == ArgumentType.MATRIX && stack.get(1) == ArgumentType.MATRIX) {
										commands.add(new Command(CommandTypes.MATRIX_SUBTRACT));
										stack.remove(0);
										stack.remove(0);
										stack.add(0, ArgumentType.MATRIX);
									}
									else if (stack.get(0) == ArgumentType.MATRIX && stack.get(1) == ArgumentType.VALUE) {
										commands.add(new Command(CommandTypes.MATRIX_VALUE_SUBTRACT_LEFT));
										stack.remove(0);
										stack.remove(0);
										stack.add(0, ArgumentType.MATRIX);
									}
									else if (stack.get(0) == ArgumentType.VALUE && stack.get(1) == ArgumentType.MATRIX) {
										commands.add(new Command(CommandTypes.MATRIX_VALUE_SUBTRACT_RIGHT));
										stack.remove(0);
										stack.remove(0);
										stack.add(0, ArgumentType.MATRIX);
									}
									else {
										commands.add(new Command(CommandTypes.SUBTRACT));
										stack.remove(0);
										stack.remove(0);
										stack.add(0, ArgumentType.VALUE);
									}
								}
								else {
									throw new SyntaxException(0, node.col, "Illegal stack content, must contain at least 2 operands");
								}
								break;
							default :
								throw new UnsupportedOperationException("Unknown addition opcode ["+addOper[index-1]+"]");
						}
					}
				}
				break;
			case MUL		:
				final char[]	mulOper = (char[])node.cargo; 
				
				for(int index = 0, maxIndex = node.children.length; index < maxIndex; index++) {
					buildCommands((SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>) node.children[index], commands, stack);
					if (index > 0) {
						switch (mulOper[index-1]) {
							case '*' :
								if (stack.size() >= 2) {
									if (stack.get(0) == ArgumentType.MATRIX && stack.get(1) == ArgumentType.MATRIX) {
										commands.add(new Command(CommandTypes.MATRIX_MUL));
										stack.remove(0);
										stack.remove(0);
										stack.add(0, ArgumentType.MATRIX);
									}
									else if (stack.get(0) == ArgumentType.MATRIX && stack.get(1) == ArgumentType.VALUE) {
										commands.add(new Command(CommandTypes.MATRIX_VALUE_MUL_LEFT));
										stack.remove(0);
										stack.remove(0);
										stack.add(0, ArgumentType.MATRIX);
									}
									else if (stack.get(0) == ArgumentType.VALUE && stack.get(1) == ArgumentType.MATRIX) {
										commands.add(new Command(CommandTypes.MATRIX_VALUE_MUL_RIGHT));
										stack.remove(0);
										stack.remove(0);
										stack.add(0, ArgumentType.MATRIX);
									}
									else {
										commands.add(new Command(CommandTypes.MULTIPLY));
										stack.remove(0);
										stack.remove(0);
										stack.add(0, ArgumentType.VALUE);
									}
								}
								else {
									throw new SyntaxException(0, node.col, "Illegal stack content, must contain at least 2 operands");
								}
								break;
							case '/' :
								if (stack.size() >= 2) {
									if (stack.get(0) == ArgumentType.MATRIX && stack.get(1) == ArgumentType.VALUE) {
										commands.add(new Command(CommandTypes.MATRIX_VALUE_DIV_LEFT));
										stack.remove(0);
										stack.remove(0);
										stack.add(0, ArgumentType.MATRIX);
									}
									else if (stack.get(0) == ArgumentType.VALUE && stack.get(1) == ArgumentType.MATRIX) {
										commands.add(new Command(CommandTypes.MATRIX_VALUE_DIV_RIGHT));
										stack.remove(0);
										stack.remove(0);
										stack.add(0, ArgumentType.MATRIX);
									}
									else if (stack.get(0) == ArgumentType.VALUE && stack.get(1) == ArgumentType.VALUE) {
										commands.add(new Command(CommandTypes.DIVIDE));
										stack.remove(0);
										stack.remove(0);
										stack.add(0, ArgumentType.VALUE);
									}
									else {
										throw new SyntaxException(0, node.col, "Illegal stack content, both matrices can't be divided");
									}
								}
								else {
									throw new SyntaxException(0, node.col, "Illegal stack content, must contain at least 2 operands");
								}
								break;
							case '^' :
								if (stack.size() >= 2 && stack.get(0) == ArgumentType.MATRIX && stack.get(1) == ArgumentType.MATRIX) {
									commands.add(new Command(CommandTypes.KRONEKER_MUL));
									stack.remove(0);
									stack.remove(0);
									stack.add(0, ArgumentType.MATRIX);
								}
								else {
									throw new SyntaxException(0, node.col, "Illegal stack content, left and right operands must be matrices");
								}
								break;
							case 'x' :
								if (stack.size() >= 2 && stack.get(0) == ArgumentType.MATRIX && stack.get(1) == ArgumentType.MATRIX) {
									commands.add(new Command(CommandTypes.HADAMARD_MUL));
									stack.remove(0);
									stack.remove(0);
									stack.add(0, ArgumentType.MATRIX);
								}
								else {
									throw new SyntaxException(0, node.col, "Illegal stack content, left and right operands must be matrices");
								}
								break;
							default :
								throw new UnsupportedOperationException("Unknown multiplication opcode ["+mulOper[index-1]+"]");
						}
					}
				}
				break;
 			case CAST		:
				buildCommands((SyntaxNode<SyntaxNodeType, SyntaxNode<?,?>>)node.cargo, commands, stack);
				commands.add(new Command(CommandTypes.CAST, Type.byOrdinal((int)node.value)));
				break;
			case CONST		:
				commands.add(new Command(CommandTypes.LOAD_VALUE, node.cargo));
				stack.add(0, ArgumentType.VALUE);
				break;
			case FUNCTION	:
				for(SyntaxNode item : node.children) {
					buildCommands(item, commands, stack);
				}
				final FunctionType	ft = (FunctionType)node.cargo;
				
				commands.add(new Command(CommandTypes.CALL_FUNCTION, ft));
				if (stack.size() >= ft.args.length) {
					final int		delta = stack.size() - ft.args.length;
					ArgumentType	temp = null;
					
					for(int index = 0; index < ft.args.length; index++) {
						if (!(stack.get(delta + index) == ft.args[index] || ft.args[index] == ArgumentType.ANY)) {
							throw new SyntaxException(0, node.col, "Illegal stack content, funciton argument #["+index+"] has wrong type");
						}
						else if (ft.args[index] == ArgumentType.ANY) {
							temp = stack.get(delta + index);
						}
					}
					for(int index = 0; index < ft.args.length; index++) {
						stack.remove(0);
					}
					stack.add(0, ft.result == ArgumentType.SAMEAS ? temp : ft.result);
				}
				else {
					throw new SyntaxException(0, node.col, "Illegal stack content, too few arguments to call this function");
				}
				break;
			case MATRIX		:
				commands.add(new Command(CommandTypes.LOAD_MATRIX, Integer.valueOf((int)node.value)));
				stack.add(0, ArgumentType.MATRIX);
				break;
			case NEG		:
				buildCommands((SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>) node.cargo, commands, stack);
				if (stack.size() >= 1 && stack.get(0) == ArgumentType.MATRIX) {
					commands.add(new Command(CommandTypes.MATRIX_NEGATE));
				}
				else if (stack.size() >= 1 && stack.get(0) == ArgumentType.VALUE) {
					commands.add(new Command(CommandTypes.NEGATE));
				}
				else {
					throw new SyntaxException(0, node.col, "Illegal stack content, at least one operand required");
				}
				break;
			case ROOT		:
				throw new IllegalArgumentException("Illegal ROOT node!");
			default :
				throw new UnsupportedOperationException("Node type ["+node.getType()+"] is not supported yet");
		}
	}

	
	private static enum LexType {
		REAL_CONSTANT,
		IMAGE_CONSTANT,
		OPEN,
		CLOSE,
		MATRIX_REF,
		FUNCTION,
		CAST,
		OPERATOR,
		DIV,
		EOF
	}
	
	private static enum ArgumentType {
		VALUE,
		MATRIX,
		ANY,
		SAMEAS;
	}
	
	private static class Lexema {
		private final int		pos;
		private final LexType	type;
		private final double	value;
		private final int		index;
		private final Object	cargo;

		private Lexema(final int pos, final LexType type) {
			this(pos, type, 0, -1, null);
		}

		private Lexema(final int pos, final LexType type, final double value) {
			this(pos, type, value, -1, null);
		}
		
		private Lexema(final int pos, final LexType type, final int operator) {
			this(pos, type, 0, operator, null);
		}

		private Lexema(final int pos, final LexType type, final Object cargo) {
			this(pos, type, 0, -1, cargo);
		}
		
		private Lexema(final int pos, final LexType type, final double value, final int index, final Object cargo) {
			this.pos = pos;
			this.type = type;
			this.value = value;
			this.index = index;
			this.cargo = cargo;
		}

		@Override
		public String toString() {
			return "Lexema [pos=" + pos + ", type=" + type + ", value=" + value + ", index=" + index + ", cargo=" + cargo + "]";
		}
	}
	
	private static enum Priority {
		ADD,
		MUL,
		UNARY,
		CAST,
		TERM
	}
	
	private static enum SyntaxNodeType {
		ROOT,
		ADD,
		MUL,
		NEG,
		CAST,
		CONST,
		MATRIX,
		FUNCTION,
	}
}
