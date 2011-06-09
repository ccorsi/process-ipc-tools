/**
 * 
 */
package org.valhalla.tools.process.classloader;

/**
 * @author ccorsi
 *
 */
public class Request {
	
	private String name;
	private Type type;
	
	public Request(String name, Type type) {
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}
	
	public Type getType() {
		return type;
	}
	
}
