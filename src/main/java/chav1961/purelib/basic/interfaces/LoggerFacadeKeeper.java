package chav1961.purelib.basic.interfaces;

/**
 * <p>This interface is used for logger keepers. Any class what has logger inside, can publish it by this interface</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.5
 */
public interface LoggerFacadeKeeper {
	/**
	 * <p>Get logger</p>
	 * @return logger facade instance. Can't be null
	 */
	LoggerFacade getLogger();
}
