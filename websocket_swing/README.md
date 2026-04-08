
# 🖥️ Java Swing Project Setup Guide

Follow these steps to get your Java Swing project up and running:

---

## 🔧 Prerequisites

- **Java SDK**: Version **>= 22.0.1**
- **IDE**: IntelliJ IDEA (recommended)
- **Docker**: Docker Desktop or Rancher Desktop

---

## 🚀 Getting Started

### 1. Set Java SDK in IntelliJ

> **Path:** `File > Project Structure > Project`

- Set the **Project SDK** to **Java >= 22.0.1**

---

### 2. Start the HTTPBin Docker Container

Make sure Docker or Rancher is running, then execute:

```bash
docker run -p 8080:80 kennethreitz/httpbin 
```

---

### 3. 🗂️ Configure Content Root in IntelliJ

> **Menu Path:** `File > Project Structure > Modules`

1. Remove all existing **Content Root Entries**
2. + Add a new Content Root:

```text
websocket_swing/swing/src/main/java
```


---

### 4. ▶️ Run the Application

1. Run Main.java

```text
websocket_swing/swing/src/main/java/com
```

