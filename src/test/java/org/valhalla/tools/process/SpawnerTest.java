package org.valhalla.tools.process;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple Spawner.
 */
public class SpawnerTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public SpawnerTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( SpawnerTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
    	System.out.println(new File("."));
        assertTrue( true );
    }
    
    public void testSpawnedClassLoader() throws Exception {
    	System.setProperty("spawned.property", "I'M SET");
    	System.out.println("INSIDE testSpawnedClassLoader");
    	Spawner spawner = new Spawner(RunMeExecute.class.getName());
    	spawner.setSpawnedClassPath("target" + File.separator + "classes");
    	spawner.spawnProcess();
    	System.out.println("EXITING testSpawnedClassLoader");
    }
}
