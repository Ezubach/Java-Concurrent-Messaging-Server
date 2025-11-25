package Auxiliares;

public class Tupla {
	
	private int a;
	private int b;
	
	public Tupla(int _a, int _b) {
		a = _a;
		b = _b;
	}
	
	public Boolean mayor(Tupla B) {
		
		return a > B.a || (a == B.a && b > B.b);
	}

}
