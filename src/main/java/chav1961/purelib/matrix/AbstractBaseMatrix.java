package chav1961.purelib.matrix;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.concurrent.Exchanger;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.matrix.interfaces.BaseMatrix;

public abstract class AbstractBaseMatrix<T extends BaseMatrix<?>> implements BaseMatrix<T> {
	
	private final AtomicBoolean	trans = new AtomicBoolean(false);
	private final AtomicBoolean	closed = new AtomicBoolean(false);
	private final ContentType	contentType;
	private final FormatType	formatType;
	private final StoreType		storeType;
	private final int			width;
	private final int			height;
	
	protected AbstractBaseMatrix(final ContentType contentType, final FormatType formatType, final StoreType storeType, final int width, final int height) {
		if (contentType == null) {
			throw new NullPointerException("Content type can't be null"); 
		}
		else if (formatType == null) {
			throw new NullPointerException("Format type can't be null"); 
		}
		else if (storeType == null) {
			throw new NullPointerException("Store type can't be null"); 
		}
		else if (width <= 0) {
			throw new IllegalArgumentException("Matrix width ["+width+"] must ge greater than 0");
		}
		else if (height <= 0) {
			throw new IllegalArgumentException("Matrix height ["+height+"] must ge greater than 0");
		}
		else {
			this.contentType = contentType;
			this.formatType = formatType;
			this.storeType = storeType;
			this.width = width;
			this.height = height;
		}
	}
	
	@Override
	public ContentType getContentType() {
		return contentType;
	}

	@Override
	public FormatType getFormatType() {
		return formatType;
	}

	@Override
	public StoreType getStoreType() {
		return storeType;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override 
	public void close() throws CalculationException {
		closed.set(true);
	}
	
	@Override public abstract Object clone() throws CloneNotSupportedException;

	@Override public abstract T transpose() throws CalculationException;
	@Override public abstract Operand det() throws CalculationException;
	@Override public abstract Operand track() throws CalculationException;
	
	protected abstract T downloadInternal(final Piece piece, final DataInput in) throws IOException;
	protected abstract T downloadInternal(final Piece piece, final T in) throws IOException;
	protected abstract T uploadInternal(final Piece piece, final DataOutput out) throws IOException;
	protected abstract T uploadInternal(final Piece piece, final T out) throws IOException;
	protected abstract T cropInternal(Piece piece) throws CalculationException;
	protected abstract <AC extends ApplyCallback> T applyInternal(Piece piece, AC callback) throws CalculationException;
	protected abstract BaseMatrix<?> castInternal(ContentType type) throws CalculationException;
	protected abstract BaseMatrix<?> castInternal(FormatType type) throws CalculationException;
	protected abstract BaseMatrix<?> castInternal(StoreType type) throws CalculationException;
	protected abstract T addInternal(T another) throws CalculationException;
	protected abstract T addInternal(Operand another) throws CalculationException;
	protected abstract T subtractInternal(T another) throws CalculationException;
	protected abstract T subtractInternal(Operand another) throws CalculationException;
	protected abstract T subtractRevInternal(T another) throws CalculationException;
	protected abstract T subtractRevInternal(Operand another) throws CalculationException;
	protected abstract T mulInternal(T another) throws CalculationException;
	protected abstract T mulRevInternal(T another) throws CalculationException;
	protected abstract T mulHadamardInternal(T another) throws CalculationException;
	protected abstract T mulKronekerInternal(T another) throws CalculationException;
	protected abstract T mulKronekerRevInternal(T another) throws CalculationException;
	protected abstract T mulInternal(Operand another) throws CalculationException;
	protected abstract T divInternal(Operand another) throws CalculationException;
	protected abstract T divRevInternal(Operand another) throws CalculationException;
	protected abstract T aggregateInternal(Piece piece, AggregateDirection dir, AggregateType type) throws CalculationException;
	protected abstract T invertInternal() throws CalculationException;
	
	@Override
	public T download(final Piece piece, final DataInput in) throws IOException {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (!isInside(piece)) {
			throw new IllegalArgumentException("Piece ["+piece+"] is crossing matrix bounds "+getMatrixBounds(this));
		}
		else if (in == null) {
			throw new NullPointerException("Data input can't be null");
		}
		else if (closed.get()) {
			throw new IllegalStateException("Attempt to call methos after calling close()");
		}
		else {
			return downloadInternal(piece, in);
		}
	}

	@Override
	public T download(final Piece piece, final T in) throws IOException {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (!isInside(piece)) {
			throw new IllegalArgumentException("Piece ["+piece+"] is crossing matrix bounds "+getMatrixBounds(this));
		}
		else if (in == null) {
			throw new NullPointerException("Source matrix can't be null");
		}
		else if (closed.get()) {
			throw new IllegalStateException("Attempt to call methos after calling close()");
		}
		else {
			return downloadInternal(piece, in);
		}
	}

	@Override
	public T upload(final Piece piece, final DataOutput out) throws IOException {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (!isInside(piece)) {
			throw new IllegalArgumentException("Piece ["+piece+"] is crossing matrix bounds "+getMatrixBounds(this));
		}
		else if (out == null) {
			throw new NullPointerException("Data output can't be null");
		}
		else if (closed.get()) {
			throw new IllegalStateException("Attempt to call methos after calling close()");
		}
		else {
			return uploadInternal(piece, out);
		}
	}

	@Override
	public T upload(final Piece piece, final T out) throws IOException {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (!isInside(piece)) {
			throw new IllegalArgumentException("Piece ["+piece+"] is crossing matrix bounds "+getMatrixBounds(this));
		}
		else if (out == null) {
			throw new NullPointerException("Target matrix can't be null");
		}
		else if (closed.get()) {
			throw new IllegalStateException("Attempt to call methos after calling close()");
		}
		else {
			return uploadInternal(piece, out);
		}
	}

	@Override
	public T crop(final Piece piece) throws CalculationException {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (!isInside(piece)) {
			throw new IllegalArgumentException("Piece ["+piece+"] is crossing matrix bounds "+getMatrixBounds(this));
		}
		else if (closed.get()) {
			throw new IllegalStateException("Attempt to call methos after calling close()");
		}
		else {
			return cropInternal(piece);
		}
	}

	@Override
	public T apply(Piece piece, ApplyCallback callback) throws CalculationException {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (!isInside(piece)) {
			throw new IllegalArgumentException("Piece ["+piece+"] is crossing matrix bounds "+getMatrixBounds(this));
		}
		else if (callback == null) {
			throw new NullPointerException("Data output can't be null");
		}
		else if (closed.get()) {
			throw new IllegalStateException("Attempt to call methos after calling close()");
		}
		else {
			switch (getContentType()) {
				case BIT			:
					if (callback instanceof BitApplyCallback) {
						return applyInternal(piece, (BitApplyCallback) callback);
					}
					else {
						throw wrongCallbackType(callback, BitApplyCallback.class);
					}
				case COMPLEX_DOUBLE	:
					if (callback instanceof ComplexDoubleApplyCallback) {
						return applyInternal(piece, (ComplexDoubleApplyCallback) callback);
					}
					else {
						throw wrongCallbackType(callback, ComplexDoubleApplyCallback.class);
					}
				case COMPLEX_FLOAT	:
					if (callback instanceof ComplexFloatApplyCallback) {
						return applyInternal(piece, (ComplexFloatApplyCallback) callback);
					}
					else {
						throw wrongCallbackType(callback, ComplexFloatApplyCallback.class);
					}
				case REAL_DOUBLE	:
					if (callback instanceof DoubleApplyCallback) {
						return applyInternal(piece, (DoubleApplyCallback) callback);
					}
					else {
						throw wrongCallbackType(callback, DoubleApplyCallback.class);
					}
				case REAL_FLOAT		:
					if (callback instanceof FloatApplyCallback) {
						return applyInternal(piece, (FloatApplyCallback) callback);
					}
					else {
						throw wrongCallbackType(callback, FloatApplyCallback.class);
					}
				case REAL_INT		:
					if (callback instanceof IntApplyCallback) {
						return applyInternal(piece, (IntApplyCallback) callback);
					}
					else {
						throw wrongCallbackType(callback, IntApplyCallback.class);
					}
				case REAL_LONG		:
					if (callback instanceof LongApplyCallback) {
						return applyInternal(piece, (LongApplyCallback) callback);
					}
					else {
						throw wrongCallbackType(callback, LongApplyCallback.class);
					}
				default:
					throw new UnsupportedOperationException("Matrix content type ["+getContentType()+"] is not suppirte yet");
			}
		}
	}

	@Override
	public BaseMatrix<?> cast(final ContentType type) throws CalculationException {
		if (type == null) {
			throw new NullPointerException("Cast content type can't be null");
		}
		else if (closed.get()) {
			throw new IllegalStateException("Attempt to call methos after calling close()");
		}
		else if (type == getContentType()) {
			try {
				return (BaseMatrix<?>)this.clone();
			} catch (CloneNotSupportedException e) {
				throw new CalculationException(e);
			}
		}
		else {
			return castInternal(type);
		}
	}

	@Override
	public BaseMatrix<?> cast(final FormatType type) throws CalculationException {
		if (type == null) {
			throw new NullPointerException("Cast format type can't be null");
		}
		else if (closed.get()) {
			throw new IllegalStateException("Attempt to call methos after calling close()");
		}
		else if (type == getFormatType()) {
			try {
				return (BaseMatrix<?>)this.clone();
			} catch (CloneNotSupportedException e) {
				throw new CalculationException(e);
			}
		}
		else {
			return castInternal(type);
		}
	}

	@Override
	public BaseMatrix<?> cast(final StoreType type) throws CalculationException {
		if (type == null) {
			throw new NullPointerException("Cast store type can't be null");
		}
		else if (closed.get()) {
			throw new IllegalStateException("Attempt to call methos after calling close()");
		}
		else if (type == getStoreType()) {
			try {
				return (BaseMatrix<?>)this.clone();
			} catch (CloneNotSupportedException e) {
				throw new CalculationException(e);
			}
		}
		else {
			return castInternal(type);
		}
	}

	@Override
	public T add(final T another) throws CalculationException {
		if (another == null) {
			throw new NullPointerException("Another matrix to add can't be null");
		}
		else if (!areSizeIdentical(another)) {
			throw wrongIdenticalSize(another);
		}
		else if (closed.get()) {
			throw new IllegalStateException("Attempt to call methos after calling close()");
		}
		else {
			return addInternal(another);
		}
	}


	@Override
	public T add(final Operand another) throws CalculationException {
		if (another == null) {
			throw new NullPointerException("Operand to add can't be null");
		}
		else if (closed.get()) {
			throw new IllegalStateException("Attempt to call methos after calling close()");
		}
		else {
			return addInternal(another);
		}
	}

	@Override
	public T subtract(final T another) throws CalculationException {
		if (another == null) {
			throw new NullPointerException("Another matrix to subtract can't be null");
		}
		else if (!areSizeIdentical(another)) {
			throw wrongIdenticalSize(another);
		}
		else if (closed.get()) {
			throw new IllegalStateException("Attempt to call methos after calling close()");
		}
		else {
			return subtractInternal(another);
		}
	}

	@Override
	public T subtract(final Operand another) throws CalculationException {
		if (another == null) {
			throw new NullPointerException("Operand to subtract can't be null");
		}
		else if (closed.get()) {
			throw new IllegalStateException("Attempt to call methos after calling close()");
		}
		else {
			return subtractInternal(another);
		}
	}

	@Override
	public T subtractRev(final T another) throws CalculationException {
		if (another == null) {
			throw new NullPointerException("Another matrix to subtract can't be null");
		}
		else if (!areSizeIdentical(another)) {
			throw wrongIdenticalSize(another);
		}
		else if (closed.get()) {
			throw new IllegalStateException("Attempt to call methos after calling close()");
		}
		else {
			return subtractRevInternal(another);
		}
	}

	@Override
	public T subtractRev(final Operand another) throws CalculationException {
		if (another == null) {
			throw new NullPointerException("Operand to subtract can't be null");
		}
		else if (closed.get()) {
			throw new IllegalStateException("Attempt to call methos after calling close()");
		}
		else {
			return subtractRevInternal(another);
		}
	}

	@Override
	public T mul(final T another) throws CalculationException {
		if (another == null) {
			throw new NullPointerException("Another matrix to subtract can't be null");
		}
		else if (another.getHeight() != getWidth()) {
			throw new IllegalArgumentException("Incompatible matrices: another matrix height ["+another.getHeight()+"] differ with current matrix width ["+getWidth()+"]");
		}
		else if (closed.get()) {
			throw new IllegalStateException("Attempt to call methos after calling close()");
		}
		else {
			return mulInternal(another);
		}
	}

	@Override
	public T mulRev(final T another) throws CalculationException {
		if (another == null) {
			throw new NullPointerException("Another matrix to subtract can't be null");
		}
		else if (another.getWidth() != getHeight()) {
			throw new IllegalArgumentException("Incompatible matrices: another matrix width ["+another.getWidth()+"] differ with current matrix height ["+getHeight()+"]");
		}
		else if (closed.get()) {
			throw new IllegalStateException("Attempt to call methos after calling close()");
		}
		else {
			return mulRevInternal(another);
		}
	}

	@Override
	public T mulHadamard(T another) throws CalculationException {
		if (another == null) {
			throw new NullPointerException("Another matrix to multiply can't be null");
		}
		else if (!areSizeIdentical(another)) {
			throw wrongIdenticalSize(another);
		}
		else if (closed.get()) {
			throw new IllegalStateException("Attempt to call methos after calling close()");
		}
		else {
			return mulHadamardInternal(another);
		}
	}

	@Override
	public T mulKroneker(final T another) throws CalculationException {
		if (another == null) {
			throw new NullPointerException("Matrix to multiply can't be null");
		}
		else if (closed.get()) {
			throw new IllegalStateException("Attempt to call methos after calling close()");
		}
		else {
			return mulKroneker(another);
		}
	}

	@Override
	public T mulKronekerRev(final T another) throws CalculationException {
		if (another == null) {
			throw new NullPointerException("Matrix to multiply can't be null");
		}
		else if (closed.get()) {
			throw new IllegalStateException("Attempt to call methos after calling close()");
		}
		else {
			return mulKronekerRev(another);
		}
	}

	@Override
	public T mul(final Operand another) throws CalculationException {
		if (another == null) {
			throw new NullPointerException("Operand to nultiply can't be null");
		}
		else if (closed.get()) {
			throw new IllegalStateException("Attempt to call methos after calling close()");
		}
		else {
			return mulInternal(another);
		}
	}

	@Override
	public T div(final Operand another) throws CalculationException {
		if (another == null) {
			throw new NullPointerException("Operand to divide can't be null");
		}
		else if (another.getComplexDouble()[0]+another.getComplexDouble()[1] == 0) {
			throw new NullPointerException("Attempt to divide by zero ["+another+"]");
		}
		else if (closed.get()) {
			throw new IllegalStateException("Attempt to call methos after calling close()");
		}
		else {
			return divInternal(another);
		}
	}

	@Override
	public T divRev(final Operand another) throws CalculationException {
		if (another == null) {
			throw new NullPointerException("Operand to divide can't be null");
		}
		else if (another.getComplexDouble()[0]+another.getComplexDouble()[1] == 0) {
			throw new NullPointerException("Attempt to divide by zero ["+another+"]");
		}
		else if (closed.get()) {
			throw new IllegalStateException("Attempt to call methos after calling close()");
		}
		else {
			return divRevInternal(another);
		}
	}

	@Override 
	public T invert() throws CalculationException {
		if (getWidth() != getHeight()) {
			throw new IllegalStateException("Current matrix is not a square matrix: "+getMatrixBounds(this)+", inversion is not applicable for it");
		}
		else if (closed.get()) {
			throw new IllegalStateException("Attempt to call methos after calling close()");
		}
		else {
			return invertInternal();
		}
	}
	
	@Override
	public T aggregate(final Piece piece, final AggregateDirection dir, final AggregateType type) throws CalculationException {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (dir == null) {
			throw new NullPointerException("Aggregate direction can't be null");
		}
		else if (type == null) {
			throw new NullPointerException("Aggregate type can't be null");
		}
		else if (closed.get()) {
			throw new IllegalStateException("Attempt to call methos after calling close()");
		}
		else {
			return aggregateInternal(piece, dir, type);
		}
	}

	protected void beginTransaction() {
		if (closed.get()) {
			throw new IllegalStateException("Attempt to call methos after calling close()");
		}
		trans.set(true);
	}

	protected boolean checkAndBeginTransaction() {
		if (closed.get()) {
			throw new IllegalStateException("Attempt to call methos after calling close()");
		}
		return trans.getAndSet(true);
	}
	
	protected boolean inTransaction() {
		return trans.get();
	}
	
	protected void commit() {
		if (closed.get()) {
			throw new IllegalStateException("Attempt to call methos after calling close()");
		}
		trans.set(false);
	}
	
	protected final boolean isInside(final Piece piece) {
		if (piece.getX() < 0 || piece.getX() >= getWidth()) {
			return false;
		}
		else if (piece.getX() + piece.getWidth() < 0 || piece.getX() + piece.getWidth() > getWidth()) {
			return false;
		}
		else if (piece.getY() < 0 || piece.getY() >= getHeight()) {
			return false;
		}
		else if (piece.getY() + piece.getHeight() < 0 || piece.getY() + piece.getHeight() > getHeight()) {
			return false;
		}
		else {
			return true;
		}
	}

	protected final String getMatrixBounds(final BaseMatrix<?> matrix) {
		return "["+matrix.getWidth()+'x'+matrix.getHeight()+']';
	}

	private boolean areSizeIdentical(final T another) {
		return getWidth() == another.getWidth() && getHeight() == another.getHeight();
	}
	
	private IllegalArgumentException wrongCallbackType(final ApplyCallback callback, final Class<? extends ApplyCallback> awaited) {
		return new IllegalArgumentException("Matrix content type ["+getContentType()+"] requires to use ["+awaited.getName()+"] callback here, but you use ["+callback.getClass().getName()+"] instead");
	}

	private IllegalArgumentException wrongIdenticalSize(final T another) {
		return new IllegalArgumentException("Another matrix size "+getMatrixBounds(another)+" differ from current "+getMatrixBounds(this));
	}

}
