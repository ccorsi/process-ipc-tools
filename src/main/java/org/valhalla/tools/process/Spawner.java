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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.valhalla.tools.process.classloader.SpawnerClassLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class will spawn a new process and will execute the main method for the passed class.  
 * It can also execute a given method but will require that the class contain a no-arg 
 * constructor.
 * 
 * @author Claudio Corsi
 */
public class Spawner 
{
	private static final Logger log = LoggerFactory.getLogger(Spawner.class);
	
	private String className;
	private String methodName;
	private SpawnerClassLoader classLoader;
	private String spawnedClassPath = "";
	private Process process;
	private ServerSocket server;

	public Spawner(String className) {
		this(className,null);
	}
	
	public Spawner(String className, String methodName) {
		this.className = className;
		this.methodName = methodName;
	}
	
	public void spawnProcess() throws IOException, InterruptedException {
		log.info("INSIDE spawnProcess");
		// Setup the classloader and determine the port used to process the incoming requests.
		String javaHome = System.getProperty("java.home");
		String javaExe = javaHome + File.separator + "bin" + File.separator + "java";
		server = new ServerSocket(0);
		int port = server.getLocalPort();
		String hostname = InetAddress.getLocalHost().getHostName();
		ClassLoader parent = Thread.currentThread().getContextClassLoader();
		if(parent == null) {
			classLoader = new SpawnerClassLoader(server);
		} else {
			classLoader = new SpawnerClassLoader(server,parent);
		}
		classLoader.start();
		List<String> command = new LinkedList<String>();
		command.add(javaExe);
		command.add("-cp");
		command.add(System.getProperty("java.class.path") + File.pathSeparator + spawnedClassPath);
		command.add(Spawned.class.getName());
		command.add(Options.PORT);
		command.add(String.valueOf(port));
		command.add(Options.HOST);
		command.add(hostname);
		command.add(Options.CLASSNAME);
		command.add(className);
		if (methodName != null) {
			command.add(Options.METHODNAME);
			command.add(methodName);
		}
		// Store the system properties to a file that will be loaded by the spawned application.
		Properties props = System.getProperties();
		File file = File.createTempFile("spawned", ".properties");
		Writer out = new FileWriter(file);
		props.store(out, "Spawned properties");
		out.close();
		command.add(Options.PROPERTYFILE);
		command.add(file.getAbsolutePath());
		log.info("command: {}", command);
		ProcessBuilder processBuilder = new ProcessBuilder(command);
//		processBuilder.redirectErrorStream();
		process = processBuilder.start();
		final BufferedReader input = new BufferedReader( new InputStreamReader( process.getInputStream() ) );
		final BufferedReader error = new BufferedReader( new InputStreamReader( process.getErrorStream() ) );
		Thread thread = new Thread( new Runnable() {
			public void run() {
				log.info("Inside run");
				String line;
				try {
					while((line = input.readLine()) != null) {
						log.info("stdout: {}", line);
					}
				} catch (IOException e) {
					log.debug("An exception was raised while trying to read the next line", e);
				}
			}
		});
		thread.start();
		thread = new Thread( new Runnable() {
			public void run() {
				log.info("Inside run");
				String line;
				try {
					while((line = error.readLine()) != null) {
						log.info("stderr: {} ", line);
					}
				} catch (IOException e) {
					log.debug("An exception was raised while trying to read the next line", e);
				}
			}
		});
		thread.start();
		int result = process.waitFor();
		log.info("Exiting process with: {}", result);
	}
	
	public void exitProcess() {
		// Just to get rid of the warning....
		classLoader.getClass();
		// Send a request to shutdown the spawned process and return...
		classLoader = null;
	}
	
    /**
	 * @param spawnedClassPath the spawnedClassPath to set
	 */
	public void setSpawnedClassPath(String spawnedClassPath) {
		this.spawnedClassPath = spawnedClassPath;
	}

	public static void main( String[] args )
    {
        log.info( "Hello World!" );
    }
}
