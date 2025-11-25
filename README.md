# Java Concurrent Messaging Server

![Java](https://img.shields.io/badge/Java-Concurrency-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Multithreading](https://img.shields.io/badge/Pattern-Multi--Threaded-blue?style=for-the-badge)
![Algorithms](https://img.shields.io/badge/Algorithms-Custom%20Impl-success?style=for-the-badge)

> **Overview:** A high-performance, multi-threaded chat server implemented in Java. This project explores the evolution of concurrency control by implementing classic algorithms (Bakery, Ticket, Barriers) from scratch using **Atomic Operations**, before evolving into high-level constructs like **Monitors** and **Semaphores**.

---

## Introduction

In the domain of distributed systems, managing concurrent access to shared resources is the critical challenge. This project implements a robust Server-Client architecture capable of handling multiple simultaneous users exchanging messages.

Rather than relying solely on standard libraries, this repository serves as a deep-dive into the mechanics of **Synchronization**. We manually implement distinct solutions for the **Critical Section Problem**, ensuring **Mutual Exclusion**, **Progress**, and **Bounded Waiting** using different levels of abstraction: from low-level atomic variables to high-level locks.

---

## Project Objectives

* **Concurrency Mastery:** Solve Race Conditions using classic theoretical algorithms.
* **Producer-Consumer Pattern:** Implement a bounded buffer system for message handling using Semaphores.
* **Thread Coordination:** Orchestrate thread execution phases using complex Barrier structures (Butterfly & Symmetric).
* **Monitor Pattern:** encapsulate shared state using Java's `synchronized` keyword and `ReentrantLock`.
* **Fairness:** Ensure FIFO access to resources using Ticket and Bakery algorithms.

---

## System Architecture & Algorithms

The system is built upon three layers of concurrency control, applied to the message exchange core:

### 1. Mutual Exclusion Algorithms (Low-Level)
Implemented using Java's `AtomicInteger` and `AtomicBoolean` to simulate hardware-level instructions.
* **Tie-Breaker Algorithm (Rompe Empate):** A generalization of Peterson’s algorithm for $N$ threads.
* **Ticket Algorithm:** Ensures fairness. Threads take a "ticket" number and wait for their turn (FIFO).
* **Bakery Algorithm (Lamport):** Solves the critical section problem for multiple threads, guaranteeing fairness even without atomic hardware numbers.

### 2. Synchronization Primitives
Custom implementations used to coordinate the flow of the Producer-Consumer pattern.
* **Barriers:**
    * **Butterfly Barrier:** Optimized for recursive doubling communication patterns.
    * **Symmetric Barrier:** Ensures all threads reach a synchronization point before proceeding.
* **Semaphores:** Used to control access to the shared message buffer (counting empty/full slots).

### 3. Monitors & High-Level Locking
Encapsulation of the logic using Java's built-in support.
* **Intrinsic Locks:** Implementation using `synchronized` methods and blocks.
* **Explicit Locks:** Implementation using `java.util.concurrent.locks.Lock` and Condition Variables for fine-grained control.

---

## Workflow

1.  **Connection:** Clients connect to the server, spawning a new `ServerThread`.
2.  **Production:** A sender (Producer) generates a message.
3.  **Entry Protocol:** The thread requests access to the shared buffer using a selected algorithm (e.g., *Ticket Algorithm* or *Semaphore*).
4.  **Critical Section:**
    * The message is written to the shared structure.
    * If using **Barriers**, threads wait for the group to synchronize.
5.  **Exit Protocol:** The thread releases the lock/semaphore, notifying waiting threads.
6.  **Consumption:** The receiver (Consumer) reads the message safely.

---

## Concurrency Strategies Comparison

This project benchmarks different approaches to solving the same problems:

| Challenge | Low-Level Solution (Atomics) | High-Level Solution (Java API) |
| :--- | :--- | :--- |
| **Mutual Exclusion** | **Bakery / Ticket / Tie-Breaker** | `ReentrantLock` / `synchronized` |
| **Buffer Management** | **Custom Semaphores** (using CAS) | `java.util.concurrent.Semaphore` |
| **Group Sync** | **Butterfly / Symmetric Barriers** | `CyclicBarrier` / `Phaser` |
| **Fairness** | **Ticket Algorithm** (Strict FIFO) | `Lock(fair=true)` |

---

## Optimization & Atomic Operations

A key feature of this implementation is the usage of **Atomic Operations** (Compare-And-Swap) to build the algorithms.

* **Volatile Variables:** Used to ensure visibility of flags across threads (preventing CPU caching issues).
* **CAS (Compare-And-Swap):** Used in the implementation of the Ticket and Semaphore logic to ensure that state updates are indivisible instructions.

---

## Conclusions

The development of this Concurrent Chat Server demonstrates the trade-offs between implementing custom synchronization algorithms versus using standard libraries. While algorithms like **Bakery** or **Butterfly Barriers** offer deep control and theoretical fairness, Java's **Monitors (`synchronized`)** provide a safer and less error-prone development experience.

By implementing the entire stack—from atomic spin-locks to complex monitors—we achieved a system that is not only thread-safe but also serves as a comprehensive reference for concurrent programming patterns.

---
*Project developed for [Nombre de la Asignatura de Concurrencia] - Computer Engineering.*
