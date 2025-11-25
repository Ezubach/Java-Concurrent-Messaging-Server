package Locks;
import java.util.ArrayList;

import Auxiliares.Entero;

public class LockRompeEmpate implements Lock{
	
	
	private volatile ArrayList<Entero> last;
	private volatile ArrayList<Entero> in;
	
	private int _n;
	
	public LockRompeEmpate(int n) {
		_n = n;
		 // Inicializar los arreglos
		
        in = new ArrayList<Entero>(n + 1);  
        last = new ArrayList<Entero>(n + 1);

     // Add Cell objects to both lists
        for (int i = 0; i <= n; i++) {
            in.add(new Entero(0));  // Use add() to append
            last.add(new Entero(0)); // Use add() to append
        }
	}
	
	public void takeLock (int i) {
		
		for(int j = 1; j <= _n;j++) {
			in.get(i).asignar(j);
			last.get(j).asignar(i);
			for(int k = 1; k <=_n;k++) {
				if(k!=i) {
					while(in.get(k).getValor() >= in.get(i).getValor() && last.get(j).getValor() == i) {
						Thread.yield();
					}
				}
			}
		}
		
	}
	
	public void releaseLock(int n) {
		in.get(n).asignar(0);
	}
	
	
	
	

}
