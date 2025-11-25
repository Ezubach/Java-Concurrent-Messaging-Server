package Mensaje;

public class MensajeEnviarArchivo extends Mensaje {

	

	private static final long serialVersionUID = 1L;
	
	String contenido;

	public MensajeEnviarArchivo(String _contenido) {
		super(TipoMensaje.ENVIAR_ARCHIVO);
		this.contenido = _contenido;
	}
	
	public String getContenido() {
		return contenido;
	}

}
