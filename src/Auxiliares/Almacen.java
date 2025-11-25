package Auxiliares;

public interface Almacen { 
	/** * Almacena (como ultimo) un entero en el almacen
	 * . Si no hay * hueco el proceso que ejecute el metodo bloqueara hasta que lo * haya. 
*/
	public void almacenar(Entero entero); 
	/** * Extrae el primer entero disponible.
	 Si no hay enteros el * proceso que ejecute el metodo bloqueara 
	 hasta que se almacene un * dato. */ 
	
	public Entero extraer(); 
	
}