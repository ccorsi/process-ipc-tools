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
package org.valhalla.tools.process;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Claudio Corsi
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
