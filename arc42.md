# Arc42 Architecture Documentation – SwingApp PoC

> **Template:** arc42 – Architecture Communication and Documentation  
> **Version:** 1.0  
> **Date:** 2026-04-08  
> **Status:** Initial Draft

---

## Table of Contents

1. [Introduction and Goals](#1-introduction-and-goals)
2. [Constraints](#2-constraints)
3. [System Scope and Context](#3-system-scope-and-context)
4. [Solution Strategy](#4-solution-strategy)
5. [Building Block View](#5-building-block-view)
6. [Runtime View](#6-runtime-view)
7. [Deployment View](#7-deployment-view)
8. [Crosscutting Concepts](#8-crosscutting-concepts)
9. [Architecture Decisions](#9-architecture-decisions)
10. [Quality Requirements](#10-quality-requirements)
11. [Risks and Technical Debt](#11-risks-and-technical-debt)
12. [Glossary](#12-glossary)

---

## 1. Introduction and Goals

### 1.1 Purpose

This document describes the architecture of the **SwingApp PoC** (Proof of Concept), a project that demonstrates how a legacy Java Swing desktop application ("Allegro") can be gradually modernized by integrating a browser-based frontend while keeping the existing desktop application alive and receiving data in real time.

### 1.2 Requirements Overview

| ID | Requirement |
|----|-------------|
| R1 | A user can search for persons (customers) in a web browser UI. |
| R2 | A selected search result can be transferred from the browser to the legacy Swing desktop application. |
| R3 | Free-text input in the browser can be mirrored to a text area in the Swing application. |
| R4 | The Swing application can independently send form data to a backend HTTP API (Allegro modernization endpoint). |
| R5 | The system shall operate as a PoC; production-level reliability is not required. |

### 1.3 Quality Goals

| Priority | Quality Goal | Motivation |
|----------|-------------|------------|
| 1 | **Interoperability** | Browser client and Swing desktop must communicate in real time. |
| 2 | **Simplicity** | The PoC should be easy to understand and set up. |
| 3 | **Extensibility** | The MVP pattern in the Swing client should allow clean separation for future growth. |

### 1.4 Stakeholders

| Role | Expectation |
|------|-------------|
| Developer / Architect | Understand how modern web tech can integrate with legacy Swing. |
| Project Owner | See a working PoC that transfers data from browser to desktop. |
| Future Modernization Team | Use this PoC as a starting point for full migration. |

---

## 2. Constraints

### 2.1 Technical Constraints

| Constraint | Background |
|------------|------------|
| Java 22, Maven | The Swing client is built with Java 22 and managed via Maven (`pom.xml`). |
| Java Swing (javax.swing) | The desktop UI relies on the classic Java Swing UI toolkit. |
| Node.js WebSocket server | The relay server is implemented in Node.js using the `websocket` library. |
| Vue.js 2 | The browser client is built with Vue.js 2 (Vue CLI). |
| WebSocket protocol (RFC 6455) | Browser↔Server and Server↔Desktop communication uses WebSockets. |
| HTTP/JSON | The Swing application posts form data to a backend REST endpoint via plain `HttpURLConnection`. |

### 2.2 Organisational Constraints

| Constraint | Background |
|------------|------------|
| PoC scope | The project is explicitly a PoC; no test suite, CI/CD pipeline, or production hardening is expected. |
| Hard-coded data | The Vue.js client uses an in-memory static dataset for search (no real database). |
| Single-developer setup | The entire system is intended to run on one developer machine. |

---

## 3. System Scope and Context

### 3.1 Business Context

```
┌─────────────────────────────────────────────────────────────────┐
│                         SwingApp PoC                            │
│                                                                 │
│  [Browser User] ──search/select──> [Vue.js Client]             │
│                                         │                       │
│                                    WebSocket                    │
│                                         │                       │
│                                  [Node.js Server]               │
│                                         │                       │
│                                    WebSocket                    │
│                                         │                       │
│                               [Java Swing Client]               │
│                                         │                       │
│                                       HTTP                      │
│                                         │                       │
│                               [Backend API / httpbin]           │
└─────────────────────────────────────────────────────────────────┘
```

| Neighbour | Communication | Description |
|-----------|---------------|-------------|
| Browser User | HTTP (Vue.js) | Human operator using the web UI. |
| Backend API (`http://localhost:8080/post`) | HTTP POST / JSON | Receives form data submitted from the Swing application ("Anordnen" button). |

### 3.2 Technical Context

| Channel | Technology | Port |
|---------|-----------|------|
| Browser → Node.js WS Server | WebSocket (ws://) | 1337 |
| Swing Client → Node.js WS Server | WebSocket (Tyrus client) | 1337 |
| Swing Client → Backend API | HTTP POST | 8080 |
| Browser → Vue Dev Server | HTTP | 8080 (Vue CLI default) |

---

## 4. Solution Strategy

| Goal | Strategy |
|------|----------|
| Bridge browser and desktop | A central Node.js WebSocket server acts as a message broker. All clients connect to it; messages are broadcast to all connected clients. |
| Keep Swing application alive | The Swing application connects to the WebSocket server as a WebSocket client (using Tyrus), and updates its UI fields when a message arrives. |
| Clean Swing internals | The newer Swing entry point (`com.Main`) uses an **MVP (Model–View–Presenter)** pattern with data binding, event emission, and an HTTP service layer. |
| Easy integration test via API spec | An OpenAPI 3.0 specification (`api.yml`) documents the backend POST endpoint. |

---

## 5. Building Block View

### 5.1 Level 1 – System Overview

```
┌──────────────────────────────────────────────────────────────────┐
│  SwingApp PoC                                                    │
│                                                                  │
│  ┌─────────────────┐    WS    ┌──────────────────┐              │
│  │  node-vue-client │◄───────►│  node-server      │             │
│  │  (Vue.js 2 SPA)  │         │  (WS Relay)       │             │
│  └─────────────────┘         └────────┬─────────┘              │
│                                        │ WS                      │
│                               ┌────────▼─────────┐              │
│                               │  swing           │              │
│                               │  (Java Swing App)│              │
│                               └────────┬─────────┘              │
│                                        │ HTTP                    │
│                               ┌────────▼─────────┐              │
│                               │  Backend API     │              │
│                               │  (localhost:8080)│              │
│                               └──────────────────┘              │
└──────────────────────────────────────────────────────────────────┘
```

### 5.2 Level 2 – node-vue-client (Vue.js SPA)

| Component | File | Responsibility |
|-----------|------|----------------|
| `App` | `src/App.vue` | Root component; renders header and `Search` component. |
| `Search` | `src/components/Search.vue` | Search form, result tables, WebSocket connect/send logic. |

**Key behaviours:**
- On mount, connects to `ws://localhost:1337/`.
- `searchPerson()` filters the in-memory `search_space` array.
- `sendMessage()` sends a JSON object `{ target, content }` over the WebSocket.
- The `textarea` watcher sends text changes to target `"textarea"`.
- The "Nach ALLEGRO übernehmen" button sends selected result to target `"textfield"`.

### 5.3 Level 2 – node-server (WebSocket Relay)

| Component | File | Responsibility |
|-----------|------|----------------|
| WebSocket Server | `src/WebsocketServer.js` | Accepts all incoming WS connections and broadcasts every received UTF-8 message to all connected clients. |

**Key behaviours:**
- Listens on port **1337** over HTTP/WebSocket.
- Maintains a list of all active connections (`clients[]`).
- Any message received from any client is echoed/broadcast to all clients (including sender).

### 5.4 Level 2 – swing (Java Swing Application)

The `swing` module contains **two** independent entry points:

#### Entry Point 1: `websocket.Main` (original PoC)

| Component | Description |
|-----------|-------------|
| `websocket.Main` | Initialises the Swing UI and connects a WebSocket client to `ws://localhost:1337/`. |
| `WebsocketClientEndpoint` | Inner class annotated `@ClientEndpoint` (Tyrus). Handles `onOpen`, `onClose`, `onMessage`. |
| `Message` | Simple value type: `{target, content}`. |
| `SearchResult` | Value type holding all person/payment fields extracted from JSON. |

On message receipt:
- `target == "textarea"` → sets `textArea` text.
- `target == "textfield"` → parses full JSON into `SearchResult` and fills all text fields.

#### Entry Point 2: `com.Main` (MVP-structured PoC)

```
com.Main
  ├── PocView          – Swing JFrame with all UI components
  ├── EventEmitter     – Simple in-process event bus
  ├── PocModel         – Data model + HttpBinService call
  └── PocPresenter     – Binds View ↔ Model; listens to EventEmitter
```

| Class | Package | Responsibility |
|-------|---------|----------------|
| `Main` | `com` | Bootstrap: creates View, EventEmitter, Model, Presenter; awaits latch. |
| `PocView` | `com.poc.presentation` | All Swing UI declarations and layout (GridBagLayout). |
| `PocPresenter` | `com.poc.presentation` | Two-way data binding (View → Model via `DocumentListener`/`ChangeListener`; Model → View via `EventEmitter` subscription). |
| `PocModel` | `com.poc.model` | Holds `ValueModel<?>` map for all form fields; calls `HttpBinService.post()` on `action()`. |
| `HttpBinService` | `com.poc.model` | Posts all model fields as JSON to `http://localhost:8080/post`. |
| `EventEmitter` | `com.poc.model` | Pub/sub: `subscribe(EventListener)` and `emit(String)`. |
| `EventListener` | `com.poc.model` | Functional interface: `onEvent(String)`. |
| `ValueModel<T>` | `com.poc` | Generic wrapper for a single typed field. |
| `ModelProperties` | `com.poc.model` | Enum of all model property keys. |

---

## 6. Runtime View

### 6.1 Scenario: User Searches and Transfers a Person to the Swing App

```
Browser User   Vue.js Client     Node.js WS Server   Swing App (websocket.Main)
     │               │                   │                       │
     │ type & click  │                   │                       │
     │──searchPerson>│                   │                       │
     │               │ (local filter)    │                       │
     │<─search result│                   │                       │
     │               │                   │                       │
     │ select row    │                   │                       │
     │──selectResult>│                   │                       │
     │               │                   │                       │
     │ click button  │                   │                       │
     │──sendMessage──►──{target:"textfield", content:{...}}──►  │
     │               │                   │──broadcast to all──►  │
     │               │                   │                  onMessage()
     │               │                   │                  fill text fields
```

### 6.2 Scenario: User Types in Textarea

```
Browser User   Vue.js Client     Node.js WS Server   Swing App
     │               │                   │                │
     │ type text     │                   │                │
     │──(watch)──────►──{target:"textarea", content:"..."}►│
     │               │                   │──broadcast──►  │
     │               │                   │           onMessage()
     │               │                   │           textArea.setText(...)
```

### 6.3 Scenario: User Submits Form in Swing App (MVP version)

```
User      PocView      PocPresenter      PocModel       HttpBinService   Backend API
  │           │               │               │                │               │
  │ click     │               │               │                │               │
  │─button───►│               │               │                │               │
  │           │─ActionEvent──►│               │                │               │
  │           │               │─model.action()►               │               │
  │           │               │               │──post(data)───►│               │
  │           │               │               │               │──HTTP POST────►│
  │           │               │               │               │◄──response─────│
  │           │               │               │◄─responseBody──│               │
  │           │               │               │─eventEmitter.emit(responseBody)│
  │           │◄──textArea.setText(responseBody)               │               │
```

---

## 7. Deployment View

### 7.1 Local Developer Machine (Single Node)

```
┌──────────────────────────────────────────────────────────────────┐
│  Developer Machine                                               │
│                                                                  │
│  ┌──────────────┐   ┌──────────────┐   ┌──────────────────────┐ │
│  │ Browser      │   │ Node.js      │   │ JVM (Java 22)        │ │
│  │ (Chrome/FF)  │   │ WS Server    │   │                      │ │
│  │              │   │ :1337        │   │  swing JAR           │ │
│  │  Vue.js SPA  │   │              │   │  (Swing GUI)         │ │
│  │  (:8080 dev) │   │              │   │                      │ │
│  └──────┬───────┘   └──────┬───────┘   └──────────┬───────────┘ │
│         │   ws://localhost:1337   │                │             │
│         └────────────────────────┘                │             │
│                                     ws://localhost:1337          │
│                                                   │             │
│                           ┌───────────────────────┘             │
│                           ▼                                     │
│                  ┌──────────────────┐                           │
│                  │ Backend API      │                           │
│                  │ :8080            │                           │
│                  │ (httpbin / mock) │                           │
│                  └──────────────────┘                           │
└──────────────────────────────────────────────────────────────────┘
```

### 7.2 Start-up Order

1. **Start Node.js WebSocket server:**  
   ```bash
   cd websocket_swing/node-server
   npm install
   node src/WebsocketServer.js
   ```

2. **Start Vue.js dev server:**  
   ```bash
   cd websocket_swing/node-vue-client
   yarn install
   yarn serve
   ```

3. **Build and run Java Swing application:**  
   ```bash
   cd websocket_swing
   mvn package
   java -cp target/websocket_swing-0.0.1-SNAPSHOT.jar websocket.Main
   # OR (MVP version)
   java -cp target/websocket_swing-0.0.1-SNAPSHOT.jar com.Main
   ```

4. **Open browser:** `http://localhost:8080`

---

## 8. Crosscutting Concepts

### 8.1 Data Model (Message Format)

All WebSocket messages exchanged between the Vue.js client and the Swing application use a common JSON envelope:

```json
{
  "target": "<textarea|textfield>",
  "content": "<string or object>"
}
```

| `target` | `content` type | Effect in Swing |
|----------|---------------|-----------------|
| `"textarea"` | `string` | Sets the Swing `JTextArea` content. |
| `"textfield"` | JSON object (person + payment) | Parses person fields and fills all `JTextField` components. |

### 8.2 Data Binding (MVP Pattern)

The `com.Main` version implements a manual two-way data binding:

- **View → Model:** `DocumentListener` and `ChangeListener` are attached to every form field in `PocPresenter.initializeBindings()`. Any change updates the corresponding `ValueModel<?>` in `PocModel`.
- **Model → View:** `EventEmitter.emit()` triggers all registered `EventListener` callbacks, which update the View directly on the Swing event thread.

### 8.3 Error Handling

- The Node.js server does not perform JSON validation; malformed messages are broadcast as-is.
- The Swing WebSocket client (`websocket.Main`) wraps connection and latch logic in `try/catch` and rethrows as `RuntimeException`.
- `PocModel.action()` rethrows `IOException` and `InterruptedException` from `PocPresenter` as `RuntimeException`.
- No retry logic or circuit breaker is implemented (PoC scope).

### 8.4 Threading

- The Swing GUI must only be updated on the **Event Dispatch Thread (EDT)**. In `websocket.Main`, `onMessage` updates Swing components directly from the WebSocket callback thread — this is a known PoC simplification and would need `SwingUtilities.invokeLater()` in production code.
- The `com.Main` version also updates Swing components from the `EventEmitter` callback thread, which carries the same risk.

---

## 9. Architecture Decisions

### AD-01: WebSocket as Integration Channel

**Decision:** Use WebSocket (RFC 6455) as the real-time communication channel between the web frontend and the desktop application.

**Rationale:** WebSocket is natively supported by modern browsers (no plugin needed) and is straightforward to use from Java via Tyrus. It enables bidirectional, low-latency messaging.

**Alternatives considered:** REST polling (higher latency, more complex); shared database (overkill for PoC).

---

### AD-02: Node.js Relay Server

**Decision:** Introduce a Node.js server as a central WebSocket relay/broker rather than a direct P2P connection.

**Rationale:** Browsers cannot initiate incoming TCP connections to desktop applications. A central server solves the reachability problem and decouples the two clients. Node.js is lightweight, has excellent WebSocket library support, and is fast to set up.

---

### AD-03: MVP Pattern in Swing Application

**Decision:** The refactored Swing entry point (`com.Main`) uses the **Model–View–Presenter** pattern.

**Rationale:** Separating the Swing GUI code (`PocView`) from data handling (`PocModel`) and coordination logic (`PocPresenter`) improves testability and maintainability. This is the recommended pattern for Swing applications that need to grow.

---

### AD-04: Static In-Memory Search Data in Vue.js Client

**Decision:** Person search data is hardcoded as a JavaScript array in `Search.vue`.

**Rationale:** This is sufficient for the PoC. A real system would connect to a backend search API.

---

### AD-05: Two Independent Entry Points in `swing` Module

**Decision:** The `swing` Maven module contains two independent `main` classes: `websocket.Main` (original flat structure) and `com.Main` (MVP-based refactoring).

**Rationale:** The original entry point was kept for reference during incremental refactoring. In a production project, the legacy `websocket.Main` would be removed once `com.Main` reaches feature parity.

---

## 10. Quality Requirements

### 10.1 Quality Tree

```
Quality
├── Interoperability
│   └── Browser and Swing app exchange messages in real time via WebSocket
├── Simplicity
│   └── System can be started on a single developer machine with three commands
├── Maintainability
│   └── MVP separation in Swing module
│   └── OpenAPI spec documents the backend interface
└── Portability
    └── Java 22 + Node.js run on Windows, macOS, Linux
```

### 10.2 Quality Scenarios

| ID | Quality | Scenario | Expected Result |
|----|---------|----------|-----------------|
| Q1 | Interoperability | User selects a person in the browser and clicks "Nach ALLEGRO übernehmen". | Within 1 s, all text fields in the Swing app are populated. |
| Q2 | Simplicity | A new developer clones the repo and follows the README. | All three components are running and communicating within 10 min. |
| Q3 | Maintainability | Developer adds a new form field to `PocModel`. | Only `ModelProperties`, `PocModel`, `PocView`, and `PocPresenter` need changes. |

---

## 11. Risks and Technical Debt

| ID | Risk / Debt | Severity | Mitigation / Note |
|----|------------|----------|-------------------|
| R1 | Swing components updated outside the EDT | High | Use `SwingUtilities.invokeLater()` for all WebSocket-triggered UI updates. |
| R2 | No authentication on the WebSocket server | Medium | Any client on the local network can connect and send/receive messages. Accept for PoC; add auth for production. |
| R3 | Hardcoded URLs and ports (`ws://localhost:1337`, `http://localhost:8080`) | Medium | Move to configuration files or environment variables for real deployments. |
| R4 | WebSocket server broadcasts to *all* clients including the sender | Low | Acceptable for PoC; would cause echo in a multi-user scenario. |
| R5 | Two parallel entry points (`websocket.Main` and `com.Main`) in the same module | Low | Leads to confusion. Remove legacy `websocket.Main` once MVP version is complete. |
| R6 | No tests (unit or integration) | High | Acceptable for PoC; add JUnit tests for `PocModel`, `HttpBinService`, and `PocPresenter` before production. |
| R7 | Tyrus WebSocket library version mismatch in `pom.xml` | Low | `websocket-api 0.2` and `tyrus-websocket-core 1.2.1` are outdated compared to `tyrus-standalone-client 1.15`. Align versions. |
| R8 | `ValueModel.getField()` can return `null` and `toString()` is called on it in `PocModel.action()` | Medium | Add null guard or default values in `ValueModel`. |

---

## 12. Glossary

| Term | Definition |
|------|-----------|
| **Allegro** | The name of the legacy desktop application being modernized. Used as the window title and button label in the Swing GUI. |
| **PoC** | Proof of Concept — a minimal implementation to validate a technical approach. |
| **WebSocket** | A full-duplex communication protocol over a single TCP connection, standardized in RFC 6455. |
| **Tyrus** | The reference implementation of JSR-356 (Java API for WebSocket), used here as a WebSocket client library in the Swing application. |
| **MVP** | Model–View–Presenter design pattern, used in the refactored Swing module. |
| **EDT** | Event Dispatch Thread — the single thread in Java Swing responsible for all UI updates. |
| **Node.js** | A JavaScript runtime used here to implement the WebSocket relay server. |
| **Vue.js** | A progressive JavaScript framework used for the browser-based search UI. |
| **Zahlungsempfänger** | German: "payment recipient" — represents the bank account (IBAN/BIC) data associated with a person. |
| **httpbin** | A free HTTP request-and-response service used as a simple backend mock during development (`http://localhost:8080`). |
| **OpenAPI** | An API description standard (formerly Swagger); the `api.yml` file documents the backend POST endpoint. |
