package chav1961.purelib.basic.interfaces;

/**
 * <p>This interface describes extended {@linkplain Appendable} interface.</p> 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.7
 */
public interface RichAppendable extends Appendable {
    RichAppendable append(CharSequence csq);
    RichAppendable append(CharSequence csq, int start, int end);
    RichAppendable append(char c);
    RichAppendable append(char[] csq);
    RichAppendable append(char[] csq, int start, int end);
    RichAppendable append(boolean v);
    RichAppendable append(int v);
    RichAppendable append(long v);
    RichAppendable append(float v);
    RichAppendable append(double v);
}
