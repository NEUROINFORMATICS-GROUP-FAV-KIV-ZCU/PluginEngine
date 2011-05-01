/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/*
 *  
 *    Copyright (C) 2009 - 2011 
 *    							University of West Bohemia, 
 *                  Department of Computer Science and Engineering, 
 *                  Pilsen, Czech Republic
 */
package ch.ethz.origo.juigle.plugin;

import java.awt.image.BufferedImage;
import java.net.URI;

/**
 * Interface which must be implemented by all plug-ins. Contains main methods
 * for plug-in setting and important methods for plugin loader.
 * 
 * @author Vaclav Souhrada (v.souhrada at gmail.com)
 * @version 2.0.0 (5/01/2011)
 * @since (3/07/2010)
 * 
 */
public interface IPluggable {

	/**
	 * @return the name for this plug in, should not be null
	 */
	public String getPluginName();

	/**
	 * @return the version for this plug in
	 */
	public String getPluginVersion();

	/**
	 * By this method you can set a version of the plug in
	 * @param version version of the plug-in
	 */
	public void setPluginVersion(String version);

	/**
	 * @return the minimal version of the main application for which this plug-in
	 *         can work
	 */
	public int[] getMinimalAppVersion();
	
	/**
	 * @return the minimal version of the main application for which this plug-in
	 *         can work
	 */
	public String getMinimalAppVersionAsString();

	/**
	 * By this method you can set a minimal version of application for which is
	 * plug-in compatible.
	 * 
	 * @param version minimal version of application for which is
	 * plug-in compatible
	 */
	public void setMinimalAppVersion(int[] version);

	/**
	 * @return the basic description for this plug-in
	 */
	public String getPluginBasicDescription();

	/**
	 * @return the description for this plug-in
	 */
	public String getPluginDescription();

	/**
	 * @return the icon for this plug-in
	 */
	public BufferedImage getIcon();

	/**
	 * @return the place where the XML descriptor for this plug-in can be found,
	 *         should not be null
	 */
	public URI getURI();

	/**
	 * this method will be called when the main program start if this plug-in is
	 * installed
	 * 
	 * @param args
	 *          arguments given by the main program
	 */
	public void init(Object... args);

	/**
	 * this method will be called when this plug-in is deleted, so you can free
	 * resources, or restore some settings etc ... (the files insides the plug-in
	 * directory are automatically deleted, so you don't have to take care of
	 * this)
	 */
	public void destroy();

	/**
	 * @return true if has some options to configure by the user
	 */
	public boolean hasOptions();

	/**
	 * if hasOptions() return true, this method will be called when the user want
	 * to edit the options from this plug-in.
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
	
	/**
	 * Set a category for plug-in
	 * @param category name for plug-in
	 */
	public void setCategory(String category);

}
