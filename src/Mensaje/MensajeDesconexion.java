package Mensaje;

import Clases.Usuario;
import Mensaje.Mensaje.TipoMensaje;

public class MensajeDesconexion extends Mensaje{
	private static final long serialVersionUID = 1L;
	
	private Usuario user;


	public MensajeDesconexion(Usuario user) {
        super(TipoMensaje.DESCONEXION);
        this.user = user;
    }
	
	public Usuario getUuario() {
		return this.user;
	}

}
