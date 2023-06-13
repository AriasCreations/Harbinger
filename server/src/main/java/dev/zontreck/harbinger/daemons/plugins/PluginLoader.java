package dev.zontreck.harbinger.daemons.plugins;

import dev.zontreck.harbinger.HarbingerServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public enum PluginLoader {
	;
	public static final Logger LOGGER = LoggerFactory.getLogger(PluginLoader.class.getSimpleName());

	public static List<Path> JARS = new ArrayList<>();

	public static void scan() throws MalformedURLException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		final ClassLoader loader = ClassLoader.getSystemClassLoader();

		final File[] lst = HarbingerServer.PLUGINS.toFile().listFiles();
		final boolean iNextJar = false;
		/*for(File f : lst)
		{
			URL jar = f.toURI().toURL();
			for(URL it : Arrays.asList())
			{
				if(it.equals(jar))iNextJar=true;
				if(iNextJar)break;
			}
			if(!iNextJar)
			{
				Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
				method.setAccessible(true);
				method.invoke(loader, new Object[]{jar});
			}
		}*/
	}

	public static void activate() {
		final Class<?> clazz = Plugin.class;
		final Class<?>[] cls = clazz.getClasses();
		final List<IPlugin> plugins = new ArrayList<>();

		for (final Class<?> C : cls) {
			final Plugin plug = C.getAnnotation(Plugin.class);
			PluginLoader.LOGGER.info("Activating plugin: {}", plug.name());

			try {
				final Constructor<IPlugin> Construct = (Constructor<IPlugin>) C.getConstructor();
				final IPlugin plugin = Construct.newInstance();
				plugins.add(plugin);

				plugin.init();

				PluginLoader.LOGGER.info("Plugin activated [{}]", plug.name());
			} catch (final NoSuchMethodException |
						   InvocationTargetException |
						   InstantiationException |
						   IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
