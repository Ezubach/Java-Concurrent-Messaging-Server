package Locks;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class LockTicket implements Lock{
	
	
	//private ArrayList<Entero> turn;
	
	private AtomicInteger number;
	private volatile int next;

	
	private int _n;
	
	public LockTicket(int n) {
		_n = n; 
		
        number = new AtomicInteger(1);
        next = 1;


	}
	
	public void takeLock (int i) {
		
//		int turn = number.getAndAdd(1);
//		while(turn != next) {
//			Thread.yield();
//		}
		
		int turn = number.getAndAdd(1);
		if (turn == this._n) {
			number.getAndAdd(-this._n);
		}
		else if (turn > this._n) {turn -=this._n;}
		
		while(turn!= next) {
			Thread.yield();
		}
		
		
	}
	
	public void releaseLock(int n) {
		next = (next % this._n) + 1;
	}
	
	
	
	

}
