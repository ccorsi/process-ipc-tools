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
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.SecureClassLoader;
import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * @author Claudio Corsi
 *
 */
public class SpawnedClassLoader extends SecureClassLoader {

	private String host;
	private int port;
	private boolean debug = false;

	public SpawnedClassLoader(String host, int port) {
		super();
		init(host,port);
	}
	
	public SpawnedClassLoader(String host, int port, ClassLoader parent) {
		super(parent);
		init(host, port);
	}
	
	private void init(String host, int port) {
		this.host = host;
		this.port = port;
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
	
	/* (non-Javadoc)
	 * @see java.lang.ClassLoader#findClass(java.lang.String)
	 */
	@Override
	protected Class<?> findClass(String className) throws ClassNotFoundException {
		try {
			println("INSIDE findClass - opening socket");
			Socket socket = new Socket(host, port);
			Request request = new Request(className, Type.CLASS);
			ObjectOutputStream os = new ObjectOutputStream( socket.getOutputStream() );
			System.out.println("INSIDE findClass - sending request: " + request);
			os.writeObject( request );
			ObjectInputStream is = new ObjectInputStream( socket.getInputStream() );
			println("INSIDE findClass - waiting for reply");
			Reply reply = Reply.class.cast( is.readObject() );
			println("INSIDE findClass - received reply: " + reply);
			return reply.getClassObject();
		} catch (UnknownHostException e) {
			throw new ClassNotFoundException(className, e);
		} catch (IOException e) {
			throw new ClassNotFoundException(className, e);
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.ClassLoader#findResource(java.lang.String)
	 */
	@Override
	protected URL findResource(String resourceName) {
		URL url = null;
		try {
			println("INSIDE findResource - opening socket");
			Socket socket = new Socket(host, port);
			Request request = new Request(resourceName, Type.RESOURCE);
			ObjectOutputStream os = new ObjectOutputStream(
					socket.getOutputStream());
			println("INSIDE findResource - sending request: " + request);
			os.writeObject(request);
			ObjectInputStream is = new ObjectInputStream(
					socket.getInputStream());
			println("INSIDE findResource - waiting for reply");
			Reply reply = Reply.class.cast(is.readObject());
			println("INSIDE findResource - received reply: " + reply);
			url = reply.getUrl();
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
		return url;
	}

	private static final Enumeration<URL> nullurls = new Enumeration<URL>() {

		public boolean hasMoreElements() {
			return false;
		}

		public URL nextElement() {
			throw new NoSuchElementException();
		}
		
	};
 
	/* (non-Javadoc)
	 * @see java.lang.ClassLoader#findResources(java.lang.String)
	 */
	@Override
	protected Enumeration<URL> findResources(String resourceName) throws IOException {
		Enumeration<URL> urls = nullurls;
		try {
			println("INSIDE findResources - opening socket");
			Socket socket = new Socket(host, port);
			Request request = new Request(resourceName, Type.RESOURCES);
			ObjectOutputStream os = new ObjectOutputStream(
					socket.getOutputStream());
			println("INSIDE findResources - sending request: " + request);
			os.writeObject(request);
			ObjectInputStream is = new ObjectInputStream(
					socket.getInputStream());
			println("INSIDE findResources - waiting for reply");
			Reply reply = Reply.class.cast(is.readObject());
			println("INSIDE findResources - received reply: " + reply);
			urls = reply.getUrls();
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
		return urls;
	}

}
