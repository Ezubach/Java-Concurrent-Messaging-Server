package Clases;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;

import Auxiliares.Entero;
import Locks.Lock;
import Mensaje.*;
import Monitores.MonitorProdCons;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;

public class EmisorP2P implements Runnable {
	private ServerSocket socket;
	private String archivo;
	private String folder;
	
	private Lock lockRompEmpate;
	private MonitorProdCons ticketsEmisor;
	
	private Entero ticket;

	public EmisorP2P(ServerSocket _socket, String archivo, String _folder, Lock lockRompEmpate,
			MonitorProdCons ticketsEmisor) {
		this.socket = _socket;
		this.archivo = archivo;
		this.folder = _folder;
		this.lockRompEmpate = lockRompEmpate;
		this.ticketsEmisor = ticketsEmisor;
	}

	public void run() {
		
		ticket = ticketsEmisor.extraer();
		
		try (Socket s = socket.accept();
				ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(s.getInputStream())) {
			
			
			lockRompEmpate.takeLock(ticket.getValor() + 2);
			System.out.println("EmisorP2P escuchando en puerto " + socket.getLocalPort());
			System.out.println("Cliente conectado al EmisorP2P");
			lockRompEmpate.releaseLock(ticket.getValor() + 2);

			boolean conectado = true;

			while (conectado) {
				Mensaje m = (Mensaje) in.readObject();
				Mensaje.TipoMensaje type = m.getTipo();

				switch (type) {
				case DESCARGAR_ARCHIVO:
					StringBuilder contenidoArchivo = new StringBuilder();
					try (BufferedReader reader = new BufferedReader(new FileReader(folder + "/" + archivo))) {
						String linea;
						while ((linea = reader.readLine()) != null) {
							contenidoArchivo.append(linea).append("\n");
						}
					}

					MensajeEnviarArchivo mensajeArchivo = new MensajeEnviarArchivo(contenidoArchivo.toString());
					out.writeObject(mensajeArchivo);
					out.flush();
					
					
					
					//Seccion critica
					lockRompEmpate.takeLock(ticket.getValor() + 2);
					System.out.println("Archivo enviado correctamente: " + archivo);
					lockRompEmpate.releaseLock(ticket.getValor() + 2);
					
					break;

				case DESCONEXION_CON_CLIENTE:
					MensajeConfirmacionDescConCliente cdc = new MensajeConfirmacionDescConCliente();
					
					out.writeObject(cdc);
					out.flush();
					
					
					conectado = false; 
					
					
					break;

				default:
					System.out.println("Tipo de mensaje no reconocido: " + type);
					break;
				}
			}

		} catch (IOException | ClassNotFoundException e) {
			System.err.println("Error en EmisorP2P:");
			e.printStackTrace();
		}
		
		//Devuelvo el ticket
		ticketsEmisor.almacenar(ticket);
		ticket = null;
	}
}
