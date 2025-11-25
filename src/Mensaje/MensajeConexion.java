package Mensaje;

import java.net.ServerSocket;
import java.util.List;

import Clases.Usuario;

public class MensajeConexion extends Mensaje {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Usuario user;
	

	public MensajeConexion(Usuario _user) {
		super(TipoMensaje.CONEXION);
		
		this.user = _user;
	}
	
	public Usuario getUser() {
		
		return this.user;
	}
	
}
