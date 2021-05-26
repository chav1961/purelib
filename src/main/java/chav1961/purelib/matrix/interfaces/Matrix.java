package chav1961.purelib.matrix.interfaces;

public interface Matrix {
	Class<?> getContentType();
	int getDimensions();
	int getSize(int dimension);
	Matrix add(Matrix another);
	Matrix sub(Matrix another);
	Matrix mul(Matrix another);
	Matrix inv();
	Matrix transp();
	Matrix add(Number number);
	Matrix mul(Number number);
	double getEpsilon();
	Matrix setEpsilon(double epsilon);
}
