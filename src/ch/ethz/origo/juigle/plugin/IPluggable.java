package ch.ethz.origo.juigle.plugin;

import java.awt.image.BufferedImage;
import java.net.URI;

/**
 * 
 * @author Vaclav Souhrada (v.souhrada at gmail.com)
 * @version 0.1.1 (4/30/2010)
 * @since (3/07/2010)
 *
 */
public interface IPluggable {

	/**
	 * @return the name for this plugin, should not be null
	 */
	public String getPluginName();
	
	/**
	 * @return the version for this plugin
	 */
	public String getPluginVersion();
	
	/**
	 * @return the minimal version of the main application for which this plugin can work
	 */
	public int[] getMinimalAppVersion();
	
	/**
	 * @return the basic description for this plugin
	 */
	public String getPluginBasicDescription();
	
	/**
	 * @return the description for this plugin
	 */
	public String getPluginDescription();
	
	/**
	 * @return the icon for this plugin
	 */
	public BufferedImage getIcon();
	
	/**
	 * @return the place where the XML descriptor for this plugin can be found, should not be null
	 */
	public URI getURI();
	
	/**
	 * this method will be called when the main program start if this plugin is installed
	 * @param args arguments given by the main program
	 */
	public void init(Object... args);
	
	/**
	 * this method will be called when this plugin is deleted, so you can free ressources,
	 * or restore some settings etc ...
	 * (the files insides the plugin directory are automatically deleted, so you don't have to take care of this)
	 */
	public void destroy();
	
	/**
	 * @return true if has some options to configure by the user
	 */
	public boolean hasOptions();
	
	/**
	 * if hasOptions() return true, this method will be called when the user 
	 * want to edit the options from this plugin. 
	 */
	public void openOptions();
	
	/**
	 * Return author's name of algorithm
	 * 
	 * @return author's name of algorithm
	 */
	public String getAuthorName();
	
	/**
	 * Return name of algorithm category
	 * 
	 * @return name of algorithm category
	 */
	public String getCategory();

}
