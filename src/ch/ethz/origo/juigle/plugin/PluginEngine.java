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
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import ch.ethz.origo.juigle.plugin.errors.PluginEngineErrorCodes;
import ch.ethz.origo.juigle.plugin.exception.PluginEngineException;

/**
 * Manage the plug-ins and the updates. Use getInstance() to get the shared
 * instance for this class.
 * 
 * @author vsouhrada (v.souhrada at gmail.com)
 * @version 1.0.2 (5/01/2011)
 * @since 0.1.0 (3/07/2010)
 * 
 */
public class PluginEngine {

	/** XML file where the list of installed plug-ins is stored */
	private String filePath;
	/** Directory where plug-ins files are stored */
	public static final String DIR = "plugins";
	/** Name of file where is plug-in defined */
	private static String FILE_NAME = "plugin.xml";
	
	private static final String ELEMENT_CATEGORY = "category";
	
	/** Instance on Plug-in Engine */
	private static PluginEngine instance;

	private int majorVersion = 0;
	private int minorVersion = 1;
	private int revisionVersion = 0;

	private List<IPluggable> plugins = new ArrayList<IPluggable>();
	private Map<IPluggable, String> localSources = new HashMap<IPluggable, String>();
	private Map<IPluggable, Boolean> hiddens = new HashMap<IPluggable, Boolean>();
	private Map<IPluggable, Boolean> updateEnable = new HashMap<IPluggable, Boolean>();
	private Map<IPluggable, Boolean> enabled = new HashMap<IPluggable, Boolean>();
	private Map<String, List<IPluggable>> listOfAllPlugins = new HashMap<String, List<IPluggable>>();

	private PluginEngine() {
	}

	/**
	 * @return the shared instance of this class
	 */
	public static PluginEngine getInstance() {
		if (instance == null) {
			instance = new PluginEngine();
		}
		return instance;
	}

	/**
	 * Change the shared instance of this class
	 */
	public static void setInstance(PluginEngine engine) {
		PluginEngine.instance = engine;
	}

	/**
	 * This method should be called first before starting plug-ins
	 * 
	 * @param filePath
	 * @throws PlugEngineException
	 */
	public void init(String filePath) throws PluginEngineException {
		this.filePath = filePath;
		this.loadPluggables();
	}

	/**
	 * load all the installed plugins without starting them
	 * 
	 * @throws PlugEngineException
	 */
	protected void loadPluggables() throws PluginEngineException {
		try {
			List<File> listOfFiles = PluginUtils
					.getAllPluginsFiles(new File(filePath));
			for (File file : listOfFiles) {
				if (file.exists()) {
					InputStream is = includeJar(file);
					if (is != null) {
						DefaultHandler handler = new PlugEngineHandler(true);
						SAXParserFactory.newInstance().newSAXParser().parse(is, handler);
						// } else {
						// TODO jinak zapis error do logu o tom ,ze plugin bude preskocen
						// TODO nebot neobsahuje plugin.xml file, ale muze to byt jen
						// knihovna, kterou dany plugin externe
						// TODO vyuziva, takze to nastavit jen jako logger.warn()
						// }
					}
					/*
					 * // delete unused plugin folder : File pluginsFolder = new
					 * File(DIR); for (File folder : pluginsFolder.listFiles(new
					 * RemovedPluginFolderFilter())) { for (File file :
					 * folder.listFiles()) file.delete(); folder.delete(); }
					 */
				}
			}
		} catch (Exception e) {
			throw new PluginEngineException(
					PluginEngineErrorCodes.UNABLE_PLUGINS_LOAD, e);
		}
	}

	private InputStream includeJar(File file) throws Exception {
		if (file.isDirectory())
			return null;

		URL jarURL = null;
		JarFile jar = null;
		try {
			jarURL = new URL("file://" + file.getCanonicalPath());
			jarURL = new URL("jar:" + jarURL.toExternalForm() + "!/");
			JarURLConnection conn = (JarURLConnection) jarURL.openConnection();
			jar = conn.getJarFile();
		} catch (Exception e) {
			// not a JAR or disk I/O error
			// either way, just skip
			return null;
		}

		if (jar == null || jarURL == null)
			return null;

		Enumeration<JarEntry> e = jar.entries();
		while (e.hasMoreElements()) {
			JarEntry entry = e.nextElement();

			if (!entry.isDirectory()) {
				if (entry.getName().toUpperCase()
						.equalsIgnoreCase(PluginUtils.XML_PLUGIN_FILE_NAME)) {
					addFileToClasspath(file);
					return jar.getInputStream(entry);
				}
				continue;
			}
		}
		return null;
	}

	/**
	 * Start all the loaded plugins without arguments (objects, data) for plugin.
	 */
	public void startPluggables() {
		for (IPluggable plugin : plugins) {
			this.startPluggable(plugin);
		}
	}

	/**
	 * Start all the loaded plugins with arguments (objects, data) for all
	 * plugins.
	 */
	public void startPluggables(Object... args) {
		for (IPluggable plugin : plugins) {
			this.startPluggable(plugin, args);
		}
	}

	/**
	 * Start the given plugin.
	 */
	public void startPluggable(IPluggable plugin, Object... args) {
		plugin.init(args);
	}

	/**
	 * Save the list of the installed plugins in a XML file
	 * 
	 * @throws PlugEngineException
	 * @version 0.2.0 (4/04/2010)
	 * @since 0.1.0 (3/07/2010)
	 */
	protected void savePluggable() throws PluginEngineException {
		try {
			Document document = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder().newDocument();
			document.setXmlStandalone(true);
			Element root = document.createElement("plugins");
			document.appendChild(root);
			Set<Entry<String, List<IPluggable>>> pluginsToSave = listOfAllPlugins
					.entrySet();
			// for - each all categories
			for (Entry<String, List<IPluggable>> entry : pluginsToSave) {
				Element categyElt = document.createElement(ELEMENT_CATEGORY);
				categyElt.setAttribute("name", entry.getKey());
				root.appendChild(categyElt);
				List<IPluggable> pluggByCategory = entry.getValue();
				// for each plugins from given category
				for (IPluggable plugin : pluggByCategory) {
					Element plugElt = document.createElement("plugin");
					plugElt.setAttribute("hidden", hiddens.get(plugin).toString());
					plugElt.setAttribute("update", updateEnable.get(plugin).toString());
					plugElt.setAttribute("enabled", enabled.get(plugin).toString());

					categyElt.appendChild(plugElt);
					Element sourceElt = document.createElement("source");
					sourceElt.setTextContent(localSources.get(plugin));
					plugElt.appendChild(sourceElt);
					Element classElt = document.createElement("class");
					classElt.setTextContent(plugin.getClass().getName().trim());
					plugElt.appendChild(classElt);
					Element appVersionElt = document.createElement("appVersion");
					plugElt.appendChild(appVersionElt);
				}
			}
			TransformerFactory
					.newInstance()
					.newTransformer()
					.transform(new DOMSource(document),
							new StreamResult(new File(filePath)));

		} catch (Exception e) {
			throw new PluginEngineException(
					PluginEngineErrorCodes.UNABLE_SAVE_PLUGINS_LIST, e);
		}
	}

	/**
	 * Install or update a plugin
	 * 
	 * @param updateFile
	 *          the XML configuration file for the plugin to install
	 * @return the installed plugin
	 * @throws PlugEngineException
	 */
	public IPluggable installOrUpdate(URI updateFile)
			throws PluginEngineException {
		try {
			PlugEngineHandler handler = new PlugEngineHandler(false);
			SAXParserFactory.newInstance().newSAXParser()
					.parse(updateFile.toString(), handler);

			return handler.getPluggable();

		} catch (Exception e) {
			throw new PluginEngineException(
					PluginEngineErrorCodes.UNABLE_INSTALL_PLUGIN, e);
		}
	}

	/**
	 * Add or update a plugin
	 * 
	 * @param plugin
	 * @throws PlugEngineException
	 */
	protected void addPluggable(IPluggable plugin) throws PluginEngineException {
		for (Iterator i = plugins.iterator(); i.hasNext();) {
			IPluggable p = (IPluggable) i.next();
			if (plugin.getPluginName().equals(p.getPluginName()))
				i.remove();
		}
		this.plugins.add(plugin);
		this.hiddens.put(plugin, false);
		this.updateEnable.put(plugin, true);
		this.enabled.put(plugin, isCompatible(plugin));

		this.savePluggable();
	}

	/**
	 * De-install a plugin, and remove its file
	 * 
	 * @throws PlugEngineException
	 */
	public void removePluggable(IPluggable plugin) throws PluginEngineException {
		plugin.destroy();

		this.plugins.remove(plugin);
		this.savePluggable();

		File folder = new File(localSources.get(plugin));
		folder.deleteOnExit();
		for (File file : folder.listFiles()) {
			file.deleteOnExit();
		}
	}

	/**
	 * @return the list of all loaded plugins
	 */
	public List<IPluggable> getAllPluggables() {
		return plugins;
	}

	/**
	 * @return the list of all non hidden loaded plugins
	 */
	public List<IPluggable> getAllVisiblePluggables() {
		List<IPluggable> result = new ArrayList<IPluggable>();

		for (IPluggable plugin : plugins) {
			if (!isHidden(plugin))
				result.add(plugin);
		}

		return result;
	}

	public List<IPluggable> getAllCorrectPluggables() {
		List<IPluggable> result = new ArrayList<IPluggable>();

		Set<Entry<IPluggable, Boolean>> map = enabled.entrySet();

		for (Entry<IPluggable, Boolean> entry : map) {
			if (entry.getValue()) {
				result.add(entry.getKey());
			}
		}
		return result;
	}

	/**
	 * Return all plugins from category which are enable, not hidden and are
	 * compatible with current application version
	 * 
	 * @param category
	 *          name of plugins category
	 * @return all correct plugins from entered category
	 * @version 0.2.0 (4/5/2011)
	 * @since 0.1.2 (3/28/2010)
	 */
	public List<IPluggable> getAllCorrectPluggables(String category) {
		List<IPluggable> correctList = new ArrayList<IPluggable>();
		if (listOfAllPlugins != null && !listOfAllPlugins.isEmpty()) {
			List<IPluggable> pluginsList = listOfAllPlugins.get(category);
			if (pluginsList != null && !pluginsList.isEmpty()) {
				for (IPluggable item : pluginsList) {
					if (isEnabled(item) && !isHidden(item) && isCompatible(item)) {
						correctList.add(item);
					}
				}
			}
		}

		return correctList;
	}

	/**
	 * @return the list of all loaded plugins with update enabled
	 */
	public List<IPluggable> getAllUpdatablePluggables() {
		List<IPluggable> result = new ArrayList<IPluggable>();

		for (IPluggable plugin : plugins) {
			if (isUpdateEnabled(plugin))
				result.add(plugin);
		}

		return result;
	}

	/**
	 * @return the list of all loaded and enabled plugins
	 */
	public List<IPluggable> getAllEnabledPluggables() {
		List<IPluggable> result = new ArrayList<IPluggable>();

		for (IPluggable plugin : plugins) {
			if (isEnabled(plugin))
				result.add(plugin);
		}

		return result;
	}

	/**
	 * @return true if the given plugin should not be show to the user
	 */
	public boolean isHidden(IPluggable plugin) {
		return hiddens.get(plugin);
	}

	/**
	 * @return false if the given plugin should not be updated
	 */
	public boolean isUpdateEnabled(IPluggable plugin) {
		return updateEnable.get(plugin);
	}

	/**
	 * Enable or disable the auto update of a plugin
	 * 
	 * @throws PlugEngineException
	 */
	public void setUpdateEnabled(IPluggable plugin, boolean enabled)
			throws PluginEngineException {
		if (plugin != null) {
			this.updateEnable.put(plugin, enabled);
			this.savePluggable();
		}
	}

	/**
	 * @return false if the given plugin has been disabled
	 */
	public boolean isEnabled(IPluggable plugin) {
		return enabled.get(plugin);
	}

	/**
	 * Enable or disable the auto update of a plugin
	 * 
	 * @throws PlugEngineException
	 */
	public void setEnabled(IPluggable plugin, boolean enabled)
			throws PluginEngineException {
		if (plugin != null) {
			if (!enabled || isCompatible(plugin)) {
				this.enabled.put(plugin, enabled);
				this.savePluggable();
			}
		}
	}

	/**
	 * @return the directory in which the files of the given plugin are storred
	 */
	public File getDirectory(IPluggable plugin) {
		String folder = this.localSources.get(plugin);
		return new File(folder);
	}

	/**
	 * @return the list of all programs which have a new version to install
	 */
	public List<IPluggable> checkForUpdates() {
		List<IPluggable> result = new ArrayList<IPluggable>();
		for (IPluggable update : this.getAllUpdatablePluggables()) {
			try {
				PlugEngineHandler handler = new PlugEngineHandler(update);
				SAXParserFactory.newInstance().newSAXParser()
						.parse(update.getURI().toString(), handler);
				if (handler.isUpdate())
					result.add(update);

			} catch (Exception e) {
			}
		}
		return result;
	}

	/**
	 * Return true if the given plug-in does not work for this version of the main
	 * application
	 * 
	 * @return true if the given plug-in does not work for this version of the
	 *         main application
	 * @see IPluggable#getMinimalAppVersion()
	 * @see PlugEngine#getCurrentVersion()
	 */
	public boolean isCompatible(IPluggable plugin) {
		int[] minimal = plugin.getMinimalAppVersion();
		String appVersion = String.valueOf(majorVersion)
				+ String.valueOf(minorVersion) + String.valueOf(revisionVersion);
		String minimalVersion = String.valueOf(minimal[0])
				+ String.valueOf(minimal[1]) + String.valueOf(minimal[2]);

		if (Integer.valueOf(appVersion) >= Integer.valueOf(minimalVersion)) {
			return true;
		}
		return false;
	}

	/**
	 * Plugin with <code>getMinimalAppVersion()</code> < to
	 * <code>getCurrentVersion()</code> will be disabled.
	 * 
	 * @return the current version of the main application
	 */
	public int[] getCurrentVersion() {
		return new int[] { majorVersion, minorVersion, revisionVersion };
	}

	/**
	 * Change the current version of the main application. Plug-in with
	 * <code>getMinimalVersion()</code> < to <code>getCurrentVersion()</code> will
	 * be disabled.
	 * 
	 * @param major
	 * @param minor
	 * @param revision
	 * @throws PlugEngineException
	 */
	public void setCurrentVersion(int major, int minor, int revision)
			throws PluginEngineException {
		this.majorVersion = major;
		this.minorVersion = minor;
		this.revisionVersion = revision;

		if (plugins.size() > 0) {
			for (IPluggable plugin : plugins) {
				if (isEnabled(plugin) && !isCompatible(plugin)) {
					this.enabled.put(plugin, false);
				}
			}
			savePluggable();
		}
	}

	/**
	 * Add a file to classpath at runtime
	 * 
	 * @param fileUrl
	 *          the path of the file to add
	 * @throws IOException
	 * @author 
	 *         http://forum.java.sun.com/thread.jspa?threadID=300557&start=45&tstart
	 *         =0
	 */
	protected static void addFileToClasspath(String fileUrl) throws Exception {
		addFileToClasspath(new File(fileUrl));
	}

	/**
	 * add a file to classpath at runtime
	 * 
	 * @param file
	 *          the file to add
	 * @throws IOException
	 * @author 
	 *         http://forum.java.sun.com/thread.jspa?threadID=300557&start=45&tstart
	 *         =0
	 */
	protected static void addFileToClasspath(File file) throws Exception {
		Method method = URLClassLoader.class.getDeclaredMethod("addURL",
				new Class[] { URL.class });
		method.setAccessible(true);
		method.invoke(ClassLoader.getSystemClassLoader(), new Object[] { file
				.toURI().toURL() });
	}

	/**
	 * Parse XML configuration files
	 * 
	 * @author vsouhrada (v.souhrada at gmail.com)
	 * @see DefaultHandler
	 */
	protected class PlugEngineHandler extends DefaultHandler {

		private boolean inVersion;
		private boolean inClass;
		private boolean inSource;
		private boolean inPlugin;
		private boolean inCategory;
		private boolean inAppVersion;
		private boolean local;
		private boolean update;
		private boolean hidden;
		private boolean updateEnabled = true;
		private boolean enable = true;
		private String folder;
		private String source;
		private String newVersion;
		private String category;
		private String appVersion;
		private IPluggable pluggable;

		/**
		 * @param local
		 *          indicate if we parse the local list of plug-ins, or a distant
		 *          configuration file of a plug-in
		 */
		public PlugEngineHandler(boolean local) {
			this.local = local;
		}

		/**
		 * @param pluggable
		 *          the plug-in which configuration file will be parsed
		 */
		public PlugEngineHandler(IPluggable pluggable) {
			this.local = false;
			this.pluggable = pluggable;
		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			inCategory = qName.equalsIgnoreCase(ELEMENT_CATEGORY);
			inPlugin = qName.equalsIgnoreCase("plugin") || inPlugin;
			inVersion = inPlugin && qName.equalsIgnoreCase("version");
			inClass = inPlugin && qName.equalsIgnoreCase("class");
			inSource = inPlugin && qName.equalsIgnoreCase("source");
			inAppVersion = inPlugin && qName.equalsIgnoreCase("appVersion");

			if (qName.equalsIgnoreCase("plugin")) {
				hidden = Boolean.parseBoolean(attributes.getValue("hidden"));
				updateEnabled = !"false".equalsIgnoreCase(attributes.getValue("update"));
				enable = !"false".equalsIgnoreCase(attributes.getValue("enabled"));
			}

			if (qName.equalsIgnoreCase(ELEMENT_CATEGORY)) {
				category = attributes.getValue("name");
			}
		}

		public void characters(char[] ch, int start, int length)
				throws SAXException {
			try {
				if (inVersion) {
					newVersion = new String(ch, start, length);
					/*
					 * if (pluggable.getPluginVersion() != null &&
					 * !pluggable.getPluginVersion().equalsIgnoreCase(newVersion)) update
					 * = true;
					 */
					if (pluggable != null) {
						pluggable.setPluginVersion(newVersion);
					}
				} else if (inSource && pluggable == null) {
					source = new String(ch, start, length);
					if (local) {
						File localFolder = new File(source);
						for (File file : localFolder.listFiles()) {
							addFileToClasspath(file);
						}
					}
				} else if (inClass && pluggable == null) {
					String className = new String(ch, start, length);
					Class<?> loadedClass = Class.forName(className);
					pluggable = (IPluggable) loadedClass.newInstance();
					// set up a versions for plug-in
					if (newVersion != null) {
						pluggable.setPluginVersion(newVersion);
					}
					if (appVersion != null) {
						setMinimalAppVersion(appVersion);
					}
					if (local) {
						plugins.add(pluggable);
						hiddens.put(pluggable, hidden);
						updateEnable.put(pluggable, updateEnabled);
						enabled.put(pluggable, enable && isCompatible(pluggable));
						localSources.put(pluggable, source);
					} else {
						addPluggable(pluggable);
						localSources.put(pluggable, DIR + File.separator + folder);
					}
					pluggable.setCategory(category);
					// add a plug-in into specific category
					addPluggableToCathegoryList(pluggable, category);
					folder = null;
				} else if (inAppVersion) {
					// get a minimal version for applicaton
					appVersion = new String(ch, start, length);
					// set a minimal version of app. for which is a plug-in compatible
					if (pluggable != null) {
						setMinimalAppVersion(appVersion);
					}
				}
			} catch (Exception e) {
				throw new SAXException(e);
			}
		}

		private void addPluggableToCathegoryList(IPluggable plugin, String category) {
			List<IPluggable> pluggables = listOfAllPlugins.get(category);
			if (pluggables != null) {
			} else {
				pluggables = new ArrayList<IPluggable>();
			}
			pluggables.add(plugin);
			listOfAllPlugins.put(category, pluggables);
		}

		private void setMinimalAppVersion(String appVersion) {
			pluggable.setMinimalAppVersion(PluginUtils
					.getVersionOfPluginAsArray(appVersion));
		}

		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			if (qName.equalsIgnoreCase("version"))
				inVersion = false;
			if (qName.equalsIgnoreCase("class"))
				inClass = false;
			if (qName.equalsIgnoreCase("plugin")) {
				inPlugin = false;
				if (local)
					pluggable = null;
			}
			if (qName.equalsIgnoreCase(ELEMENT_CATEGORY)) {
				inCategory = false;
				category = null;
			}
			if (qName.equalsIgnoreCase("source")) {
				inSource = false;
			}
			if (qName.equalsIgnoreCase("appVersion")) {
				inAppVersion = false;
			}
		}

		/**
		 * @return true if there is a new version in the XML configuration file
		 */
		public boolean isUpdate() {
			return update;
		}

		/**
		 * the current plugin
		 */
		public IPluggable getPluggable() {
			return pluggable;
		}

	}

	private class RemovedPluginFolderFilter implements FileFilter {

		public boolean accept(File pathname) {
			if (pathname.isDirectory())
				return !localSources.values().contains(
						DIR + File.separator + pathname.getName());

			return false;
		}

	}

}