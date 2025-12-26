# Concurrency Guide: Java Concurrent Messaging Server

## Table of Contents
1. [System Overview](#system-overview)
2. [Architecture](#architecture)
3. [Concurrency Mechanisms](#concurrency-mechanisms)
4. [How It All Works Together](#how-it-all-works-together)
5. [Concurrency Benefits](#concurrency-benefits)
6. [Thread Safety Analysis](#thread-safety-analysis)

---

## System Overview

This is a **multi-threaded client-server messaging system** that allows multiple clients to:
- Connect to a central server simultaneously
- Exchange file lists and metadata
- Download files directly from each other (P2P - Peer to Peer)

The system demonstrates multiple approaches to solving concurrency problems, from low-level atomic operations to high-level monitors and locks.

### Key Components

```
Server (Main Thread)
├── OyenteCliente (Listener Thread per client)
│   └── Handles client requests
└── Shared Data Structures
    ├── usuarios (User registry)
    ├── ficheros (File listings)
    ├── entradas/salidas (I/O streams)
    └── almacen (Ticket storage for Producer-Consumer)

Client (Main Thread)
├── OyenteServidor (Server Listener Thread)
├── EmisorP2P (P2P Sender Thread)
└── ReceptorP2P (P2P Receiver Thread)
```

---

## Architecture

### 1. Server Architecture (`Servidor.java`)

The server uses a **thread-per-client model**:
- Main thread listens on port 999 for incoming connections
- Each accepted connection spawns a new `OyenteCliente` thread
- All threads share common data structures protected by concurrency mechanisms

**Key Design Decisions:**
- **MAX_USERS = 2**: Limits concurrent connections (configurable)
- **Ticket-based identification**: Each client gets a unique ticket number for lock identification
- **Concurrent data structures**: Custom `ConcurrentMap` with Reader-Writer locks

### 2. Client Architecture (`Cliente.java`)

Each client has multiple threads:
- **Main thread**: Handles user interaction (console menu)
- **OyenteServidor thread**: Listens for server messages
- **EmisorP2P threads**: Send files to other clients (up to 10 simultaneous)
- **ReceptorP2P threads**: Receive files from other clients (up to 10 simultaneous)

---

## Concurrency Mechanisms

### 1. Custom Lock Implementations (Low-Level)

These are **manual implementations** of classic concurrency algorithms using atomic operations.

#### A. Ticket Algorithm (`LockTicket.java`)

**How it works:**
```java
// Each thread takes a number (like in a bakery or DMV)
int turn = number.getAndAdd(1);  // Atomic increment

// Wait until it's your turn
while(turn != next) {
    Thread.yield();  // Busy-wait with yield to reduce CPU usage
}
```

**Concurrency Benefits:**
- ✅ **FIFO Fairness**: Threads are served in the order they request access
- ✅ **Simple**: Easy to understand and implement
- ✅ **Deadlock-free**: No circular waiting
- ❌ **Busy-waiting**: Uses CPU while waiting (mitigated by `yield()`)

**Used in:** Server `lockTicket` for protecting shared I/O streams

#### B. Bakery Algorithm (`LockBakery.java`)

**How it works:**
```java
// Like a bakery - take a number, wait for smaller numbers
turn.get(i).asignar(getMax() + 1);  // Get next ticket number

// Wait for all threads with smaller (number, id) tuples
for(int j = 1; j <= _n; j++) {
    if(j != i) {
        while((turn[i], i) > (turn[j], j) && turn[j] != 0) {
            Thread.yield();
        }
    }
}
```

**Concurrency Benefits:**
- ✅ **N-thread fairness**: Works for any number of threads
- ✅ **No atomic hardware required**: Uses only reads/writes
- ✅ **Tie-breaking**: Uses (ticket, thread-id) tuples for total ordering
- ❌ **Memory overhead**: Requires array of size N
- ❌ **Busy-waiting**: CPU intensive

**Used in:** Client `lockBakery` (2 threads: main + OyenteServidor)

#### C. Tie-Breaker Algorithm (`LockRompeEmpate.java`)

**How it works:**
```java
// Multi-level tournament algorithm (generalized Peterson)
for(int j = 1; j <= _n; j++) {
    in.get(i).asignar(j);      // Announce intention at level j
    last.get(j).asignar(i);     // Mark yourself as last
    
    // Wait if someone else is at same/higher level and you're last
    for(int k = 1; k <= _n; k++) {
        if(k != i) {
            while(in.get(k) >= in.get(i) && last.get(j) == i) {
                Thread.yield();
            }
        }
    }
}
```

**Concurrency Benefits:**
- ✅ **No starvation**: Eventually everyone gets through
- ✅ **Mutual exclusion**: Only one thread in critical section
- ✅ **Progress**: If one thread is waiting, another can enter
- ❌ **Complex**: Harder to understand than other algorithms
- ❌ **Busy-waiting**: CPU intensive

**Used in:** Client `lockRompEmpate` for console output coordination (up to MAX_DESCARGAS_SIMUL + MAX_ENVIOS_SIMUL + 2 threads)

### 2. Monitor Patterns (High-Level)

#### A. Reader-Writer Monitor (`MonRWLock.java`)

**How it works:**
```java
// Multiple readers OR one writer
private volatile int nr = 0;  // Number of active readers
private volatile int nw = 0;  // Number of active writers
private final ReentrantLock lock = new ReentrantLock();
private final Condition okToRead = lock.newCondition();
private final Condition okToWrite = lock.newCondition();

public void request_read() {
    lock.lock();
    while(nw > 0) {           // Wait if writer is active
        okToRead.await();
    }
    nr = nr + 1;              // One more reader
    lock.unlock();
}

public void request_write() {
    lock.lock();
    while(nr > 0 || nw > 0) { // Wait if any readers or writers
        okToWrite.await();
    }
    nw = nw + 1;              // Mark as writing
    lock.unlock();
}
```

**Concurrency Benefits:**
- ✅ **Read concurrency**: Multiple threads can read simultaneously
- ✅ **Write safety**: Only one writer, blocks all readers
- ✅ **Condition variables**: Efficient waiting (no busy-wait)
- ✅ **Built-in fairness**: ReentrantLock can be fair or unfair
- ⚠️ **Writer starvation risk**: Continuous readers can block writers

**Used in:** `ConcurrentMap` for thread-safe access to shared maps (usuarios, ficheros, entradas, salidas)

#### B. Producer-Consumer with Semaphores (`AlmacenNEnteros.java`)

**How it works:**
```java
// Bounded buffer with 3 semaphores
private Semaphore empty;   // Counts empty slots
private Semaphore full;    // Counts full slots
private Semaphore mutexP;  // Protects producer side
private Semaphore mutexC;  // Protects consumer side

public Entero extraer() {  // Consumer
    full.acquire();        // Wait for data
    mutexC.acquire();      // Lock consumer side
    
    Entero entero = buf.get(ini).getValue();
    ini = (ini + 1) % K;   // Circular buffer
    
    mutexC.release();
    empty.release();       // Signal empty slot
    return entero;
}

public void almacenar(Entero entero) {  // Producer
    empty.acquire();       // Wait for space
    mutexP.acquire();      // Lock producer side
    
    buf.get(fin).asignar(entero);
    fin = (fin + 1) % K;   // Circular buffer
    
    mutexP.release();
    full.release();        // Signal new data
}
```

**Concurrency Benefits:**
- ✅ **Bounded buffer**: Prevents unbounded memory growth
- ✅ **Blocking operations**: Threads sleep when waiting (efficient)
- ✅ **Multiple producers/consumers**: Separate mutexes allow more concurrency
- ✅ **No busy-waiting**: Semaphores use kernel scheduling
- ✅ **Circular buffer**: Efficient memory reuse

**Used in:** Server ticket allocation system (Producer-Consumer pattern for `MAX_USERS` tickets)

### 3. Java Standard Concurrency (`Semaphore`)

Used for **simple signaling** between threads:

```java
private Semaphore conected = new Semaphore(0);

// Thread A waits
conected.acquire();  // Blocks until released

// Thread B signals
conected.release();  // Wakes up Thread A
```

**Concurrency Benefits:**
- ✅ **Simple**: One-line wait/signal
- ✅ **Efficient**: Kernel-level blocking
- ✅ **Flexible**: Can be used as binary semaphore or counting semaphore

**Used in:** Client connection synchronization (main thread waits for OyenteServidor to confirm connection)

### 4. Volatile Variables

```java
private volatile boolean conectado = false;
private volatile int nr = 0;
```

**Concurrency Benefits:**
- ✅ **Visibility guarantee**: Changes immediately visible to all threads
- ✅ **Prevents caching issues**: Forces reads from main memory
- ✅ **Lightweight**: No locking overhead for simple flags
- ⚠️ **Limited**: Only for simple reads/writes, not compound operations

**Used throughout:** Status flags, counters in monitors

---

## How It All Works Together

### Scenario 1: Client Connects to Server

```
[Client Main Thread]                [Server Accept Thread]           [OyenteCliente Thread]
      |                                       |                              |
1. Create MensajeConexion                     |                              |
2. outC.writeObject(msg) ------------------>  |                              |
3. conected.acquire() [WAIT]                  |                              |
      |                             4. Accept connection                     |
      |                             5. new Thread(OyenteCliente) ----------> |
      |                                       |                    6. inS.readObject()
      |                                       |                    7. almacen.extraer() [GET TICKET]
      |                                       |                       - full.acquire()
      |                                       |                       - mutexC.acquire()
      |                                       |                       - Get ticket from buffer
      |                                       |                       - mutexC.release()
      |                                       |                       - empty.release()
      |                                       |                    8. servidor.conectarUsuario()
      |                                       |                       - ConcurrentMap.put()
      |                                       |                       - monLock.request_write()
      |                                       |                       - Write to map
      |                                       |                       - monLock.release_write()
      |                                       |                    9. lockTicket.takeLock()
      |                                       |                   10. outS.writeObject(confirm)
      |                                       |                   11. lockTicket.releaseLock()
      | <-------------------------------------------------------- 12. Send MensajeConfirmacionConexion
      |
[OyenteServidor Thread]
13. inC.readObject()
14. conected.release() [WAKE UP MAIN]
      |
15. conected.acquire() completes
16. Show menu
```

### Scenario 2: Multiple Clients Request File List Simultaneously

```
[Client1 OyenteCliente]              [Client2 OyenteCliente]           [ConcurrentMap]
      |                                       |                              |
1. Receive CONSULTAR_INFO                    |                              |
2. servidor.getArchivos()                    |                              |
   - monLock.request_read() --------------->  |                    [nr++, ALLOW]
   - Read map data                           |                              |
      |                             3. Receive CONSULTAR_INFO               |
      |                             4. servidor.getArchivos()                |
      |                                monLock.request_read() ---------> [nr++, ALLOW]
      |                                Read map data                        |
   - monLock.release_read() -------------->  |                    [nr--, signal if needed]
      |                                monLock.release_read() ------> [nr--, signal if needed]
5. lockTicket.takeLock(ticket1)              |                              |
6. outS.writeObject(info)                    |                              |
7. lockTicket.releaseLock(ticket1)           |                              |
      |                             8. lockTicket.takeLock(ticket2)         |
      |                             9. outS.writeObject(info)               |
      |                            10. lockTicket.releaseLock(ticket2)      |
```

**Key Points:**
- Both clients can **read simultaneously** (Reader-Writer lock allows this)
- But they **write to socket sequentially** (Ticket lock ensures FIFO order)

### Scenario 3: Client Disconnects (Producer Returns Ticket)

```
[Client OyenteCliente]               [AlmacenNEnteros]
      |                                       |
1. Receive DESCONEXION                       |
2. servidor.desconectarUsuario()             |
   - ConcurrentMap.remove()                  |
   - monLock.request_write()                 |
   - Remove from map                         |
   - monLock.release_write()                 |
3. lockTicket.takeLock()                     |
4. outS.writeObject(confirm)                 |
5. lockTicket.releaseLock()                  |
6. almacen.almacenar(ticket) --------------> |
                                    7. empty.acquire() [GET SPACE]
                                    8. mutexP.acquire()
                                    9. buf[fin] = ticket
                                   10. fin = (fin + 1) % K
                                   11. mutexP.release()
                                   12. full.release() [SIGNAL: TICKET AVAILABLE]
                                             |
[New Client OyenteCliente]                   |
      |  <--------------------------------- 13. almacen.extraer() can now succeed
```

---

## Concurrency Benefits

### 1. **Throughput**

| Without Concurrency | With Concurrency |
|-------------------|-----------------|
| 1 client at a time | Up to MAX_USERS simultaneously |
| Blocked during file transfer | P2P transfers don't block server |
| Sequential request processing | Parallel request handling |

### 2. **Responsiveness**

- **Multiple readers**: Several clients can query file lists at the same time
- **Non-blocking UI**: Client console remains responsive while downloading
- **P2P architecture**: Direct client-to-client transfers free up server

### 3. **Resource Utilization**

- **CPU**: Multiple threads can run on multiple cores
- **I/O**: While one thread waits for I/O, others can execute
- **Memory**: Bounded buffers prevent memory exhaustion

### 4. **Fairness**

- **FIFO ordering**: Ticket/Bakery algorithms ensure no thread waits forever
- **Starvation prevention**: All algorithms guarantee eventual progress
- **Reader preference**: Multiple readers don't block each other

### 5. **Safety**

Every shared resource is protected:

| Shared Resource | Protection Mechanism | Why It's Needed |
|----------------|---------------------|-----------------|
| `usuarios`, `ficheros` maps | Reader-Writer Monitor (MonRWLock) | Multiple readers OR one writer |
| Server output streams | Ticket Lock | FIFO message ordering per client |
| Client output stream | Bakery Lock | Prevents garbled messages |
| Console output | Tie-Breaker Lock | Prevents interleaved text |
| Ticket allocation | Semaphore-based Producer-Consumer | Bounded resource pool |

---

## Thread Safety Analysis

### Race Conditions Prevented

#### 1. **Map Access** (usuarios, ficheros, entradas, salidas)

**Without protection:**
```java
// Thread 1                    // Thread 2
if (map.containsKey(key)) {   
                               map.remove(key);  // RACE!
    User u = map.get(key);    // Returns null - NullPointerException!
}
```

**With Reader-Writer Lock:**
```java
// Thread 1 (Read)             // Thread 2 (Write)
monLock.request_read();        monLock.request_write();
if (map.containsKey(key)) {    // BLOCKED until Thread 1 releases
    User u = map.get(key);     
}                              map.remove(key);
monLock.release_read();        monLock.release_write();
```

#### 2. **Socket Output Stream** (writeObject)

**Without protection:**
```java
// Thread 1                     // Thread 2
outS.writeObject(msg1);        outS.writeObject(msg2);
// Bytes from msg1 and msg2 get interleaved - CORRUPTED DATA!
```

**With Lock:**
```java
// Thread 1                     // Thread 2
lock.takeLock(id1);            lock.takeLock(id2);  // WAITS for turn
outS.writeObject(msg1);        
lock.releaseLock(id1);         // Now Thread 2 proceeds
                               outS.writeObject(msg2);
                               lock.releaseLock(id2);
```

#### 3. **Ticket Allocation** (Connection Limit)

**Without protection:**
```java
// Thread 1                     // Thread 2
if (tickets < MAX) {           if (tickets < MAX) {
    tickets++;                     tickets++;  // RACE!
    // Both increment - exceeds MAX!
}
```

**With Semaphore:**
```java
// Thread 1                     // Thread 2
full.acquire();                full.acquire();  // One blocks if at MAX
mutexC.acquire();              // Protected by mutex
ticket = getTicket();          
mutexC.release();              mutexC.acquire();
                               ticket = getTicket();
                               mutexC.release();
```

### Deadlock Avoidance

The system avoids deadlock through:

1. **Lock ordering**: Always acquire in consistent order
2. **No nested locks**: Most code uses single lock at a time
3. **Timeout-free design**: No indefinite waits with multiple resources
4. **Producer-Consumer pattern**: Natural ordering prevents circular waiting

### Performance Optimizations

1. **Reader concurrency**: Multiple threads read maps simultaneously
2. **Separate producer/consumer mutexes**: More parallelism in bounded buffer
3. **`Thread.yield()` in spin-locks**: Reduces CPU usage during busy-waiting
4. **Volatile for simple flags**: Avoids lock overhead

---

## Summary: What Helps With Concurrency?

### 1. **Locks & Mutual Exclusion**
- Ticket Lock, Bakery Lock, Tie-Breaker Lock
- **Benefit**: Prevent race conditions on shared resources

### 2. **Monitors (Reader-Writer)**
- ReentrantLock with Conditions
- **Benefit**: Allow multiple readers OR one writer

### 3. **Semaphores**
- Producer-Consumer pattern
- **Benefit**: Bounded resource allocation, efficient blocking

### 4. **Atomic Operations**
- `AtomicInteger.getAndAdd()`
- **Benefit**: Lock-free operations for simple counters

### 5. **Volatile Variables**
- Visibility guarantees
- **Benefit**: Lightweight synchronization for flags

### 6. **Thread-per-Client Model**
- Independent request handling
- **Benefit**: Parallel processing, responsiveness

### 7. **Producer-Consumer Pattern**
- Ticket allocation system
- **Benefit**: Connection throttling, resource management

### 8. **P2P Architecture**
- Direct client-to-client file transfers
- **Benefit**: Server offloading, scalability

---

## Educational Value

This project showcases:

1. **Evolution of concurrency**: From low-level atomics to high-level monitors
2. **Classic algorithms**: Bakery, Ticket, Reader-Writer
3. **Real-world patterns**: Producer-Consumer, Thread-per-connection
4. **Trade-offs**: Fairness vs. performance, simplicity vs. efficiency

It's a comprehensive reference for understanding concurrency at multiple levels of abstraction! 🎓
