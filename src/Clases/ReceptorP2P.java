package Clases;


import java.io.BufferedWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;

import Auxiliares.Entero;
import Locks.Lock;
import Mensaje.*;
import Monitores.MonitorProdCons;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ReceptorP2P implements Runnable {
	private String emisorIP;
	private int puerto;
	private String archivo;
	private String folder;
	
	private String nombreDeDescarga;
	
	private String id;
	
	private Lock lockRompEmpate;
	private MonitorProdCons ticketsDescargas;
	
	private Entero ticket;
	

	public ReceptorP2P(String ip, int puerto, String _archivo, String _folder, 
			String _id, String nombreDeDescarga, Lock lockRompEmpate, MonitorProdCons ticketsDescargas ) {
		this.emisorIP = ip;
		this.puerto = puerto;
		this.folder = _folder;
		this.archivo = _archivo;
		this.id = _id;
		this.nombreDeDescarga = nombreDeDescarga;
		this.lockRompEmpate = lockRompEmpate;
		this.ticketsDescargas = ticketsDescargas;
	}

	public void run() {
		
		ticket = ticketsDescargas.extraer();
		
		boolean conectado = true;

		try (Socket s = new Socket(emisorIP, puerto);
				ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(s.getInputStream())) {
			
			lockRompEmpate.takeLock(ticket.getValor() + 2 + Cliente.MAX_ENVIOS_SIMUL);
			System.out.println("Conectado a EmisorP2P en " + emisorIP + ":" + puerto);
			lockRompEmpate.releaseLock(ticket.getValor() + 2 + Cliente.MAX_ENVIOS_SIMUL);
			
			//Mando mensaje al emisor para que empiece a enviar el archivo
			out.writeObject(new MensajeDescargarArchivo());
			out.flush();

			while (conectado) {
				Mensaje m = (Mensaje) in.readObject();

				switch (m.getTipo()) {
				case ENVIAR_ARCHIVO:
					// Recuperar el contenido que viene dentro del mensaje
					MensajeEnviarArchivo mensajeArchivo = (MensajeEnviarArchivo) m;
					String contenido = mensajeArchivo.getContenido();

					String rutaCompleta = this.folder + "/" + this.nombreDeDescarga;
					try (BufferedWriter writer = new BufferedWriter(new FileWriter(rutaCompleta))) {
						writer.write(contenido);
					}
					
					
					
					//Seccion crítica uso de la consola
					lockRompEmpate.takeLock(ticket.getValor() + 2 + Cliente.MAX_ENVIOS_SIMUL);
					System.out.println("Archivo recibido y guardado como: " + rutaCompleta);
					lockRompEmpate.releaseLock(ticket.getValor() + 2 + Cliente.MAX_ENVIOS_SIMUL);


					out.writeObject(new MensajeDescConCliente());
					out.flush();
					
					break;

				case CONFIRMACION_DESCONEXION_CON_CLIENTE:
					//System.out.println("Confirmación de desconexión recibida del EmisorP2P");
					conectado = false;

					break;

				default:
					System.out.println("Tipo de mensaje no reconocido: " + m.getTipo());
					break;
				}
			}

		} catch (IOException | ClassNotFoundException e) {
			System.err.println("Error en ReceptorP2P:");
			e.printStackTrace();
		}
		
		ticketsDescargas.almacenar(ticket);
		ticket = null;
	}
}
