# AI Interaction Spring Boot Project

This project provides a Spring Boot application designed to interact with AI models, specifically focusing on conversational capabilities and maintaining interaction history. It's built with robustness, scalability, and maintainability in mind, demonstrating modern application development practices.

## Table of Contents
- [AI Interaction Spring Boot Project](#ai-interaction-spring-boot-project)
  - [Table of Contents](#table-of-contents)
  - [Project Overview](#project-overview)
  - [Features](#features)
  - [Technologies Used](#technologies-used)
  - [Setup Instructions](#setup-instructions)
    - [Prerequisites](#prerequisites)
    - [Clone the Repository](#clone-the-repository)
    - [API Key Configuration](#api-key-configuration)
    - [Database Configuration](#database-configuration)
  - [Running the Application](#running-the-application)
    - [1. Local Development (IDE/Maven)](#1-local-development-idemaven)
    - [2. Using Docker Compose](#2-using-docker-compose)
  - [API Endpoints](#api-endpoints)
    - [`POST /api/ai/interact`](#post-apiaiiinteract)
    - [`GET /history/{interactionId}`](#get-historyinteractionid)
  - [Docker Integration](#docker-integration)
  - [Future Enhancements & Concepts Explored](#future-enhancements--concepts-explored)
  - [Contributing](#contributing)
  - [License](#license)

## Project Overview

The primary goal of this project is to serve as a backend service for AI interactions, capable of processing user messages, maintaining conversation context, and providing structured responses. It's designed to be a foundational component for intelligent assistants or chatbots.

## Features

*   **Contextual Conversation Management**: Stores and retrieves conversation history to enable more coherent and continuous interactions.
*   **Robust Error Handling & Retry Logic**: Implements retry mechanisms for external AI API calls to enhance reliability.
*   **Input Validation**: Ensures incoming messages and interaction IDs meet basic requirements.
*   **Auditing & Logging**: Comprehensive logging for tracking interactions and debugging.
*   **Asynchronous Processing**: Handles AI interactions asynchronously to improve application responsiveness.
*   **Database Persistence**: Stores conversation messages in an H2 in-memory database (default configuration).
*   **Dockerized Deployment**: Provides Dockerfile and Docker Compose configurations for easy setup and consistent environments.

## Technologies Used

*   **Spring Boot**: Framework for building robust, stand-alone, production-grade Spring applications.
*   **Spring Data JPA**: For easy database interaction and persistence.
*   **H2 Database**: An in-memory relational database for development and testing.
*   **Maven**: Dependency management and build automation tool.
*   **Docker**: Containerization for consistent development and deployment environments.
*   **Gemini API**: (Conceptual/External) The target AI model for interactions.
*   **Lombok**: Reduces boilerplate code.
*   **Java 17**: The programming language version.

## Setup Instructions

### Prerequisites

*   **Java Development Kit (JDK) 17** or higher
*   **Maven 3.6** or higher
*   **Docker Desktop** (if running with Docker)
*   An **IDE** like IntelliJ IDEA, VS Code, or Eclipse

### Clone the Repository

```bash
git clone https://github.com/your-username/ai-project.git # Replace with your actual repo URL
cd ai-project
```

### API Key Configuration

This project is designed to interact with external AI models. You will need API keys for these services.

Create an `application.properties` file (or `application-dev.properties`) in `src/main/resources/` and add your API keys:

```properties
# Gemini API Key
gemini.api.key=YOUR_GEMINI_API_KEY
gemini.model.name=gemini-pro # Or your preferred Gemini model

# If using other AI models, configure their keys here
# openai.api.key=YOUR_OPENAI_API_KEY
# groq.api.key=YOUR_GROQ_API_KEY
```
**Important**: Replace `YOUR_GEMINI_API_KEY` with your actual API key. For production environments, use secure methods like environment variables or a secrets management service (e.g., Azure Key Vault, HashiCorp Vault).

### Database Configuration

By default, the project uses an H2 in-memory database, which is automatically configured by Spring Boot. No additional setup is required for H2.

```properties
# Default H2 Configuration (for local development)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
spring.datasource.url=jdbc:h2:mem:ai_project_db
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
```

## Running the Application

### 1. Local Development (IDE/Maven)

1.  **Build the project**:
    ```bash
    mvn clean install
    ```
2.  **Run from your IDE**: Open the project in your IDE and run the `InteractionApplication.java` class.
3.  **Run from terminal**:
    ```bash
    mvn spring-boot:run
    ```
The application will start on `http://localhost:8080`.

### 2. Using Docker Compose

This method provides a consistent environment and is recommended for local testing.

1.  **Ensure Docker Desktop is running.**
2.  **Set Environment Variables**: Your `docker-compose.yml` expects API keys to be set in your shell environment.
    *   **Linux/macOS**:
        ```bash
        export GEMINI_API_KEY="your_gemini_api_key_here"
        export OPENAI_API_KEY="your_openai_api_key_here" # If applicable
        export GROQ_API_KEY="your_groq_api_key_here" # If applicable
        ```
    *   **Windows (Command Prompt)**:
        ```cmd
        set GEMINI_API_KEY="your_gemini_api_key_here"
        set OPENAI_API_KEY="your_openai_api_key_here"
        set GROQ_API_KEY="your_groq_api_key_here"
        ```
    *   **Windows (PowerShell)**:
        ```powershell
        $env:GEMINI_API_KEY="your_gemini_api_key_here"
        $env:OPENAI_API_KEY="your_openai_api_key_here"
        $env:GROQ_API_KEY="your_groq_api_key_here"
        ```
    *(Replace with your actual API keys. For local testing, you might use dummy values if the service is mocked.)*

3.  **Build and Run the Container**:
    *   Navigate to the project root directory in your terminal (where `Dockerfile` and `docker-compose.yml` are located).
    *   Run:
        ```bash
        docker-compose up --build
        ```
    This command will build the Docker image for your application and start the container. The application will be accessible at `http://localhost:8080`.

4.  **Stop the Container**:
    *   To stop the running container, press `Ctrl+C` in the terminal where `docker-compose up` is running.
    *   To stop and remove the container (but keep the image), run:
        ```bash
        docker-compose down
        ```

## API Endpoints

### `POST /api/ai/interact`

Sends a message to the AI model and retrieves a response, maintaining conversation history.

*   **Method**: `POST`
*   **URL**: `http://localhost:8080/api/ai/interact`
*   **Request Parameters**:
    *   `message` (String, required): The user's message to the AI.
    *   `interactionId` (String, required): A unique identifier for the conversation session.
*   **Example `curl` command**:
    ```bash
    curl -X POST "http://localhost:8080/api/ai/interact?message=Hello%2C%20how%20are%20you%3F&interactionId=user123"
    ```

### `GET /history/{interactionId}`

Retrieves the conversation history for a specific interaction ID.

*   **Method**: `GET`
*   **URL**: `http://localhost:8080/history/{interactionId}`
*   **Path Variable**:
    *   `interactionId` (String, required): The unique identifier for the conversation session.
*   **Example `curl` command**:
    ```bash
    curl "http://localhost:8080/history/user123"
    ```

## Docker Integration

The project includes:
*   **`Dockerfile`**: Defines the steps to build a lightweight Docker image for the Spring Boot application using a multi-stage build process.
*   **`docker-compose.yml`**: Orchestrates the running of the `ai-app` service as a Docker container, mapping ports and passing environment variables.

## Future Enhancements & Concepts Explored

During the development process, several advanced capabilities were explored and prototyped, demonstrating a broad understanding of modern AI application development and DevOps practices. While not all are active in the current codebase for simplicity, they represent potential future enhancements:

*   **Deep Research Agent**: An LLM-powered agent capable of multi-step reasoning and information synthesis.
*   **Tool Use / Function Calling**: Enabling the AI to invoke external Java methods or REST APIs (e.g., `getOrderStatus`, `getMedicationInfo`, `createOrder`).
*   **Dynamic Image Generation**: Using multi-modal LLMs to generate images based on text prompts.
*   **Retrieval Augmented Generation (RAG)**: Integrating vector stores and embedding models to ground LLM responses in specific knowledge bases, including advanced techniques like query rewriting.
*   **Monitoring Stack**: Setting up Prometheus and Grafana for comprehensive application observability within a Dockerized environment.
*   **CI/CD Pipeline**: Defining `azure-pipelines.yaml` for automated build, test, code quality analysis (SonarQube), security scanning (OWASP Dependency-Check, Trivy), and deployment to Azure Kubernetes Service (AKS).

## Contributing

Feel free to fork the repository, open issues, and submit pull requests.

## License

This project is licensed under the MIT License. See the `LICENSE` file for details.
