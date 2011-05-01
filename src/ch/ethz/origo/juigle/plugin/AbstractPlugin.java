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

import java.io.File;

/**
 * Abstract class for plugins.
 * 
 * @author Vaclav Souhrada (v.souhrada at gmail.com)
 * @version 1.0.0 (5/01/2011)
 * @since 0.1.0 (3/07/2010)
 * @see IPluggable
 * 
 */
public abstract class AbstractPlugin implements IPluggable {

	private String version;
	private int[] minimalPluginVersion;

	/**
	 * Return the directory in which the files of this plugin are stored
	 * 
	 * @return the directory in which the files of this plugin are stored
	 */
	protected File getDirectory() {
		return PluginEngine.getInstance().getDirectory(this);
	}

	@Override
	public void setMinimalAppVersion(int[] version) {
		minimalPluginVersion = version;
	}

	@Override
	public int[] getMinimalAppVersion() {
		return minimalPluginVersion;
	}

	@Override
	public String getMinimalAppVersionAsString() {
		return PluginUtils.getVersionOfPluginAsString(minimalPluginVersion);
	}

	@Override
	public void setPluginVersion(String version) {
		this.version = version;
	}

	@Override
	public String getPluginVersion() {
		return version;
	}

}
