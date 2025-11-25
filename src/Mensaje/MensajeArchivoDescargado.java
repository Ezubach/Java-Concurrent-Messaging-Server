package Mensaje;

public class MensajeArchivoDescargado extends Mensaje{

	private static final long serialVersionUID = 1L;

	
	private String id;
	private String archivo;
	
	public MensajeArchivoDescargado(String _id, String _archivo) {
		super(TipoMensaje.ARCHIVO_DESCARGADO);
		this.id = _id;
		this.archivo = _archivo;
	}
	
	public String getArchivo() {
		return archivo;
	}
	
	public String getId() {
		return id;
	}
	
	

}
