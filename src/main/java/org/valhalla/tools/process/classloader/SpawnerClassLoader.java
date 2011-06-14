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
	public ClassLoader classLoader;

	/**
	 * 
	 */
	public SpawnerClassLoader(ServerSocket server) {
		super();
		this.server = server;
		this.classLoader = Thread.currentThread().getContextClassLoader();
	}

	/**
	 * @param parent
	 */
	public SpawnerClassLoader(ServerSocket server, ClassLoader parent) {
		super(parent);
		this.server = server;
		this.classLoader = Thread.currentThread().getContextClassLoader();
	}

	public void start() {
		log.info("Creating SpawnerClassLoader....");
		thread = new SpawnerClassLoaderThread();
		thread.start();
		log.info("Started SpawnerClassLoader...");
	}
	
	public void stop() {
		thread.stopProcessing();
	}
	
	class SpawnerClassLoaderThread extends Thread {
		
		private boolean process = true;

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
						log.debug("An exception was raised while trying to read an object over the wire", cnfe);
						// FIXME: Send a request with a null class
						// information....but this should never happen...maybe
						// ignore this all together...
					}
					Request request = Request.class.cast(object);
					log.info("INSIDE run received request: {}", request);
					Reply reply;
					switch (request.getType()) {
					case CLASS:
						log.info("Processing CLASS request for class: {}",
								request.getName());
						Class<?> clazz = null;
						try {
							clazz = SpawnerClassLoader.this.classLoader.loadClass(request.getName());
						} catch (ClassNotFoundException e) {
							log.debug("An exception was raised while looking up class: {}", request.getName());
						}
						reply = new Reply(request.getName(), clazz);
						log.info("Returning CLASS reply: {}", reply);
						break;
					case RESOURCE:
						log.info("Processing RESOURCE request");
						URL url = SpawnerClassLoader.this.classLoader
								.getResource(request.getName());
						reply = new Reply(request.getName(), url);
						break;
					case RESOURCES:
						log.info("Processing RESOURCES request");
						Enumeration<URL> urls = SpawnerClassLoader.this
								.classLoader.getResources(request.getName());
						reply = new Reply(request.getName(), new SerializableEnumeration(urls));
						break;
					default:
						reply = new Reply(request.getName(), (URL) null);
						break;
					}
					ObjectOutputStream os = new ObjectOutputStream(
							socket.getOutputStream());
					log.info("SENDING reply: {}", reply);
					os.writeObject(reply);
					log.info("SENT REPLY");
					socket.close();
					socket = null;
				} catch (Exception e) {
					log.debug("Received an exception while processing request", e);
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
					log.info("Waiting for a request");
					Socket socket = SpawnerClassLoader.this.server.accept();
					new ProcessRequest(socket).start();
					success = true;
				} catch (IOException e) {
					log.info("An exception was raised while waiting for a request", e);
					if (success == false) {
						// We might have to stop processing socket requests because of a bigger problem
						log.info("Multiple exceptions have been raised....");
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
