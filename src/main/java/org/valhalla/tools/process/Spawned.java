/**
 * 
 */
package org.valhalla.tools.process;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

import org.valhalla.tools.process.classloader.SpawnedClassLoader;

/**
 * @author ccorsi
 *
 */
public class Spawned {

	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		System.out.println("INSIDE Spawned main");
		Spawned spawned = new Spawned();
		
		spawned.processArgs(args);

		spawned.execute();
		
	}

	private void execute() {
		Properties props = new Properties();
		System.out.println("Creating an instance of SpawnedClassLoader");
		SpawnedClassLoader classLoader = new SpawnedClassLoader(this.hostname,this.port);
		Thread.currentThread().setContextClassLoader(classLoader);
		try {
			props.load(new FileReader(new File(this.propertyfile)));
			// Remove all properties from the loaded properties that are part of the 
			// system properties...
			for(Object o : System.getProperties().keySet()) {
				props.remove(o);
			}
			// Add all remaining properties to the system properties....
			for( String key : props.stringPropertyNames() ) {
				System.out.println("Adding property key: " + key + " value: " + props.getProperty(key));
				System.setProperty(key, props.getProperty(key));
			}
			System.out.println("Getting a reference to the class " + className);
			Class<?> clazz = Class.forName(this.className);
			System.out.println("Found class: " + clazz);
			Object object = clazz.newInstance();
			System.out.println("Created instance: " + object);
			Method method = clazz.getMethod(methodname);
			System.out.println("Got reference to method: " + method);
			method.invoke(object);
			System.out.println("Invoked method");
		} catch (ClassNotFoundException e) {
			e.printStackTrace(System.out);
		} catch (InstantiationException e) {
			e.printStackTrace(System.out);
		} catch (IllegalAccessException e) {
			e.printStackTrace(System.out);
		} catch (SecurityException e) {
			e.printStackTrace(System.out);
		} catch (NoSuchMethodException e) {
			e.printStackTrace(System.out);
		} catch (IllegalArgumentException e) {
			e.printStackTrace(System.out);
		} catch (InvocationTargetException e) {
			e.printStackTrace(System.out);
		} catch (FileNotFoundException e) {
			e.printStackTrace(System.out);
		} catch (IOException e) {
			e.printStackTrace(System.out);
		}
	}

	private String className;
	private String hostname;
	private String methodname = "execute";
	private int port;
	private String propertyfile;

	private void processArgs(String[] args) {
		for(int idx = 0 ; idx < args.length ; idx++) {
			String arg = args[idx];
			if( arg.startsWith("-")) {
				if (Options.CLASSNAME.equals(arg)) {
					className = args[++idx];
					System.out.println("class name: " + className);
				} else if (Options.HOST.equals(arg)) {
					hostname = args[++idx];
					System.out.println("host name: " + hostname);
				} else if (Options.METHODNAME.equals(arg)) {
					methodname = args[++idx];
					System.out.println("method name: " + methodname);
				} else if (Options.PORT.equals(arg)) {
					port = Integer.parseInt(args[++idx]);
					System.out.println("port: " + port);
				} else if (Options.PROPERTYFILE.equals(arg)) {
					propertyfile = args[++idx];
					System.out.println("property file: " + propertyfile);
				} else {
					System.out.println("Unknow parameter: " + arg);
				}
			}
		}
	}

}
