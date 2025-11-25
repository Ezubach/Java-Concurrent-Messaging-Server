package Auxiliares;
public class Cell<T> {

	volatile T elem;

	public Cell(T _elem) {
		elem = _elem;
	}

	public void asignar(T valor) {
		elem = valor;
	}

	public T getValue() {
		return elem;
	}

}
