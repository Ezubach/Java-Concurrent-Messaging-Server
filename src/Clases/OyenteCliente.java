package Clases;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import Auxiliares.Almacen;
import Auxiliares.Entero;
import Locks.Lock;
import Mensaje.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import Monitores.*;

public class OyenteCliente implements Runnable {

	private Socket s;
	private ObjectOutputStream outS;
	private ObjectInputStream inS;
	private boolean ejecutar;

	
	
	private Almacen almacen;
	
	private Entero serverTicket;
	
	
	private volatile Lock lock;
	
	private Servidor servidor;
	
	public OyenteCliente(Socket _s, Almacen almacen, Lock _lock, Servidor _servidor) throws IOException {
		
		this.s = _s;
		
		this.almacen = almacen;
		
		
		this.lock = _lock;


		// Creo los flujos de entrada y salida del socket para el servidor
		this.outS = new ObjectOutputStream(this.s.getOutputStream());
		this.inS = new ObjectInputStream(this.s.getInputStream());
		
		this.ejecutar = true;
		
		this.servidor = _servidor;
		
	}

	public void run() {

		while (ejecutar) {
			Mensaje m = null;
			try {
				m = (Mensaje) this.inS.readObject();

			} catch (ClassNotFoundException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			tratarMensajes(m);

		}

	}

	private void tratarMensajes(Mensaje m) {
		Mensaje.TipoMensaje type = m.getTipo();

		switch (type) {

		case CONEXION: {
			MensajeConexion c = (MensajeConexion) m;
			Usuario user = c.getUser(); // Obtengo el usuario

			//Aqui solicita el ticket al servidor con ProductorConsumidor de servidor
			//Intenta hacerlo en una funcion aparte
			

			solicitarTicket(); //Productor consumidor con semaforos


			// Ahora procedo a guardarlo en la base de datos del servidor
			//Escritura
			// ------------
			servidor.conectarUsuario(user);
			// ------------
			
			
			//Utilizo otro tipo de monitor, que es con Syncronize
			//Hago que el acceso a los flujos de entrada y salida esté controlado por este monitor
			servidor.agregarFlujos(user, this.outS, this.inS);

			
			

			// Mando el mensaje de confirmacion
			Mensaje MensajeConfirmacion = new MensajeConfirmacionConexion("Inicio de sesion con exito.");

			try {
				
				lock.takeLock(serverTicket.getValor());
				this.outS.writeObject(MensajeConfirmacion);
				this.outS.flush();
				lock.releaseLock(serverTicket.getValor());
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			
			System.out.println("Conexion aceptada de " + user.getId() + " con IP " + user.getIp()
			+ " con ticket " + serverTicket.getValor());
			
			break;
		}
		case DESCONEXION: {
			//Recibe un mensaje de desconexion y envia una confirmacion de desconexion y se desconecta
			MensajeDesconexion d = (MensajeDesconexion) m;
			Usuario u = d.getUuario();
			
			
			//Escritura (Aunque sea borrado)
			servidor.desconectarUsuario(u);
			

		
			servidor.eliminarFlujos(u, this.outS, this.inS);
			
			
			Mensaje mensajeConfDesc = new MensajeConfirmacionDesconexion();
			
			try {
				
				lock.takeLock(serverTicket.getValor());
				//Seccion critica
				this.outS.writeObject(mensajeConfDesc);
				this.outS.flush();
				lock.releaseLock(serverTicket.getValor());
				//-------------
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			System.out.println("Desconexcion aceptada de " + u.getId() + " con ticket " + serverTicket.getValor());
			
			//Devuelvo el ticket despues de haber enviado el mensaje de confirmacion
			devolverTicket();
			
			//Hago que se termine el hilo
			ejecutar = false;
			
			
			
			break;
		}
		
		case CONSULTAR_INFO: {
			//El servidor recibe una solicitud para consultar la informacion que tiene almacenada
			
			//Lectura
			Map<String, List<String>> listaArchivos = servidor.getArchivos();
			//----------------
			
			MensajeConfirmacionInfo ci = new MensajeConfirmacionInfo(listaArchivos);
			
			try {
				lock.takeLock(serverTicket.getValor());
				//Sección crítica
				this.outS.writeObject(ci);
				this.outS.flush();
				lock.releaseLock(serverTicket.getValor());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

			break;
		}
		
		case SOLICITAR_ARCHIVO: {
			//Un cliente solicita descargar un archivo
			
			MensajeSolicitarArchivo sa = (MensajeSolicitarArchivo) m;
			
			String solicitante = sa.getSolicitante();
			String solicitado = sa.getSolicitado();
			String archivo = sa.getArchivo();
			String nombreDeDescarga = sa.getNombreDeDescarga();
			
			
			ObjectOutputStream output = servidor.getFlujoSalida(solicitado);
			
			
			if (output != null) { //Existe el usuario solicitado
				MensajePrepararEnvio pe = new MensajePrepararEnvio(solicitante, archivo, nombreDeDescarga);
				
				try {
					
					lock.takeLock(serverTicket.getValor());
					//Seccion crítica, el OyenteCliente de este usuario podría estar usando el canal de salida
					output.writeObject(pe);
					lock.releaseLock(serverTicket.getValor());
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			}
			
			break;
		}

		case EMISOR_PREPARADO: {
			// El emisor avisa al servidor que ya tiene su canal creado

			// El servidor avisa al receptor del canal
			// PREPARAR_RECEPCION
			
			MensajeEmisorPreparado ep = (MensajeEmisorPreparado) m;
			
			
			ObjectOutputStream output = servidor.getFlujoSalida(ep.getReceptor());
			
			
			MensajePrepararRecepcion pe = new MensajePrepararRecepcion(ep.getIp(), ep.getPort(), 
													ep.getArchivo(), ep.getNombreDeDescarga());
			
			try {
				lock.takeLock(serverTicket.getValor());
				//Seccion crítica, el OyenteCliente de este usuario podría estar usando el canal de salida
				output.writeObject(pe);
				output.flush();
				lock.releaseLock(serverTicket.getValor());
			} catch (IOException e) {
				
				e.printStackTrace();
			}

			break;
		}

		default: {
			break;
		}
		}
	}
	
	private void solicitarTicket() {
		/* Solicitamos un ticket en el almacen el servidor
		 * donde el numero del ticket servira como identificador del usuario dentro del servidor
		 * para el tema de locks y demas*/
		

		serverTicket = almacen.extraer();

	}
	
	private void devolverTicket() {
		almacen.almacenar(serverTicket); //Almacen ya tiene la protección de la seccion crítica
		serverTicket = null;

	}
	
}
