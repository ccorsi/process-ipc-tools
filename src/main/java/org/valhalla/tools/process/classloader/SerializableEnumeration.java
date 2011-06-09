/**
 * 
 */
package org.valhalla.tools.process.classloader;

import java.io.Serializable;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author ccorsi
 *
 */
public class SerializableEnumeration implements Enumeration<URL>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6842411400169149712L;
	
	private List<URL> urls = new LinkedList<URL>();
	private Iterator<URL> iterator;
	
	public SerializableEnumeration(Enumeration<URL> urls) {
		while(urls.hasMoreElements()) {
			this.urls.add(urls.nextElement());
		}
	}
	
	private void checkIterator() {
		if (this.iterator == null) {
			this.iterator = this.urls.iterator();
		}
	}
	
	@Override
	public boolean hasMoreElements() {
		checkIterator();
		return this.iterator.hasNext();
	}

	@Override
	public URL nextElement() {
		checkIterator();
		return this.iterator.next();
	}

}
