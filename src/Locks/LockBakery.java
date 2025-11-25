package Locks;

import java.util.ArrayList;


import Auxiliares.Entero;
import Auxiliares.Tupla;


public class LockBakery implements Lock{
	
	
	private ArrayList<Entero> turn;

	
	private int _n;
	
	public LockBakery(int n) {
		_n = n;
		 // Inicializar los arreglos
		
        turn = new ArrayList<Entero>(n + 1);  

     // Add Cell objects to both lists
        for (int i = 0; i <= this._n; i++) {
            turn.add(new Entero(0));  // Use add() to append
        }
	}
	
	public void takeLock (int i) {
		
		turn.get(i).asignar(1);
		turn.get(i).asignar(getMax() + 1);
		for(int j = 1; j <= _n; j++) {
			if(j!=i) {
				while((new Tupla(turn.get(i).getValor(),i)).mayor(new Tupla(turn.get(j).getValor(),j)) 
						&& turn.get(j).getValor()!=0) {
					Thread.yield();
				}
			}
		}
		
		
	}
	
	public void releaseLock(int n) {
		turn.get(n).asignar(0);
	}
	
	private int getMax() {
		int max = -1;
		
		for(int i = 1; i < _n + 1;i++) {
			if(turn.get(i).getValor() > max)max = turn.get(i).getValor();
		}
		return max;
	}
	
	
	
	

}
