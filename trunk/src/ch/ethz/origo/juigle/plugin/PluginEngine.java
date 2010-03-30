package ch.ethz.origo.juigle.plugin;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import ch.ethz.origo.juigle.plugin.exception.PluginEngineException;

/**
 * Manage the plugins and the updates. Use getInstance() to get the shared
 * instance for this class.
 * 
 * @author Vaclav Souhrada (v.souhrada at gmail.com)
 * @version 0.1.3 (3/29/2010)
 * @since (3/07/2010)
 * 
 */
public class PluginEngine {

	/**
	 * XML file where the list of installed plugins is stored
	 */
	private String file; //$NON-NLS-1$
	/**
	 * directory where plugins files are stored
	 */
	public static final String DIR = "plugins"; //$NON-NLS-1$
	private static PluginEngine instance;

	private int majorVersion = 0;
	private int minorVersion = 1;
	private int revisionVersion = 0;

	private List<Pluggable> plugins = new ArrayList<Pluggable>();
	private Map<Pluggable, String> localSources = new HashMap<Pluggable, String>();
	private Map<Pluggable, Boolean> hiddens = new HashMap<Pluggable, Boolean>();
	private Map<Pluggable, Boolean> updateEnable = new HashMap<Pluggable, Boolean>();
	private Map<Pluggable, Boolean> enabled = new HashMap<Pluggable, Boolean>();
	private Map<String, List<Pluggable>> listOfAllPlugins = new HashMap<String, List<Pluggable>>();

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
	 * change the shared instance of this class
	 */
	public static void setInstance(PluginEngine engine) {
		PluginEngine.instance = engine;
	}

	/**
	 * this method should be called first before starting plugins
	 * 
	 * @param filePath
	 * @throws PlugEngineException
	 */
	public void init(String filePath) throws PluginEngineException {
		this.file = filePath;
		this.loadPluggables();
	}

	/**
	 * load all the installed plugins without starting them
	 * 
	 * @throws PlugEngineException
	 */
	protected void loadPluggables() throws PluginEngineException {
		try {
			File xml = new File(file);

			if (xml.exists()) {
				DefaultHandler handler = new PlugEngineHandler(true);
				SAXParserFactory.newInstance().newSAXParser().parse(xml, handler);
			}
			/*
			 * // delete unused plugin folder : File pluginsFolder = new File(DIR);
			 * for (File folder : pluginsFolder.listFiles(new
			 * RemovedPluginFolderFilter())) { for (File file : folder.listFiles())
			 * file.delete(); folder.delete(); }
			 */

		} catch (Exception e) {
			throw new PluginEngineException("Unable to load plugins", e); //$NON-NLS-1$
		}
	}

	/**
	 * Start all the loaded plugins without arguments (objects, data) for plugin.
	 */
	public void startPluggables() {
		for (Pluggable plugin : plugins) {
			this.startPluggable(plugin);
		}
	}

	/**
	 * Start all the loaded plugins with arguments (objects, data) for all
	 * plugins.
	 */
	public void startPluggables(Object... args) {
		for (Pluggable plugin : plugins) {
			this.startPluggable(plugin, args);
		}
	}

	/**
	 * Start the given plugin.
	 */
	public void startPluggable(Pluggable plugin, Object... args) {
		plugin.init(args);
	}

	/**
	 * save the list of the installed plugins in a XML file
	 * 
	 * @throws PlugEngineException
	 */
	protected void savePluggable() throws PluginEngineException {
		System.out.println("Neukladam,,,,zmeneno");
		/*
		 * try { Document document = DocumentBuilderFactory.newInstance()
		 * .newDocumentBuilder().newDocument(); document.setXmlStandalone(true);
		 * 
		 * Element root = document.createElement("plugins"); //$NON-NLS-1$
		 * document.appendChild(root); for (Pluggable plugin : this.plugins) {
		 * Element plugElt = document.createElement("plugin"); //$NON-NLS-1$
		 * plugElt.setAttribute("hidden", hiddens.get(plugin).toString());
		 * //$NON-NLS-1$ plugElt.setAttribute("update",
		 * updateEnable.get(plugin).toString()); //$NON-NLS-1$
		 * plugElt.setAttribute("enabled", enabled.get(plugin).toString());
		 * //$NON-NLS-1$
		 * 
		 * root.appendChild(plugElt); Element sourceElt =
		 * document.createElement("source"); //$NON-NLS-1$
		 * sourceElt.setTextContent(localSources.get(plugin));
		 * plugElt.appendChild(sourceElt); Element classElt =
		 * document.createElement("class"); //$NON-NLS-1$
		 * classElt.setTextContent(plugin.getClass().getName().trim());
		 * plugElt.appendChild(classElt); }
		 * 
		 * TransformerFactory.newInstance().newTransformer().transform( new
		 * DOMSource(document), new StreamResult(new File(file)));
		 * 
		 * } catch (Exception e) { throw new
		 * PluginEngineException("Unable to save plugins list", e); //$NON-NLS-1$ }
		 */
	}

	/**
	 * Install or update a plugin
	 * 
	 * @param updateFile
	 *          the XML configuration file for the plugin to install
	 * @return the installed plugin
	 * @throws PlugEngineException
	 */
	public Pluggable installOrUpdate(URI updateFile) throws PluginEngineException {
		try {
			PlugEngineHandler handler = new PlugEngineHandler(false);
			SAXParserFactory.newInstance().newSAXParser().parse(
					updateFile.toString(), handler);

			return handler.getPluggable();

		} catch (Exception e) {
			throw new PluginEngineException("Unable to install plugin", e); //$NON-NLS-1$
		}
	}

	/**
	 * add or update a plugin
	 * 
	 * @param plugin
	 * @throws PlugEngineException
	 */
	protected void addPluggable(Pluggable plugin) throws PluginEngineException {
		for (Iterator i = plugins.iterator(); i.hasNext();) {
			Pluggable p = (Pluggable) i.next();
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
	public void removePluggable(Pluggable plugin) throws PluginEngineException {
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
	public List<Pluggable> getAllPluggables() {
		return plugins;
	}

	/**
	 * @return the list of all non hidden loaded plugins
	 */
	public List<Pluggable> getAllVisiblePluggables() {
		List<Pluggable> result = new ArrayList<Pluggable>();

		for (Pluggable plugin : plugins) {
			if (!isHidden(plugin))
				result.add(plugin);
		}

		return result;
	}

	public List<Pluggable> getAllCorrectPluggables() {
		List<Pluggable> result = new ArrayList<Pluggable>();

		Set<Entry<Pluggable, Boolean>> map = enabled.entrySet();

		for (Entry<Pluggable, Boolean> entry : map) {
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
	 * @param category name of plugins category
	 * @return all correct plugins from entered category
	 * @version 0.1.1 (3/29/2010)
	 * @since 0.1.2 (3/28/2010)
	 */
	public List<Pluggable> getAllCorrectPluggables(String category) {
		List<Pluggable> pluginsList = listOfAllPlugins.get(category);
		List<Pluggable> correctList = new ArrayList<Pluggable>();
		if (pluginsList.size() > 0) {
			for (Pluggable item : pluginsList) {
				if (isEnabled(item) && !isHidden(item) && isCompatible(item)) {
					correctList.add(item);
				}
			}
		}
		return correctList;
	}

	/**
	 * @return the list of all loaded plugins with update enabled
	 */
	public List<Pluggable> getAllUpdatablePluggables() {
		List<Pluggable> result = new ArrayList<Pluggable>();

		for (Pluggable plugin : plugins) {
			if (isUpdateEnabled(plugin))
				result.add(plugin);
		}

		return result;
	}

	/**
	 * @return the list of all loaded and enabled plugins
	 */
	public List<Pluggable> getAllEnabledPluggables() {
		List<Pluggable> result = new ArrayList<Pluggable>();

		for (Pluggable plugin : plugins) {
			if (isEnabled(plugin))
				result.add(plugin);
		}

		return result;
	}

	/**
	 * @return true if the given plugin should not be show to the user
	 */
	public boolean isHidden(Pluggable plugin) {
		return hiddens.get(plugin);
	}

	/**
	 * @return false if the given plugin should not be updated
	 */
	public boolean isUpdateEnabled(Pluggable plugin) {
		return updateEnable.get(plugin);
	}

	/**
	 * enable or disable the auto update of a plugin
	 * 
	 * @throws PlugEngineException
	 */
	public void setUpdateEnabled(Pluggable plugin, boolean enabled)
			throws PluginEngineException {
		if (plugin != null) {
			this.updateEnable.put(plugin, enabled);
			this.savePluggable();
		}
	}

	/**
	 * @return false if the given plugin has been disabled
	 */
	public boolean isEnabled(Pluggable plugin) {
		return enabled.get(plugin);
	}

	/**
	 * enable or disable the auto update of a plugin
	 * 
	 * @throws PlugEngineException
	 */
	public void setEnabled(Pluggable plugin, boolean enabled)
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
	public File getDirectory(Pluggable plugin) {
		String folder = this.localSources.get(plugin);
		return new File(folder);
	}

	/**
	 * @return the list of all programs which have a new version to install
	 */
	public List<Pluggable> checkForUpdates() {
		List<Pluggable> result = new ArrayList<Pluggable>();
		for (Pluggable update : this.getAllUpdatablePluggables()) {
			try {
				PlugEngineHandler handler = new PlugEngineHandler(update);
				SAXParserFactory.newInstance().newSAXParser().parse(
						update.getURI().toString(), handler);
				if (handler.isUpdate())
					result.add(update);

			} catch (Exception e) {
			}
		}
		return result;
	}

	/**
	 * @return true if the given plugin does not work for this version of the main
	 *         application
	 * @see Pluggable#getMinimalAppVersion()
	 * @see PlugEngine#getCurrentVersion()
	 */
	public boolean isCompatible(Pluggable plugin) {
		int[] minimal = plugin.getMinimalAppVersion();
		int[] plugVersion = this.getCurrentVersion();

		if ((minimal[0] <= plugVersion[0]) && (minimal[1] <= plugVersion[1])
				&& (minimal[2] <= plugVersion[2])) {
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
	 * change the current version of the main application. Plugin with
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
			for (Pluggable plugin : plugins) {
				if (isEnabled(plugin) && !isCompatible(plugin)) {
					this.enabled.put(plugin, false);
				}
			}
			savePluggable();
		}
	}

	/**
	 * add a file to classpath at runtime
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
		Method method = URLClassLoader.class.getDeclaredMethod(
				"addURL", new Class[] { URL.class }); //$NON-NLS-1$
		method.setAccessible(true);
		method.invoke(ClassLoader.getSystemClassLoader(), new Object[] { file
				.toURI().toURL() });
	}

	/**
	 * parse XML configuration files
	 */
	protected class PlugEngineHandler extends DefaultHandler {

		private boolean inVersion = false, inClass = false, inSource = false,
				inPlugin = false, inCategory = false;
		private boolean local, update = false, hidden = false,
				updateEnabled = true, enable = true;
		private String folder, source, newVersion, category = null;
		private Pluggable pluggable;

		/**
		 * @param local
		 *          indicate if we parse the local list of plugins, or a distant
		 *          configuartion file of a plugin
		 */
		public PlugEngineHandler(boolean local) {
			this.local = local;
		}

		/**
		 * @param pluggable
		 *          the plugin which configuration file will be parsed
		 */
		public PlugEngineHandler(Pluggable pluggable) {
			this.local = false;
			this.pluggable = pluggable;
		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			inCategory = qName.equalsIgnoreCase("category"); //$NON-NLS-1$
			inPlugin = qName.equalsIgnoreCase("plugin") || inPlugin; //$NON-NLS-1$
			inVersion = inPlugin && qName.equalsIgnoreCase("version"); //$NON-NLS-1$
			inClass = inPlugin && qName.equalsIgnoreCase("class"); //$NON-NLS-1$
			inSource = inPlugin && qName.equalsIgnoreCase("source"); //$NON-NLS-1$

			if (qName.equalsIgnoreCase("plugin")) { //$NON-NLS-1$
				hidden = Boolean.parseBoolean(attributes.getValue("hidden")); //$NON-NLS-1$
				updateEnabled = !"false".equalsIgnoreCase(attributes.getValue("update")); //$NON-NLS-1$ //$NON-NLS-2$
				enable = !"false".equalsIgnoreCase(attributes.getValue("enabled")); //$NON-NLS-1$ //$NON-NLS-2$
			}

			if (qName.equalsIgnoreCase("category")) { //$NON-NLS-1$
				category = attributes.getValue("name"); //$NON-NLS-1$
				System.out.println("nacetla kategorue " + category);
			}
		}

		public void characters(char[] ch, int start, int length)
				throws SAXException {
			try {
				if (inVersion && pluggable != null) {
					newVersion = new String(ch, start, length);
					if (pluggable.getPluginVersion() != null
							&& !pluggable.getPluginVersion().equalsIgnoreCase(newVersion))
						update = true;

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
					System.out.println("class name " + className);
					Class<?> loadedClass = Class.forName(className);
					pluggable = (Pluggable) loadedClass.newInstance();
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
					addPluggableToCathegoryList(pluggable, category);
					folder = null;
				}
			} catch (Exception e) {
				throw new SAXException(e);
			}
		}

		private void addPluggableToCathegoryList(Pluggable plugin, String category) {
			System.out.println("davame plugin do kategorue " + category);
			List<Pluggable> pluggables = listOfAllPlugins.get(category);
			if (pluggables != null) {
			} else {
				pluggables = new ArrayList<Pluggable>();
			}
			pluggables.add(plugin);
			listOfAllPlugins.put(category, pluggables);
		}

		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			if (qName.equalsIgnoreCase("version")) //$NON-NLS-1$
				inVersion = false;
			if (qName.equalsIgnoreCase("class")) //$NON-NLS-1$
				inClass = false;
			if (qName.equalsIgnoreCase("plugin")) { //$NON-NLS-1$
				inPlugin = false;
				if (local)
					pluggable = null;
			}
			if (qName.equalsIgnoreCase("category")) { //$NON-NLS-1$
				inCategory = false;
				category = null;
			}
			if (qName.equalsIgnoreCase("source")) //$NON-NLS-1$
				inSource = false;
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
		public Pluggable getPluggable() {
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
