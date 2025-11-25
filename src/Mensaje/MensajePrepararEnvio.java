package Mensaje;


public class MensajePrepararEnvio extends Mensaje {

	private static final long serialVersionUID = 1L;
	
	private String archivo;
	private String receptor;
	
	private String nombreDeGuardado;

	public MensajePrepararEnvio(String _receptor, String _archivo, String nombreDeGuardado) {
		super(TipoMensaje.PREPARAR_ENVIO);
		this.archivo = _archivo;
		this.receptor = _receptor;
		this.nombreDeGuardado = nombreDeGuardado;
	}
	
	public String getArchivo() {
		return this.archivo;
	}
	
	public String getReceptor() {
		return this.receptor;
	}
	
	public String getNombreDeDescarga() {
		return this.nombreDeGuardado;
	}

}
