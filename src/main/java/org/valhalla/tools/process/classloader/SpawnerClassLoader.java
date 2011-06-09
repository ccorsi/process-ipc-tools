/**
 * 
 */
package org.valhalla.tools.process.classloader;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.security.SecureClassLoader;
import java.util.Enumeration;

/**
 * @author ccorsi
 *
 */
public class SpawnerClassLoader extends SecureClassLoader {

	private SpawnerClassLoaderThread thread;
	private ServerSocket server;

	/**
	 * 
	 */
	public SpawnerClassLoader(ServerSocket server) {
		super();
		this.server = server;
	}

	/**
	 * @param parent
	 */
	public SpawnerClassLoader(ServerSocket server, ClassLoader parent) {
		super(parent);
		this.server = server;
	}

	public void start() {
		System.out.println("Creating SpawnerClassLoader....");
		thread = new SpawnerClassLoaderThread();
		thread.start();
		System.out.println("Started SpawnerClassLoader...");
	}
	
	public void stop() {
		thread.stopProcessing();
	}
	
	class SpawnerClassLoaderThread extends Thread {
		
		private boolean process = true;

		public void run() {
			boolean success = true;
			while(process) {
				Socket socket = null;
				try {
					System.out.println("Waiting for a request");
					socket = SpawnerClassLoader.this.server.accept();
					ObjectInputStream is = new ObjectInputStream( socket.getInputStream() );
					Object object = null;
					try {
						object = is.readObject();
					} catch (ClassNotFoundException cnfe) {
						cnfe.printStackTrace();
						// FIXME: Send a request with a null class information....but this should never happen...maybe ignore this all together...
					}
					Request request = Request.class.cast(object);
					Reply reply;
					switch (request.getType()) {
					case CLASS:
						System.out.println("Processing CLASS request for class: " + request.getName());
						Class<?> clazz = null;
						try {
							clazz = SpawnerClassLoader.this.getParent().loadClass(request.getName());
						} catch (ClassNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						reply = new Reply(request.getName(),clazz);
						System.out.println("Returning CLASS reply with class: " + reply.getClass());
						break;
					case RESOURCE:
						URL url = SpawnerClassLoader.this.getParent().getResource(request.getName());
						reply = new Reply(request.getName(),url);
						break;
					case RESOURCES:
						Enumeration<URL> urls = SpawnerClassLoader.this.getParent().getResources(request.getName());
						reply = new Reply(request.getName(),urls);
						break;
					default:
						reply = new Reply(request.getName(),(URL)null);
						break;
					}
					ObjectOutputStream os = new ObjectOutputStream( socket.getOutputStream() );
					os.writeObject(reply);
					success = true;
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
					if (success == false) {
						// We might have to stop processing socket requests because of a bigger problem
						System.out.println("Multiple exceptions have been raised....");
					}
					success = false;
				} finally {
					if (socket != null) {
						try {
							socket.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		
		public void stopProcessing() {
			this.process = false;
			// Interrupt ourselves to allow for the thread to shut itself down.
			this.interrupt();
		}
		
	}
}
