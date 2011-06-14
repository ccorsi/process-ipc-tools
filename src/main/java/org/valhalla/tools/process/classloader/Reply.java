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

import java.io.Serializable;
import java.net.URL;
import java.util.Enumeration;

/**
 * @author Claudio Corsi
 *
 */
public class Reply implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1588275222484513483L;

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the clazz
	 */
	public Class<?> getClassObject() {
		return clazz;
	}

	/**
	 * @return the url
	 */
	public URL getUrl() {
		return url;
	}

	/**
	 * @return the type
	 */
	public Type getType() {
		return type;
	}

	private String name;
	private Class<?> clazz;
	private URL url;
	private Type type;
	private Enumeration<URL> urls;
	
	public Reply(String name, Class<?> clazz) {
		this.name = name;
		this.clazz = clazz;
		this.type = Type.CLASS;
	}

	public Reply(String name, URL url) {
		this.name = name;
		this.url = url;
		this.type = Type.RESOURCE;
	}

	public Reply(String name, Enumeration<URL> urls) {
		this.name = name;
		this.urls = urls;
		this.type = Type.RESOURCES;
	}

	/**
	 * @return the urls
	 */
	public Enumeration<URL> getUrls() {
		return urls;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Reply [name=" + name + ", clazz=" + clazz + ", url=" + url
				+ ", type=" + type + ", urls=" + urls + "]";
	}

}
