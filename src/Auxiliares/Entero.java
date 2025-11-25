package Auxiliares;

public class Entero {
	private volatile int n;
	
	public Entero (int _n) {
		n = _n;
	}
	
	public void sumar(int i){
		n += i;
	}
	
	public void mostrar() {
		System.out.printf("%d\n", n);
	}
	
	public void asignar(int i) {
		this.n = i;
	}
	public int getValor() {
        return n;
    }
	
}
