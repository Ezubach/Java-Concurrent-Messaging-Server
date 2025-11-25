package Mensaje;

public class MensajeDescConCliente extends Mensaje{

	private static final long serialVersionUID = 1L;

	public MensajeDescConCliente() {
		super(TipoMensaje.DESCONEXION_CON_CLIENTE);
	}
}
