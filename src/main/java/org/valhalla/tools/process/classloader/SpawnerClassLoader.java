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
package org.valhalla.tools.process.classloader;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.security.SecureClassLoader;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Claudio Corsi
 *
 */
public class SpawnerClassLoader extends SecureClassLoader {

	private static final Logger log = LoggerFactory.getLogger(SpawnerClassLoader.class);
	
	private SpawnerClassLoaderThread thread;
	private ServerSocket server;
//	private ClassLoader classLoader;
	private String identifier;

	/**
	 * 
	 */
	public SpawnerClassLoader(ServerSocket server, String identifier) {
		super();
		this.server = server;
//		this.classLoader = Thread.currentThread().getContextClassLoader();
		this.identifier = identifier;
	}

	/**
	 * @param parent
	 */
	public SpawnerClassLoader(ServerSocket server, ClassLoader parent, String identifier) {
		super(parent);
		this.server = server;
//		this.classLoader = Thread.currentThread().getContextClassLoader();
		this.identifier = identifier;
	}

	public void start() {
		log.info("Creating SpawnerClassLoader....");
		thread = new SpawnerClassLoaderThread();
		thread.setDaemon(true);
		thread.setName(this.identifier + "SpawnerClassLoader" + thread.getName());
		thread.start();
		log.info("Started SpawnerClassLoader...");
	}
	
	public void stop() {
		thread.stopProcessing();
	}
	
	class SpawnerClassLoaderThread extends Thread {
		
		private volatile boolean process = true;

		private class ProcessRequest extends Thread {
			private Socket socket;
			
			ProcessRequest(Socket socket) {
				super();
				this.socket = socket;
			}
			
			public void run() {
				try {
					ObjectInputStream is = new ObjectInputStream(
							socket.getInputStream());
					Object object = null;
					try {
						object = is.readObject();
					} catch (ClassNotFoundException cnfe) {
						log.error("An exception was raised while trying to read an object over the wire", cnfe);
						// FIXME: Send a request with a null class
						// information....but this should never happen...maybe
						// ignore this all together...
					}
					Request request = Request.class.cast(object);
					log.debug("INSIDE run received request: {}", request);
					Reply reply;
					switch (request.getType()) {
					case CLASS:
						log.debug("Processing CLASS request for class: {}",
								request.getName());
						Class<?> clazz = null;
						try {
							clazz = SpawnerClassLoader.this.loadClass(request.getName());
						} catch (ClassNotFoundException e) {
							log.error("An exception was raised while looking up class: {}", request.getName(), e);
							try {
								Thread.currentThread().getContextClassLoader().loadClass(request.getName());
								log.error("Was able to retreive class information for class {}",request.getName());
							} catch(ClassNotFoundException cnfe) {
								log.error("Still unable to get a reference to class {}", request.getName());
								// do nothing...
							}
						}
						reply = new Reply(request.getName(), clazz);
						log.debug("Returning CLASS reply: {}", reply);
						break;
					case RESOURCE:
						log.debug("Processing RESOURCE request");
						URL url = SpawnerClassLoader.this
								.getResource(request.getName());
						reply = new Reply(request.getName(), url);
						break;
					case RESOURCES:
						log.debug("Processing RESOURCES request");
						Enumeration<URL> urls = SpawnerClassLoader.this
								.getResources(request.getName());
						reply = new Reply(request.getName(), new SerializableEnumeration(urls));
						break;
					default:
						reply = new Reply(request.getName(), (URL) null);
						break;
					}
					ObjectOutputStream os = new ObjectOutputStream(
							socket.getOutputStream());
					log.debug("SENDING reply: {}", reply);
					os.writeObject(reply);
					log.debug("SENT REPLY");
					socket.close();
					socket = null;
				} catch (Exception e) {
					log.error("Received an exception while processing request", e);
				} finally {
					if( socket != null ) {
						try {
							socket.close();
						} catch (IOException e) {
						}
					}
				}
			}
		}
		
		public void run() {
			boolean success = true;
			while(process) {
				try {
					log.debug("Waiting for a request");
					Socket socket = SpawnerClassLoader.this.server.accept();
					new ProcessRequest(socket) {
						{
							this.setDaemon(true);
							setName(SpawnerClassLoader.this.identifier + getName());
							start();
						}
					};
					success = true;
				} catch (IOException e) {
					log.error("An exception was raised while waiting for a request", e);
					if (success == false) {
						// We might have to stop processing socket requests because of a bigger problem
						log.debug("Multiple exceptions have been raised....");
					}
					success = false;
				}
			}
		}
		
		public void stopProcessing() {
			this.process = false;
			// Interrupt ourselves to allow for the thread to shut itself down.
			this.interrupt();
		}
		
	}
}
