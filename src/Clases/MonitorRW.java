package Clases;

public interface MonitorRW {
	
	public void request_write();
	
	public void release_write();
	
	public void request_read();
	
	public void release_read();
	

}