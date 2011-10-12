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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

/**
 * Unit test for simple Spawner.
 */
public class SpawnerTest 
{

    /**
     * Rigourous Test :-)
     */
    @Test public void testApp()
    {
    	System.out.println(new File(".").getAbsolutePath());
        assertTrue( true );
    }
    
    @Test public void testSpawnedClassLoader() throws Exception {
    	System.setProperty("spawned.property", "I'M SET");
    	System.out.println("INSIDE testSpawnedClassLoader");
    	Spawner spawner = new Spawner(RunMeExecute.class.getName());
    	spawner.setSpawnedClassPath("target" + File.separator + "classes");
		spawner.setIdentifier("SpawnedClassLoader");
    	spawner.spawnProcess();
    	Thread.sleep(5000);
    	System.out.println("EXITING testSpawnedClassLoader");
    }
    
    @Test public void testSetJVMArgs() throws Exception {
    	Spawner spawner = new Spawner(RunMeExecute.class.getName());
    	List<String> jvmArgs = new LinkedList<String>();
    	jvmArgs.add("-Xms1gb");
    	jvmArgs.add("-Xmx2gb");
		spawner.setJVMArgs(jvmArgs);
		spawner.setIdentifier("SetJVMArgs");
    	spawner.spawnProcess();
    	Thread.sleep(5000);
    }
}
