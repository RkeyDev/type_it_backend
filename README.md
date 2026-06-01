![Type it! logo](https://github.com/RkeyDev/type-it-client/blob/main/src/assets/Logo.png)

# Type It!

A real-time, competitive multiplayer web game where vocabulary depth and typing speed dictate victory. Players go head-to-head answering open-ended prompts, earning points directly proportional to the length of their correct answers. The first player to reach the character goal set by the lobby host wins the match.

Live Demo: [playtypeit.com](http://www.playtypeit.com) *(Note: Domain registration valid through late 2026)*

---

## Architecture & Tech Stack

The application leverages a lightweight, low-overhead tech stack designed for instant real-time data push with minimal client-side rendering overhead.

### Frontend
* **Vanilla Stack:**  
  ![HTML5](https://img.shields.io/badge/HTML5-E34F26?style=flat-square&logo=html5&logoColor=white) ![CSS3](https://img.shields.io/badge/CSS3-1572B6?style=flat-square&logo=css3&logoColor=white) ![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?style=flat-square&logo=javascript&logoColor=black)
* **Canvas & UI:** Dynamic DOM manipulations coupled with smooth transitions for real-time player progression tracks.

### Backend & Database
* **Core Platform & Networking:**  
  ![Java](https://img.shields.io/badge/Java-ED8B00?style=flat-square&logo=openjdk&logoColor=white) ![WebSockets](https://img.shields.io/badge/WebSockets-010101?style=flat-square&logo=socket.io&logoColor=white) ![JSON](https://img.shields.io/badge/JSON-000000?style=flat-square&logo=json&logoColor=white)
  * Utilizes WebSockets (JSR 356 / Java API for WebSocket) for bidirectional, full-duplex communication channels. 
  * All client-server communication is serialized as uniform JSON payloads for structured request and response handling.
* **Persistence Layer:**  
  ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=flat-square&logo=postgresql&logoColor=white)
  * Utilized to store the comprehensive relational schema of open-ended trivia prompts and their corresponding valid answer sets.

### Data Engineering & Pipeline
* **Automated Data Ingestion:**  
  ![Python](https://img.shields.io/badge/Python-3776AB?style=flat-square&logo=python&logoColor=white)
  * A specialized Python ETL script was engineered to parse massive, unstructured raw text files containing categorical answers. The script normalizes, maps, and batches these datasets, seeding them programmatically into the PostgreSQL instance as structured questions with pre-validated answer matrices.

### DevOps & Infrastructure
* **Containerization:**  
  ![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white)
  * The entire ecosystem is fully containerized, isolation-tested, and orchestrated locally via Docker Compose.

### Real-Time State Management
To achieve ultra-low latency updates when a player submits a correct answer or changes state, the backend bypasses heavy framework abstractions:
* Active client sockets are registered and managed inside an in-memory `ConcurrentHashMap`.
* **State Serialization:** Game state packets and player synchronization frames are built and pushed instantly down the active pipelines as structured JSON text frames.
* **Scalability Note:** The current architecture operates as a single-node stateful server. Client connections are pinned to the memory space of the host machine, meaning it does not scale horizontally out of the box without an external message broker (e.g., Redis Pub/Sub) to synchronize state across multiple application nodes.

---

## Gameplay & Core Features

### 1. Player Onboarding
Intuitive profile creation allowing users to choose custom nicknames and unique vector avatars during player onboarding.
![Login](https://raw.githubusercontent.com/RkeyDev/type-it-client/refs/heads/main/src/assets/tutorial-images/login.png)

---

### 2. Customizable Lobbies & Matchmaking
Host controls for match settings including Typing Time limit (5s - 120s), Character Goal threshold (50 - 1000 chars), and Matchmaking toggles. Full state synchronization across up to 8 players per room using raw WebSocket connections.

| Host View | Guest View |
| :---: | :---: |
| ![Lobby Host](https://github.com/RkeyDev/type-it-client/blob/main/src/assets/tutorial-images/room-creation.png) | ![Lobby Non Host](https://raw.githubusercontent.com/RkeyDev/type-it-client/main/src/assets/tutorial-images/wait-for-game.png) |

---

### 3. Dynamic Gameplay & Scoring Mechanics
Open-ended trivia/category challenges (e.g., *"Name an animal"*, *"Name a hot dish"*). Every character of a valid answer equals 1 point ($1 \text{ character} = 1 \text{ point}$). Long, accurate answers are rewarded over short ones, with real-time progression tracked via a dynamic canvas.

![Game start](https://raw.githubusercontent.com/RkeyDev/type-it-client/main/src/assets/tutorial-images/game-start.png)
![Answered Correctly](https://raw.githubusercontent.com/RkeyDev/type-it-client/main/src/assets/tutorial-images/longest-answer.png)

---

### 4. Victory Conditions
The first player to hit or exceed the lobby's character threshold triggers the match-end state, rendering the final standings for all connected clients simultaneously.
![Player Win](https://raw.githubusercontent.com/RkeyDev/type-it-client/main/src/assets/tutorial-images/winning-screen.png)

---

## Installation & Local Deployment

The entire ecosystem is containerized using Docker, abstracting away local Java environments or port conflicts.

### Prerequisites
* [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed and running.

### Spin Up Locally

1. Clone the repository to your local system:
  ```bash
git clone [https://github.com/RkeyDev/type-it.git](https://github.com/RkeyDev/type-it.git)
cd type-it
  ```
2. Boot both the Java WebSocket backend server and the web client using Docker Compose:
  ```bash
docker compose up --build
  ```
3. Open your browser and navigate to:
  ```bash
http://localhost:8080
```
  (Or the custom port specified within the local compose file configuration)
  
