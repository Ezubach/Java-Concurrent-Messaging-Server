package Mensaje;

import java.util.List;
import java.util.Map;

import Clases.Usuario;

public class MensajeConfirmacionInfo extends Mensaje {
	//El servidor envia la confirmacion con toda la informacion que tiene en su base de datos
	Map<String, List<String>> _listaArchivos;
	
	
	public MensajeConfirmacionInfo(Map<String, List<String>> listaArchivos ) {
		super(TipoMensaje.CONFIRMACION_INFO);
		this._listaArchivos = listaArchivos;
	}
	
	//Hay que añadir la informacion 
	public Map<String, List<String>> getLista(){
		return this._listaArchivos;
	}
}
