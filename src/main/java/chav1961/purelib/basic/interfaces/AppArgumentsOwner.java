package chav1961.purelib.basic.interfaces;

import chav1961.purelib.basic.ArgParser;

/**
 * <p>This interface describes any class that keeps application argument/ parsed in ZZZ.main(String[] args) method</p>
 * @since 0.0.8
 * @author Alexander Chernomyrdin aka chav1961
 */
@FunctionalInterface
public interface AppArgumentsOwner {
	ArgParser getAppArguments();
}
