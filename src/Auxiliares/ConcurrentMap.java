package Auxiliares;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import Clases.MonRWLock;
import Monitores.MonRWSynch;

public class ConcurrentMap<K, V> implements Map<K, V>{

	
	private MonRWLock monLock;
	//private MonRWSynch monSynch;

	private Map<K, V> backingMap;

	public ConcurrentMap() {
		this.monLock = new MonRWLock();
		//this.monSynch = new MonRWSynch();
		this.backingMap = new HashMap<>();
	}

	@Override
	public int size() {

		monLock.request_read();
		try {
			return backingMap.size();
		} finally {
			monLock.release_read();
		}

	}

	@Override
	public boolean isEmpty() {

		monLock.request_read();
		try {
			return backingMap.isEmpty();
		} finally {
			monLock.release_read();
		}
	}

	@Override
	public boolean containsKey(Object key) {

		monLock.request_read();
		try {
			return backingMap.containsKey(key);
		} finally {
			monLock.release_read();
		}
	}

	@Override
	public boolean containsValue(Object value) {

		monLock.request_read();
		try {
			return backingMap.containsValue(value);
		} finally {
			monLock.release_read();
		}

	}

	@Override
	public V get(Object key) {
		monLock.request_read();
		try {
			return backingMap.get(key);
		} finally {
			monLock.release_read();
		}
	}

	@Override
	public V put(K key, V value) {

		monLock.request_write();
		try {
			return backingMap.put(key, value);
		} finally {
			monLock.release_write();
		}

	}

	@Override
	public V remove(Object key) {

		monLock.request_write();
		try {
			return backingMap.remove(key);
		} finally {
			monLock.release_write();
		}
	}

	@Override
	public void putAll(Map m) {

		monLock.request_write();
		try {
			backingMap.putAll(m);
		} finally {
			monLock.release_write();
		}

	}

	@Override
	public void clear() {

		monLock.request_write();
		try {
			backingMap.clear();
		} finally {
			monLock.release_write();
		}

	}

	@Override
	public Set<K> keySet() {

		monLock.request_read();
		try {
			return backingMap.keySet();
		} finally {
			monLock.release_read();
		}
	}

	@Override
	public Collection<V> values() {

		monLock.request_read();
		try {
			return backingMap.values();
		} finally {
			monLock.release_read();
		}
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {

		monLock.request_read();
		try {
			return backingMap.entrySet();
		} finally {
			monLock.release_read();
		}
	}
	
	
	//
	
	public Map<K, V> getMap() {
		monLock.request_read();
		try {
			return backingMap;
		} finally {
			monLock.release_read();
		}
	}

}
