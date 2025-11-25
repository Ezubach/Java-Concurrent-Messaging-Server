package Monitores;

import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import Auxiliares.*;

public class MNProductLock implements MonitorProdCons{
	
	private ArrayList<Cell<Entero>> buf;
	private volatile int ini = 0;
	private volatile int fin = 0;
	private volatile int count;
	
	private final ReentrantLock lock = new ReentrantLock();
	private final Condition empty = lock.newCondition(); 
	private final Condition full = lock.newCondition();  
	
	
	
	private int k;
	
	public MNProductLock(int _k, int count) {
		this.k = _k;
		this.count = count;
		buf = new ArrayList<Cell<Entero>>(this.k);
		
		for (int i = 0; i < this.k; i++) {
            buf.add(new Cell<Entero>(new Entero(i)));  // Use add() to append
        }
	}

	@Override
	public void almacenar(Entero producto) {
		
		
		lock.lock();
		
		while (count == this.k) {

				try {
					empty.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
		
		buf.get(fin).asignar(producto);
		fin = (fin + 1)%this.k;
		count++;
		full.signal();
		
		lock.unlock();
		
	}
	
	@Override
	public Entero extraer() {
		
		lock.lock();
		
		while(count == 0) {
				try {
					full.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
		
		Entero result = buf.get(ini).getValue();
		ini = (ini+1)%this.k;
		count--;
	
		empty.signal();
		
		lock.unlock();
		
		return result;
	}

	

}
