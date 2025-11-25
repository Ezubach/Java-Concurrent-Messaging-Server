package Mensaje;

public class MensajeSolicitarArchivo extends Mensaje {

	private static final long serialVersionUID = 1L;
	
	private String archivo;
	private String solicitante;
	
	private String solicitado;
	
	private String archivoGuardar;

	public MensajeSolicitarArchivo(String solicitante, String solicitado, String _archivo, String guardado) {
		super(TipoMensaje.SOLICITAR_ARCHIVO);
		this.archivo = _archivo;
		this.solicitante = solicitante;
		this.solicitado = solicitado;
		this.archivoGuardar=guardado;
		
	}
	
	
	public String getArchivo() {
		return this.archivo;
	}

	public String getSolicitante() {
		return this.solicitante;
	}
	
	public String getSolicitado() {
		
		return this.solicitado;
	}
	public String getNombreDeDescarga() {
		return this.archivoGuardar;
	}
}
