package Monitores;


import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MonRWLock implements MonitorRW{
	
	private volatile int nr = 0;
	private volatile int nw = 0;
	
	private final ReentrantLock lock = new ReentrantLock();
	private final Condition okToRead = lock.newCondition(); 
	private final Condition okToWrite = lock.newCondition();
	
	

	@Override
	public void request_write() {
		
		lock.lock();
		while(nr > 0 || nw > 0) {
			try {
				okToWrite.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		nw = nw + 1;
		lock.unlock();
	}

	@Override
	public void release_write() {
		lock.lock();
		nw = nw - 1;
		okToWrite.signal();

		okToRead.signalAll();
		lock.unlock();
		
		
		
	}
	
	@Override
	public void request_read() {
		lock.lock();
		
		while(nw > 0 ) {
			try {
				okToRead.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		nr = nr + 1;
		lock.unlock();
		
	}

	@Override
	public void release_read() {
		
		lock.lock();
		nr = nr - 1;
		if(nr == 0);
		okToWrite.signal();
		lock.unlock();
		
	}



}
