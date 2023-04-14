package dev.zontreck.harbinger.daemons.plugins;

import dev.zontreck.harbinger.HarbingerServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PluginLoader
{
	public static final Logger LOGGER = LoggerFactory.getLogger(PluginLoader.class.getSimpleName());

	public static List<Path> JARS = new ArrayList<>();

	public static void scan() throws MalformedURLException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		URLClassLoader loader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		File[] lst = HarbingerServer.PLUGINS.toFile().listFiles();
		boolean iNextJar=false;
		for(File f : lst)
		{
			URL jar = f.toURI().toURL();
			for(URL it : Arrays.asList(loader.getURLs()))
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
		}
	}

	public static void activate()
	{
		Class<?> clazz = Plugin.class;
		Class<?>[] cls = clazz.getClasses();
		List<IPlugin> plugins = new ArrayList<>();

		for(Class<?>  C : cls){
			Plugin plug = C.getAnnotation(Plugin.class);
			LOGGER.info("Activating plugin: "+plug.name());

			try {
				Constructor<IPlugin> Construct = (Constructor<IPlugin>) C.getConstructor();
				IPlugin plugin = Construct.newInstance();
				plugins.add(plugin);

				plugin.init();

				LOGGER.info("Plugin activated ["+plug.name()+"]");
			} catch (NoSuchMethodException |
					 InvocationTargetException |
					 InstantiationException |
					 IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
