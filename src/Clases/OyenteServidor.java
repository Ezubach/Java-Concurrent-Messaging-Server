package Clases;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import Locks.Lock;
import Mensaje.*;
import Monitores.MNProductLock;
import Monitores.MonitorProdCons;
import Monitores.MNProductSynch;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.ArrayList;

public class OyenteServidor implements Runnable {

	private ServerSocket ss;

	// Flujos con el servidor
	private ObjectInputStream inC;
	private ObjectOutputStream outC;

	private Usuario user;

//	private List<Thread> threads = new ArrayList<>();
	
	private boolean ejecutar = true;
	
	private Lock lockBakery;
	
	private Lock lockRompEmpate;
	
	private Cliente cliente;
	
	private MonitorProdCons ticketsEmisor = new MNProductSynch(Cliente.MAX_ENVIOS_SIMUL, Cliente.MAX_ENVIOS_SIMUL);
	private MonitorProdCons ticketsDescargas = new MNProductLock(Cliente.MAX_DESCARGAS_SIMUL, Cliente.MAX_DESCARGAS_SIMUL);

	public OyenteServidor(ObjectInputStream in, ObjectOutputStream out, Usuario _user, ServerSocket ss, 
			 Lock lockBakery, Lock rompeEmpate, Cliente _cliente) {

		this.inC = in;
		this.outC = out;

		this.ss = ss;

		this.user = _user;
		
		this.lockBakery = lockBakery;
		this.lockRompEmpate = rompeEmpate;
		this.cliente = _cliente;
	}

	public void run() {

		while (ejecutar) {
			Mensaje m = null;
			try {
				m = (Mensaje) inC.readObject();
			} catch (ClassNotFoundException | IOException e) {
				
				e.printStackTrace();
			}

			Mensaje.TipoMensaje type = m.getTipo();
			switch (type) {
			case CONFIRMACION_CONEXION: {
				// Recibe que se ha conectado correctamente al servidor, por lo que ahora puede
				// ejecutar
				// Es un mensaje de confirmacion de conexion;
				MensajeConfirmacionConexion confirm = (MensajeConfirmacionConexion) m;

				String l = confirm.getMensaje();
				
				//No es seccion crítica pues Cliente espera a que se conecte antes de continuar
				System.out.println(l);

				
				cliente.conectar();

				break;

			}
			case CONFIRMACION_DESCONEXION: {
				// Recibe la confirmacion de la desconexion, entonces el cliente puede
				// desconectarse del servidor,
				
				
				cliente.desconectar();
				ejecutar = false;
				
				
				break;
			}
			case CONFIRMACION_INFO: {
				// Aqui recibe del Servidor la informacion solicitada y lo muestra en la consola
				MensajeConfirmacionInfo ci = (MensajeConfirmacionInfo) m;
				Map<String, List<String>> listado = ci.getLista();

				
				mostrarArchivos(listado);
				

				break;
			}
			case PREPARAR_ENVIO: {
				// El servidor avisa de que un cliente ha solicitado un archivo
				// Aqui creo el hilo que se encargara de enviar el archivo

				MensajePrepararEnvio pe = (MensajePrepararEnvio) m;

				String archivo = pe.getArchivo();
				String receptor = pe.getReceptor();
				String nombreDeDescarga = pe.getNombreDeDescarga();

				int puerto = prepararEnvio(archivo);

				// Enviar mensaje al servidor de que ya ha preparado el socket para enviar
				MensajeEmisorPreparado ep = new MensajeEmisorPreparado(cliente.getUser().getIp(), puerto, 
						archivo, receptor, nombreDeDescarga);
				
				try {
					
					this.lockBakery.takeLock(1);
					//Seccion crítica, pues Cliente puede estar enviando algo
					outC.writeObject(ep);
					this.lockBakery.releaseLock(1);
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				break;
			}
			case PREPARAR_RECEPCION: {
				// El servidor ya tiene al emisor preparado para enviar, aqui se avisa al
				// receptor para que se prepare para
				// recibir
				MensajePrepararRecepcion pr = (MensajePrepararRecepcion) m;

				// Aqui creo otro hilo para en el cual se comunica directamente con el emisor
				// para descargar

				prepararRecepcion(pr.getIp(), pr.getPort(), pr.getArchivo(), pr.getNombreDeDescarga());

				break;
			}

			default:
				// Mensaje incorrecto
				break;
			}

		}

	}

	private int prepararEnvio(String archivo) {
		Thread oyente = new Thread(new EmisorP2P(ss, archivo, user.getFolder(), lockRompEmpate, ticketsEmisor));
		oyente.start();

		return ss.getLocalPort();
	}

	private void prepararRecepcion(String emisorIP, int port, String archivo, String nombreDeDescarga) {
		
		Thread oyente = new Thread(new ReceptorP2P(emisorIP, port, archivo, user.getFolder(), 
				user.getId(), nombreDeDescarga, lockRompEmpate, ticketsDescargas));
		
		oyente.start();

	}

	private void mostrarArchivos(Map<String, List<String>> usuarios) {

		StringBuilder sb = new StringBuilder();

		sb.append("----------\n");
		sb.append("Listado de archivos de los usuarios del servidor: \n");

		for (Entry<String, List<String>> usuario : usuarios.entrySet()) {

			String userId = usuario.getKey();

			List<String> archivos = usuario.getValue();

			sb.append("\n");

			sb.append("Usuario: ").append(userId).append('\n');

			for (String archivo : archivos) {
				sb.append(archivo).append('\n');
			}

		}
		sb.append("----------\n");
		
		//Seccion critica
		lockRompEmpate.takeLock(1);
		System.out.println(sb.toString());
		lockRompEmpate.releaseLock(1);
	}

}
