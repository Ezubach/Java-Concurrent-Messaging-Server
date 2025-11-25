package Mensaje;

public class MensajeConsultarInfo extends Mensaje{
	
	//Clase de Mensaje donde el cliente envía al Servidor para pedirle la lista de usuarios con la informacion
	
	
	
    public MensajeConsultarInfo() {
		super(TipoMensaje.CONSULTAR_INFO);
	}

	

}
