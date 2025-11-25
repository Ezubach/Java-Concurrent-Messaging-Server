package Clases;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.ArrayList;
import java.io.ObjectInputStream;

import Mensaje.*;
import Locks.*;

public class Cliente {

	public final static int MAX_DESCARGAS_SIMUL = 10;

	public final static int MAX_ENVIOS_SIMUL = 10;

	private Lock lockBakery = new LockBakery(2); // Solo lo van a usar el proceso Cliente y el hilo OyenteServidor
														// Para proteger el uso de la Seccion crítica de
														// outC.writeObject()
	
	
	private Lock lockRompEmpate = new LockRompeEmpate(MAX_DESCARGAS_SIMUL + MAX_ENVIOS_SIMUL + 2); //Lock para los system.out
	//Cliente hará takelock(0) y releaseLock(0)
	//OyenteServidor hará takelock(1) y releaseLock(1)
	//Para saber que id van a tomar los hilos EmisorP2P y ReceptorP2P vamos a hacer que vayan tomando tickets si hay y lo devuelvan
	//cuando acaben. Así simulamos un Productor Consumidor.
	//EmisorP2P hara takelock(i + 2) y releaseLock(i + 2) siendo i el numero de ticket que ha cogido para enviar datos
	//ReceptorP2P hara takeLock(i + 2 + MAX_ENVIOS_SIMUL) y releaseLock(i + 2 + MAX_ENVIOS_SIMUL)
	
	
	protected Usuario user;

	private ObjectInputStream inC;
	private ObjectOutputStream outC;

	private BufferedReader consoleInput;

	private ServerSocket ss;
	private Socket server;

	private volatile boolean conectado = false;

	private Semaphore conected = new Semaphore(0);

	public static void main(String[] args) throws IOException {
		
		Cliente cliente = new Cliente();
		cliente.ejecutar();

	}
	
	private void ejecutar() throws IOException {
		// Creo el ServerSocket al iniciar
		ss = new ServerSocket(0);

		// Creo el socket con el servidor
		server = new Socket("localhost", 999);

		// Creo los flujos con el servidor
		inC = new ObjectInputStream(server.getInputStream());
		outC = new ObjectOutputStream(server.getOutputStream());

		// Flujo para leer de consola
		consoleInput = new BufferedReader(new InputStreamReader(System.in));

		iniciarSesion(); // Se supone que ya tenemos el usuario registrado, solo queda enviar el mensaje
							// de conexion al servidor

		Mensaje connectMessage = new MensajeConexion(user); // Mensaje de conexion

		outC.writeObject(connectMessage); // Envio el mensaje de conexion al servidor con el usuario
		outC.flush();

		Thread oyente = new Thread(new OyenteServidor(inC, outC, user, ss, lockBakery, lockRompEmpate,this));
		oyente.start();

		// Espero un release de OyenteCliente que indicaría que ya ha llegado el mensaje
		// de confirmación de conexion.
		try {
			conected.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		menuOpciones();
		
	}

	private void menuOpciones() throws IOException {
		while (conectado) {

			// Muestro las opciones disponibles
			mostrarOpciones();

			// Leo la opcion
			String opcion = consoleInput.readLine();

			// Tratar las opciones
			tratarOpcion(opcion);

		}

	}

	private void mostrarOpciones() {

		lockRompEmpate.takeLock(0);
		System.out.println("Opciones disponibles:\n");
		System.out.println("1. Mostrar archivos disponibles");
		System.out.println("2. Descargar archivo");
		System.out.println("3. Salir");
		System.out.print("Seleccione una opcion: ");
		lockRompEmpate.releaseLock(0);
	}

	private void tratarOpcion(String op) throws IOException {
		switch (op) {
		case "1": { // Listar Archivos disponibles

			// Envia un mensaje al servidor para consultar la informacion de los usuarios
			// La informacion lo recibira el hilo OyenteServidor
			MensajeConsultarInfo m = new MensajeConsultarInfo();

			lockBakery.takeLock(0);
			// Seccion crítica pues OyenteServidor también accede a outC para escribir
			outC.writeObject(m);
			outC.flush();
			lockBakery.releaseLock(0);

			break;
		}
		case "2": {
			
			
			// Seccion critica en la consola
			lockRompEmpate.takeLock(0);
			System.out.println("Nombre del usuario que tiene el archivo: ");
			String solicitado = consoleInput.readLine();

			System.out.println("Introduzca el archivo que quiera descargar: ");
			String archivo = consoleInput.readLine();

			System.out.println("Introduzca el nombre del fichero que va a guardar: ");
			String guardar = consoleInput.readLine();
			lockRompEmpate.releaseLock(0);

			String solicitante = user.getId();
			// Envia la solicitud al servidor para descargar archivo
			MensajeSolicitarArchivo solicitud = new MensajeSolicitarArchivo(solicitante, solicitado, archivo, guardar);

			lockBakery.takeLock(0);
			// Seccion critica
			outC.writeObject(solicitud);
			outC.flush();
			lockBakery.releaseLock(0);


			break;
		}

		case "3": {

			MensajeDesconexion mensajeDesconexion = new MensajeDesconexion(user);

			lockBakery.takeLock(0);
			// Seccion critica
			outC.writeObject(mensajeDesconexion);
			outC.flush();
			lockBakery.releaseLock(0);

			try {
				conected.acquire();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// No es seccion critica porque se han cerrado todos los hilos en este punto
			System.out.println("\nFin de la transmision.");
			break;
		}
		default:
			System.out.println("Introduzca una opción válida.");
			break;
		}

	}

	private void iniciarSesion() throws IOException {
		System.out.print("Introduzca su nombre de usuario ");

		// Leo el nombre del usuario
		String nombreUsuario = consoleInput.readLine();

		String ip = InetAddress.getLocalHost().getHostAddress();
		// String ip = server.getInetAddress().getHostAddress();

		List<String> archivosCompartidos = leerArchivosCompartidos(nombreUsuario);

		System.out.println("Introduce la carpeta en la que se encuentra: ");
		String folder = consoleInput.readLine();

		// Directorio donde estará los ficheros compartidos del cliente en concreto

		user = new Usuario(nombreUsuario, ip, archivosCompartidos, folder); // Hay que cambiar puerto

	}

	public List<String> leerArchivosCompartidos(String name) throws IOException {
		List<String> archivos = new ArrayList<>();

		System.out.println("Introduce los nombres de archivos públicos (una línea por archivo).");
		System.out.println("Escribe una línea vacía para terminar:");

		while (true) {
			String linea = consoleInput.readLine().trim(); // <-- Usar consoleInput
			if (linea.isEmpty())
				break;
			archivos.add(linea);
		}

		return archivos;
	}

	// Funcion que contiene seccion critica
	public void conectar() {
		conectado = true;
		conected.release();
	}

	public void desconectar() {
		try {
			inC.close();
			outC.close();
			server.close();
			ss.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		conectado = false;
		conected.release();
	}
	
	public Usuario getUser() {
		return this.user;
	}

}
