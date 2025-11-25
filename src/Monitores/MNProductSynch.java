package Monitores;

import Auxiliares.*;

import java.util.ArrayList;



public class MNProductSynch implements MonitorProdCons{
	
	private ArrayList<Cell<Entero>> buf;
	private volatile int ini = 0;
	private volatile int fin = 0;
	private volatile int count;
	
	
	private int k;
	
	public MNProductSynch(int _k, int count) {
		this.k = _k;
		this.count = count;
		buf = new ArrayList<Cell<Entero>>(this.k);
		
		for (int i = 0; i < this.k; i++) {
            buf.add(new Cell<Entero>(new Entero(i)));  // Use add() to append
        }
	}

	@Override
	public synchronized void almacenar(Entero producto) {
		while (count == this.k) {

				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
		
		buf.get(fin).asignar(producto);
		fin = (fin + 1)%this.k;
		count++;
		notifyAll();
		
		
	}
	
	@Override
	public synchronized Entero extraer() {
		
		while(count == 0) {
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
		
		Entero result = buf.get(ini).getValue();
		ini = (ini+1)%this.k;
		count--;
	
		notifyAll();
		
		return result;
	}

	

}
