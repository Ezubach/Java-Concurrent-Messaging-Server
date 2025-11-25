package Auxiliares;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class AlmacenNEnteros implements Almacen{

	private volatile int ini = 0;
	private volatile int fin = 0;
	
	private Semaphore empty;
	private Semaphore full; 
	private Semaphore mutexP;
	private Semaphore mutexC;
	
	private int K;
	
	private ArrayList <Cell<Entero>> buf;
	
	public AlmacenNEnteros(int k) {
		this.K = k;
		buf = new ArrayList<Cell<Entero>>(K);
		
		empty = new Semaphore(0);
		full = new Semaphore(this.K);
		mutexP = new Semaphore(1);
		mutexC = new Semaphore(1);
		
		 // Add Cell objects to both lists
        for (int i = 0; i < K; i++) {
            buf.add(new Cell<Entero>(new Entero(i)));  // Use add() to append
        }
	}
	
	
	@Override
	public void almacenar(Entero entero) {
		
		try {
			empty.acquire();
			mutexP.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		buf.get(fin).asignar(entero);
		System.out.println("Guardo el ticket en la casilla " + fin + '\n');
		fin = (fin+1)%K;
		
		
		mutexP.release();
		full.release();
		
	}

	@Override
	public Entero extraer() {
		
		try {

			full.acquire();
			mutexC.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		Entero entero = buf.get(ini).getValue();
		System.out.println("Otorgo el ticket de la casilla " + ini + '\n');
		ini = (ini + 1)%K;
		
		
		
		mutexC.release();
		empty.release();
		
		return entero;
		
		
	}
	
	

}