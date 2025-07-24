
# High-Performance Distributed Microservices System

A high-performance distributed system built in Java using microservices and containerized infrastructure. Capable of handling over **5900 requests/sec** during demo testing — nearly **300× improvement** over the initial baseline. Designed for **horizontal scalability**, **modularity**, and **fault-tolerant real-time load handling**.

---

## 🏆 Achievements

- 🥈 **2nd place** in throughput challenge  
- ✅ **Perfect score** on project evaluation
- 🧠 Refactored and optimized entire system beyond team baseline

---

## 🔧 Tech Stack

| Languages             | Frameworks & Tools                | Infrastructure                                     |
|-----------------------|-----------------------------------|----------------------------------------------------|
| Java (Virtual Threads) | Hazelcast, Postgres, Docker, Bash | Nginx (load balancing), Linux (screen), Custom CLI |
| Shell scripting       | Git                               | JVM 21+, Maven (dynamic JAR handling)              |

---

## 🌐 Architecture Overview

This project consists of **four primary microservices**:

- `UserService`: CRUD operations for users  
- `ProductService`: Product catalog with stock management  
- `OrderService`: Order placement, tracking, and purchase history  

**Each service:**

- Operates independently with its own endpoint
- Shares a centralized Postgres database with connection pooling
- Utilizes both in-memory (local) and Hazelcast (distributed) caching
- Exposes HTTP APIs used by a custom load generation and testing engine

A **reverse proxy** powered by **Nginx** distributes load across clusters for horizontal scaling.

---

## 🚀 Performance & Optimization Highlights

| Optimization                                 | Impact                                                    |
|---------------------------------------------|------------------------------------------------------------|
| ⚙️ Java Virtual Threads                      | Reduced thread overhead, unlocked massive concurrency      |
| 🧠 Caching Strategy (Hazelcast + Memory)     | Cut latency by **150%**                                   |
| 💾 DB Write Batching + Indexes              | Improved throughput and reduced I/O load                  |
| 🌐 Nginx Load Balancing                      | Increased request parallelism and fault tolerance          |
| 🔄 Reusable HTTP Clients + Connection Pools | Eliminated recreate/destroy bottlenecks                   |
| 🧼 Logging Optimization                      | Boosted production performance via minimal I/O noise       |
| 🛠️ Codebase Refactor                         | Applied SOLID principles, removed redundancy, added abstractions |
| 🧪 Advanced Testing DSL                      | Enabled complex simulation and performance profiling       |

**Final Benchmark:**
- 🔥 ~5900 req/s at demo
- 🔥 ~10,000 req/s locally in distributed mode  
- 📉 Initial baseline: 27 req/s

---

## 💡 Innovation: Custom Load Testing DSL

Created a **domain-specific language (DSL)** for realistic scenario simulation with support for:

- Loops, variables, and conditionals
- Randomized inputs
- Parallel execution with synchronization control
- Response assertions (HTTP status + body)
- GOTO and flow control
- Timers, delays, and console tracing

> 🔍 **DSL** enabled advanced testing beyond the capabilities of the professor-provided suite.

---

## 📁 Example Scripts

- 📄 **[Basic Test Script](#)**  
- 📄 **[Advanced Test Script (High Load)](#)**

---

## 📸 Demo Snapshot

- 📍 Demo performed on-campus using real-time test loads
- 🧪 Verified persistence, concurrent correctness, and error handling  
- 🔗 **[GitHub Repo](#)**

---

## 🧱 How to Run Locally

### 🐳 Prerequisites

- Linux/macOS or WSL (Windows Subsystem for Linux)
- Java 21+
- Bash, `screen`, and Docker installed

### ⚙️ Compilation

```bash
./runme.sh -c
```

- Downloads JDK 21
- Compiles code
- Pulls dependencies automatically

### 🚀 Start All Services

```bash
./runme.sh -a
```

Each service runs in its own screen session.

Switch between services:
```bash
./runme.sh -su   # UserService
./runme.sh -p    # ProductService
./runme.sh -o    # OrderService
```

Detach from a service screen: `Ctrl + A` then `D`

### 🔄 Stop All Services

```bash
./runme.sh -x
```

---

## 🧠 Key Learnings

- Deep understanding of distributed systems under high load
- Performance profiling and targeted bottleneck optimization
- Real-world software architecture: scalability, resilience, abstraction
- Built developer tooling: custom CLI, config system, testing DSL
- Learned the importance of writing clean, extensible code — not just working code

---

## 🧑‍💻 Contributions & Ownership

Although a team project, the initial codebase was largely non-functional. I independently:

- Refactored all core services
- Designed the caching and load balancing strategy
- Built the DSL testing engine and CLI scripts
- Engineered the final system exceeding performance expectations

---

## 🧩 Configuration System

Flexible **JSON configuration** supports:

- Per-service overrides
- Pool sizes, timeouts, queue lengths
- Cache settings
- Logging verbosity
- Host IP autodetection
- Seamless test environment switching

📄 **[Example Config File](#)**

---

## 📚 Project Background

This was a **capstone-style project** for a university distributed systems course, with goals to:

- Scale microservices to support 1000+ concurrent clients
- Demonstrate state consistency and resiliency under load
- Showcase design thinking and engineering rigor

📄 **[Assignment Details](#)** (include link or PDF)

---

## 📎 Links

- 🔗 **[Live GitHub Repo](#)**
- 📄 **[Advanced Test Script](#)**
- 🛠️ **[Startup Script](#)**

---

## 📢 Contact

**👤 Sam Zhang**  
📬 sam.zhangv1.0@gmail.com  
🔗 [LinkedIn](#) | [GitHub](#)

---
