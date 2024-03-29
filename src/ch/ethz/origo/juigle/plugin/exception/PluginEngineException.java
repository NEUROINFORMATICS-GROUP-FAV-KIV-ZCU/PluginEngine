package ch.ethz.origo.juigle.plugin.exception;

/**
 * PluginEngine exception. This exception is call when: plug-in can not be load,
 * not implemented main interface, problem with configuration XML file and a lot
 * of others exceptions.
 * 
 * @author Vaclav Souhrada (v.souhrada at gmail.com)
 * @version 0.1.0 (3/07/2010)
 * @since 0.1.0 (3/07/09)
 * @see Exception
 * 
 */
public class PluginEngineException extends Exception {

	/**
	 * Only for serialization
	 */
	private static final long serialVersionUID = 4495599664837030072L;

	/**
	 * Constructs a new ProjectWriterException exception with the specified cause
	 * and a detail message of <tt>(cause==null ? null : cause.toString())</tt>
	 * (which typically contains the class and detail message of <tt>cause</tt>).
	 * This constructor is useful for exceptions that are little more than
	 * wrappers for other throwables (for example,
	 * {@link java.security.PrivilegedActionException}).
	 * 
	 * @param cause
	 *          the cause (which is saved for later retrieval by the
	 *          {@link #getCause()} method). (A <tt>null</tt> value is permitted,
	 *          and indicates that the cause is nonexistent or unknown.)
	 */
	public PluginEngineException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new exception with the specified detail message. The cause is
	 * not initialized, and may subsequently be initialized by a call to
	 * {@link #initCause}.
	 * 
	 * @param message
	 *          the detail message. The detail message is saved for later
	 *          retrieval by the {@link #getMessage()} method.
	 */
	public PluginEngineException(String message) {
		super(message);
	}

	/**
	 * Constructs a new PluginEngineException exception with the specified detail
	 * message and cause.
	 * <p>
	 * Note that the detail message associated with <code>cause</code> is
	 * <i>not</i> automatically incorporated in this exception's detail message.
	 * 
	 * @param message
	 *          the detail message (which is saved for later retrieval by the
	 *          {@link #getMessage()} method).
	 * @param cause
	 *          the cause (which is saved for later retrieval by the
	 *          {@link #getCause()} method). (A <tt>null</tt> value is permitted,
	 *          and indicates that the cause is nonexistent or unknown.)
	 */
	public PluginEngineException(String message, Throwable cause) {
		super(message, cause);
	}

}
