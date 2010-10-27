package ch.ethz.origo.juigle.plugin;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import ch.ethz.origo.juigle.plugin.errors.PluginEngineErrorCodes;
import ch.ethz.origo.juigle.plugin.exception.PluginEngineException;

/**
 * 
 * 
 * @author vsouhrada (v.souhrada at gmail.com)
 * @version 0.1.0.00 (10/20/2010)
 * @since 2.0.0 (10/20/2010)
 * 
 */
public class PluginUtils {

	/** Extension of JAR archive */
	public static final String JAR_EXTENSION = ".jar";
	/** Plug-in descriptor file */
	public static final String XML_PLUGIN_FILE_NAME = "plugin.xml";

	/**
	 * Return all Plug-ins JAR archives from the root directory (parameter files).
	 * 
	 * @param files
	 *          root directory where are plug-ins stores.
	 * @return all Plug-ins JAR archives from the root directory (parameter
	 *         files).
	 * @throws PluginEngineException
	 */
	public static List<File> getAllPluginsFiles(File files)
			throws PluginEngineException {
		List<File> listOfFiles = new ArrayList<File>();
		if (files.exists()) {
			File[] arrayOfFiles = files.listFiles(new PluginFileNameFilter());
			for (int i = 0; i < arrayOfFiles.length; i++) {
				File file = arrayOfFiles[i];
				if (file.isDirectory()) {
					List<File> anotherFiles = getAllPluginsFiles(file);
					for (File fileItm : anotherFiles) {
						// FIXME
						listOfFiles.add(fileItm);
					}
				} else {
					listOfFiles.add(file);
				}
			}
		} else {
			throw new PluginEngineException(PluginEngineErrorCodes.NO_FOUND_PLUGINS_IN_DIRECTORY_P1 + ": "
					+ files.getAbsolutePath());
		}
		return listOfFiles;
	}

	public static void printClasspath() {

		// Get the System Classloader
		ClassLoader sysClassLoader = ClassLoader.getSystemClassLoader();

		// Get the URLs
		URL[] urls = ((URLClassLoader) sysClassLoader).getURLs();

		for (int i = 0; i < urls.length; i++) {
			System.out.println(urls[i].getFile());
		}

	}

}
