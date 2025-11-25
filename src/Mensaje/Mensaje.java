package Mensaje;

import java.io.Serializable;

public abstract class Mensaje implements Serializable {
	

	public static enum TipoMensaje {
		//Mensajes de conexion y desconexion
		CONEXION, DESCONEXION, CONFIRMACION_CONEXION, CONFIRMACION_DESCONEXION,
		
		//Cliente solicita la informacion de los usuarios conectados 
		//y el servidor manda confirmacion que lleva consigo la lista
		CONSULTAR_INFO, CONFIRMACION_INFO, 
		
		
		//Un cliente solicita un archivo al servidor, 
		//y este manda un mensaje al cliente para que se prepare para enviar
		SOLICITAR_ARCHIVO, PREPARAR_ENVIO,
		
		//El cliente emisor envia un mensaje de que tiene preparado el puerto de envío, 
		//y el servidor avisa el cliente receptor para que se prepare e inicie la descarga
		EMISOR_PREPARADO, PREPARAR_RECEPCION,
		
		
		
		ARCHIVO_DESCARGADO,
		//-------------
		//Estos ya son mensajes entre clientes
		//-------------
		

		
		
		//Solicitud directa del cliente receptor al cliente emisor cuando ya se ha establecido la conexion entre ambos
		DESCARGAR_ARCHIVO, ENVIAR_ARCHIVO,
		
		//Una vez terminado de descargar la informacion, el cliente receptor avisa al emisor de cerrar conexion
		//El emisor manda confirmacion de desconexion
		DESCONEXION_CON_CLIENTE, CONFIRMACION_DESCONEXION_CON_CLIENTE
	}


    private static final long serialVersionUID = 1L;

    private final TipoMensaje tipo;

    public Mensaje(TipoMensaje tipo) {
        this.tipo = tipo;
    }

    public TipoMensaje getTipo() {
        return tipo;
    }
}