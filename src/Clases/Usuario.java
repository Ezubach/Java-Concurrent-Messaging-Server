package Clases;

import java.io.Serializable;
import java.util.List;

public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

	private String id;
	private String ip;
	private List<String> sharedArchives;
	private String folder;

	public Usuario(String _id, String _ip, List<String> _sharedArchives,String _folder) {
		this.id = _id;
		this.ip = _ip;
		this.sharedArchives = _sharedArchives;
		this.folder = _folder;
	}

	public String getId() {

		return this.id;
	}

	public String getIp() {
		return this.ip;
	}

	public List<String> getSharedArhives() {
		return this.sharedArchives;
	}

	
	public void addArchive(String file) {
		sharedArchives.add(file);
	}
	
	
	public String getFolder() {
		return this.folder;
	}

}
