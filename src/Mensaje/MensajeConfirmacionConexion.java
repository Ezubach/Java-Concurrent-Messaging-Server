package Mensaje;

public class MensajeConfirmacionConexion extends Mensaje {
    private String mensaje;


    public MensajeConfirmacionConexion(String mensaje) {
        super(TipoMensaje.CONFIRMACION_CONEXION);
        this.mensaje = mensaje;
    }

    public String getMensaje() {
        return mensaje;
    }

}