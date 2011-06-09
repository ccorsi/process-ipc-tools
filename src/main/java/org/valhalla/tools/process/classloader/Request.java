/**
 * 
 */
package org.valhalla.tools.process.classloader;

import java.io.Serializable;

/**
 * @author ccorsi
 *
 */
public class Request implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5222820364266315059L;
	
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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Request [name=" + name + ", type=" + type + "]";
	}
	
}
