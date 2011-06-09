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
	public ClassLoader classLoader;

	/**
	 * 
	 */
	public SpawnerClassLoader(ServerSocket server) {
		super();
		this.server = server;
		this.classLoader = Thread.currentThread().getContextClassLoader();
	}

	/**
	 * @param parent
	 */
	public SpawnerClassLoader(ServerSocket server, ClassLoader parent) {
		super(parent);
		this.server = server;
		this.classLoader = Thread.currentThread().getContextClassLoader();
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

		private class ProcessRequest extends Thread {
			private Socket socket;
			
			ProcessRequest(Socket socket) {
				super();
				this.socket = socket;
			}
			
			public void run() {
				try {
					ObjectInputStream is = new ObjectInputStream(
							socket.getInputStream());
					Object object = null;
					try {
						object = is.readObject();
					} catch (ClassNotFoundException cnfe) {
						cnfe.printStackTrace();
						// FIXME: Send a request with a null class
						// information....but this should never happen...maybe
						// ignore this all together...
					}
					Request request = Request.class.cast(object);
					System.out.println("INSIDE run received request: " + request);
					Reply reply;
					switch (request.getType()) {
					case CLASS:
						System.out
								.println("Processing CLASS request for class: "
										+ request.getName());
						Class<?> clazz = null;
						try {
							clazz = SpawnerClassLoader.this.classLoader.loadClass(request.getName());
						} catch (ClassNotFoundException e) {
							e.printStackTrace(System.out);
						}
						reply = new Reply(request.getName(), clazz);
						System.out.println("Returning CLASS reply: " + reply);
						break;
					case RESOURCE:
						System.out.println("Processing RESOURCE request");
						URL url = SpawnerClassLoader.this.classLoader
								.getResource(request.getName());
						reply = new Reply(request.getName(), url);
						break;
					case RESOURCES:
						System.out.println("Processing RESOURCES request");
						Enumeration<URL> urls = SpawnerClassLoader.this
								.classLoader.getResources(request.getName());
						reply = new Reply(request.getName(), new SerializableEnumeration(urls));
						break;
					default:
						reply = new Reply(request.getName(), (URL) null);
						break;
					}
					ObjectOutputStream os = new ObjectOutputStream(
							socket.getOutputStream());
					System.out.println("SENDING reply: " + reply);
					os.writeObject(reply);
					System.out.println("SENT REPLY");
					socket.close();
					socket = null;
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if( socket != null ) {
						try {
							socket.close();
						} catch (IOException e) {
						}
					}
				}
			}
		}
		
		public void run() {
			boolean success = true;
			while(process) {
				try {
					System.out.println("Waiting for a request");
					Socket socket = SpawnerClassLoader.this.server.accept();
					new ProcessRequest(socket).start();
					success = true;
				} catch (IOException e) {
					e.printStackTrace();
					if (success == false) {
						// We might have to stop processing socket requests because of a bigger problem
						System.out.println("Multiple exceptions have been raised....");
					}
					success = false;
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
