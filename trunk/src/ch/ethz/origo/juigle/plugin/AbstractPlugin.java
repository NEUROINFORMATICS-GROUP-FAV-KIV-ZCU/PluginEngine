package ch.ethz.origo.juigle.plugin;

import java.io.File;

/**
 * 
 * @author Vaclav Souhrada (v.souhrada at gmail.com)
 * @version 0.1.0 (3/07/2010)
 * @since (3/07/2010)
 * @see Pluggable
 *
 */
public abstract class AbstractPlugin implements Pluggable {
	
	/**
	 * @return the directory in which the files of this plugin are storred
	 */
	protected File getDirectory() {
		return PluginEngine.getInstance().getDirectory(this);
	}

}
