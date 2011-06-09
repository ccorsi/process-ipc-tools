/**
 * 
 */
package org.valhalla.tools.process.classloader;

import java.io.Serializable;
import java.net.URL;
import java.util.Enumeration;

/**
 * @author ccorsi
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
