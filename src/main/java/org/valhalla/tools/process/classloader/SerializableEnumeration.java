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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * This class replaces the transient version that is returned by the findResources call.
 * 
 * This class will allow us to transport the results over the wire and be applied within
 * the spawned process.
 * 
 * @author Claudio Corsi
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
