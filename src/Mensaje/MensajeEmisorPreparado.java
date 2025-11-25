package Mensaje;

public class MensajeEmisorPreparado extends Mensaje{

	private static final long serialVersionUID = 1L;
	
	private String ip;
	private int port;
	private String archivo;
	private String receptor;
	private String nombreDeDescarga;
	
	public MensajeEmisorPreparado(String _ip, int _port, String _archivo, String _receptor, String nombreDeDescarga) {
		super(TipoMensaje.EMISOR_PREPARADO);
		this.ip = _ip;
		this.port = _port;
		this.archivo = _archivo;
		this.receptor = _receptor;
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
	public String getReceptor() {
		return receptor;
	}
	
	public String getNombreDeDescarga() {
		return this.nombreDeDescarga;
	}


}
