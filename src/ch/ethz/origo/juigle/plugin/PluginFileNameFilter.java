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
 *    Copyright (C) 2009 - 2010 
 *    							University of West Bohemia, 
 *                  Department of Computer Science and Engineering, 
 *                  Pilsen, Czech Republic
 */
package ch.ethz.origo.juigle.plugin;

import java.io.File;
import java.io.FilenameFilter;


/**
 * File filter by name. This is for filtering unsupported data formats.
 * 
 * @author Vaclav Souhrada (v.souhrada at gmail.com)
 * @version 0.1.0.0 (10/20/2010)
 * @since 2.0.0 (10/20/2010)
 * @see FilenameFilter
 * 
 */
public class PluginFileNameFilter implements FilenameFilter {
	
	private String[] files;
	
	
	/** 
	 * Default constructor. In this case, this filter will be
	 * accept all <code>.jar</code> archives.
	 */
	public PluginFileNameFilter() {
		// do nothing
	}

	/**
	 * Set up array with name of files.
	 * @param filesName array with name of files
	 */
	public PluginFileNameFilter(String[] filesName) {
		this.files = filesName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean accept(File dir, String name) {
		if (name.endsWith(PluginUtils.JAR_EXTENSION)) {
			if (files == null) {
				return true;
			} else {
				for (String item : files) {
					if (item.trim().equals(name.substring(0, name.lastIndexOf('.')))) {
						return true;
					}
				}
				return false;
			}
		} else {
			if (dir.isDirectory() && !name.startsWith(".")) {
				return true;
			}
		}
		return false;
	}

}
