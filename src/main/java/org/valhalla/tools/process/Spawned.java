/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * @author Claudio Corsi
 *
 */
public class Spawned {

	private boolean debug = false;
	
	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		Spawned spawned = new Spawned();
		
		spawned.processArgs(args);

		spawned.execute();
		
	}

	public Spawned() {
		println("INSIDE Spawned main");
	}

	private void setProperties() {
		debug = Boolean.parseBoolean(System.getProperty("spawned.debug"));
	}

	/**
	 * @param message
	 */
	private void println(String message) {
		if (debug) {
			System.out.println(message);
		}
	}

	/**
	 * @param throwable
	 */
	private void printStackTrace(Throwable throwable) {
		if (debug) {
			throwable.printStackTrace(System.out);
		}
	}
	
	private void execute() {
		Properties props = new Properties();
		if (this.noClassLoader == false) {
			println("Creating an instance of SpawnedClassLoader");
			SpawnedClassLoader classLoader = new SpawnedClassLoader(
					this.hostname, this.port);
			Thread.currentThread().setContextClassLoader(classLoader);
		}
		try {
			props.load(new FileReader(new File(this.propertyfile)));
			// Remove all properties from the loaded properties that are part of the 
			// system properties...
			for(Object o : System.getProperties().keySet()) {
				props.remove(o);
			}
			// Add all remaining properties to the system properties....
			for( String key : props.stringPropertyNames() ) {
				println("Adding property key: " + key + " value: " + props.getProperty(key));
				System.setProperty(key, props.getProperty(key));
			}
			// Set properties....
			setProperties();
			println("Getting a reference to the class " + className);
			Class<?> clazz = Class.forName(this.className);
			println("Found class: " + clazz);
			Object object = clazz.newInstance();
			println("Created instance: " + object);
			Method method = clazz.getMethod(methodname);
			println("Got reference to method: " + method);
			method.invoke(object);
			println("Invoked method");
		} catch (ClassNotFoundException e) {
			printStackTrace(e);
		} catch (InstantiationException e) {
			printStackTrace(e);
		} catch (IllegalAccessException e) {
			printStackTrace(e);
		} catch (SecurityException e) {
			printStackTrace(e);
		} catch (NoSuchMethodException e) {
			printStackTrace(e);
		} catch (IllegalArgumentException e) {
			printStackTrace(e);
		} catch (InvocationTargetException e) {
			printStackTrace(e);
		} catch (FileNotFoundException e) {
			printStackTrace(e);
		} catch (IOException e) {
			printStackTrace(e);
		}
		
		try {
			Thread.sleep(2000);  // Give it a chance to have the spawner class time to read the output.
		} catch (InterruptedException e) {
			printStackTrace(e);
		}
	}

	private String className;
	private String hostname;
	private String methodname = "execute";
	private int port;
	private String propertyfile;
	private boolean noClassLoader = false;

	private void processArgs(String[] args) {
		for(int idx = 0 ; idx < args.length ; idx++) {
			String arg = args[idx];
			if(arg.startsWith("-")) {
				if (Options.CLASSNAME.equals(arg)) {
					className = args[++idx];
					println("class name: " + className);
				} else if (Options.HOST.equals(arg)) {
					hostname = args[++idx];
					println("host name: " + hostname);
				} else if (Options.METHODNAME.equals(arg)) {
					methodname = args[++idx];
					println("method name: " + methodname);
				} else if (Options.PORT.equals(arg)) {
					port = Integer.parseInt(args[++idx]);
					println("port: " + port);
				} else if (Options.PROPERTYFILE.equals(arg)) {
					propertyfile = args[++idx];
					println("property file: " + propertyfile);
				} else if (Options.NOCLASSLOADER.equals(arg)) {
					noClassLoader = true;
					println("no remote classloader required");
				} else {
					println("Unknow parameter: " + arg);
				}
			}
		}
	}

}
