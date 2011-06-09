/**
 * 
 */
package org.valhalla.tools.process;

import java.util.HashSet;
import java.util.Set;

/**
 * @author ccorsi
 *
 */
public class Options {
	
	public static final String PORT = "-p";
	public static final String HOST = "-h";
	public static final String CLASSNAME = "-c";
	public static final String METHODNAME =  "-m";
	public static final String PROPERTYFILE = "-P";
	
	public static final Set<String> options;
	
	static {
		options = new HashSet<String>();
		options.add(PORT);
		options.add(HOST);
		options.add(CLASSNAME);
		options.add(METHODNAME);
		options.add(PROPERTYFILE);
	}

}
