package Clases;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

import Auxiliares.Cell;

import java.util.HashMap;
import java.util.List;

import Monitores.*;
import java.util.concurrent.Semaphore;

import Auxiliares.*;
import Locks.*;


public class Servidor {
	
	private static final int MAX_USERS = 2;
	
	
	//Mapa que guarda el nombre de usuario como clave junto a todo el resto de informacion del usuario
	private final ConcurrentMap<String, Cell<Usuario>> usuarios = new ConcurrentMap<>();
	
	//Mapa donde guardamos cuales son los ficheros públicos de cada cliente
	private final ConcurrentMap<String, List<String>> ficheros = new ConcurrentMap<>();
	private final ConcurrentMap<String, Cell<ObjectInputStream>> entradas = new ConcurrentMap<>();
	private final ConcurrentMap<String, Cell<ObjectOutputStream>> salidas = new ConcurrentMap<>();
	
	
	private Almacen almacen = new AlmacenNEnteros(MAX_USERS);
	
	
	private Lock lockTicket = new LockTicket(MAX_USERS);

	public static void main(String[] args) throws IOException {
		Servidor servidor = new Servidor();
		servidor.ejecutar();
	}

	public void ejecutar() throws IOException {
		ServerSocket ss = new ServerSocket(999);

		while (true) {
			System.out.println("Esperando conexion");
			Socket s = ss.accept();

			System.out.println("Un cliente con IP " + s.getInetAddress().getHostAddress() + " intenta conectarse");
			new Thread(new OyenteCliente(s, almacen, lockTicket, this)).start();
		}
	}
	
	
	//---------------------
	//FUNCIONES DE ESCRITOR
	//---------------------

	public void conectarUsuario(Usuario u) {

		usuarios.put(u.getId(), new Cell<>(u));
		ficheros.put(u.getId(), u.getSharedArhives());	
		
	}
	
	public void agregarFlujos(Usuario u, ObjectOutputStream o, ObjectInputStream in) {
		entradas.put(u.getId(), new Cell<>(in));
		salidas.put(u.getId(), new Cell<>(o));
	}

	public void desconectarUsuario(Usuario u) {
		usuarios.remove(u.getId());
		ficheros.remove(u.getId());
	}
	public void eliminarFlujos(Usuario u, ObjectOutputStream o, ObjectInputStream in) {
		entradas.remove(u.getId());
		salidas.remove(u.getId()); 
	}
	

	
	
	//-------------------
	//FUNCIONES DE LECTOR
	//-------------------


	public Map<String, List<String>> getArchivos() {
	    Map<String, List<String>> result = ficheros.getMap();
	    return result;
	}

	public Usuario buscarArchivo(String nombre) {
		for (Cell<Usuario> cell : usuarios.values()) {
			Usuario usuario = cell.getValue();
			List<String> archivos = usuario.getSharedArhives();
			if (archivos.contains(nombre)) {
				return usuario;
			}
		}


		return null; // Nadie lo tiene
	}
	
	
	public ObjectInputStream getFlujoEntrada(String user) {
	    Cell<ObjectInputStream> cell = entradas.get(user);
	    if (cell != null) {
	        return cell.getValue();
	    } else {
	        return null;
	    }
	}

	
	public ObjectOutputStream getFlujoSalida(String user) {
	    Cell<ObjectOutputStream> cell = salidas.get(user);
	    if (cell != null) {
	        return cell.getValue();
	    } else {
	        return null;
	    }
	}
	
	


}
