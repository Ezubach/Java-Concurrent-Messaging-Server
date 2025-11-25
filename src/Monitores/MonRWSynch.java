package Monitores;





public class MonRWSynch implements MonitorRW{

	
	private volatile int nr = 0;
	private volatile int nw = 0;
	
	

	@Override
	public synchronized void request_write() {
		while(nr > 0 || nw > 0) {
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		nw = nw + 1;
	}

	@Override
	public synchronized void release_write() {
		nw = nw - 1;
		notifyAll();
		
		
	}
	
	@Override
	public synchronized void request_read() {
		
		while(nw > 0 ) {
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		nr = nr + 1;
		
	}

	@Override
	public synchronized void release_read() {
		nr = nr - 1;
		if(nr == 0)notifyAll();
		
	}

	

}
