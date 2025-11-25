package Mensaje;

public class MensajePrepararRecepcion extends Mensaje{

	private static final long serialVersionUID = 1L;
	
	private String ip;
	private int port;
	private String archivo;
	private String nombreDeDescarga;

	public MensajePrepararRecepcion(String _ip, int _port, String _archivo, String nombreDeDescarga) {
		super(TipoMensaje.PREPARAR_RECEPCION);
		this.ip = _ip;
		this.port = _port;
		this.archivo = _archivo;
		this.nombreDeDescarga = nombreDeDescarga;
	}

	public int getPort() {
		return port;
	}

	public String getIp() {
		return ip;
	}
	
	public String getArchivo() {
		return archivo;
	}

	public String getNombreDeDescarga() {
		return this.nombreDeDescarga;
	}


}
