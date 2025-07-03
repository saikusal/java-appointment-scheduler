# Deployment Guide: Java Appointment Scheduler

This guide provides a complete walkthrough for deploying the Java-based microservices application to a single Amazon Linux EC2 instance using Docker Compose.

## 1. Architectural Flow

When deployed, all services run in a shared Docker network, allowing them to communicate with each other using their service names as hostnames (e.g., `http://schedule-service:8082`).

```mermaid
graph TD
    subgraph "EC2 Instance (Docker Host)"
        subgraph "Docker Network"
            B[Booking Service <br> (Port 8083)]
            S[Schedule Service <br> (Port 8082)]
            U[User Service <br> (Port 8081)]
            DB[(MySQL Database <br> Port 3306)]
        end
    end

    Client[User's Browser / Mobile App] --> B
    Client --> U

    B --> S
    B --> DB
    S --> DB
    U --> DB
```

### Service Responsibilities:

*   **User Service (Port 8081):** Handles user registration and login. It communicates only with the database.
*   **Schedule Service (Port 8082):** Manages provider services and their weekly availability. It communicates only with the database.
*   **Booking Service (Port 8083):** The core orchestrator. It handles requests for available slots by fetching data from the **Schedule Service** and its own database, calculating conflicts, and returning open slots. It also handles the creation of new appointments.

---

## 2. Consolidated Deployment Steps

### Phase 0: Local Git Repository Setup (First-Time Setup)

You are correct, you must have Git installed on your local machine. If you don't have it, you can download it from [git-scm.com](https://git-scm.com/downloads).

Follow these steps in your project's root directory (`C:/Users/ajithsai.kusal/Desktop/JavaAppointmentScheduler`) using a terminal or command prompt.

1.  **Initialize a Git Repository:**
    This command turns your project folder into a Git repository.
    ```bash
    git init -b main
    ```

2.  **Add All Files to Staging:**
    This command prepares all your project files for the first commit.
    ```bash
    git add .
    ```

3.  **Make Your First Commit:**
    This saves a snapshot of your project's current state.
    ```bash
    git commit -m "Initial commit of appointment scheduler microservices"
    ```

4.  **Create a GitHub Repository:**
    *   Go to [GitHub.com](https://github.com) and log in.
    *   Click the "+" icon in the top-right corner and select "New repository".
    *   Give your repository a name (e.g., `java-appointment-scheduler`) and create it. **Do not** initialize it with a README or .gitignore file, as you already have those.

5.  **Link Your Local and Remote Repositories:**
    Copy the repository URL from the GitHub page and run the following command. **Replace the URL with your own.**
    ```bash
    git remote add origin https://github.com/your-username/your-repo.git
    ```

6.  **Push Your Code to GitHub:**
    This command uploads your committed code to the GitHub repository.
    ```bash
    git push -u origin main
    ```

### Phase 1: Preparation (On Your Local Machine)

1.  **Build Docker Images:**
    For each of the three services, navigate into its directory and run the `docker build` command.
    **Note:** Replace `your-docker-repo` with your Docker Hub username or Amazon ECR URI.
    ```bash
    # In user-service/
    docker build -t your-docker-repo/user-service:latest .
    # In schedule-service/
    docker build -t your-docker-repo/schedule-service:latest .
    # In booking-service/
    docker build -t your-docker-repo/booking-service:latest .
    ```

3.  **Push Docker Images:**
    Push the newly built images to your container registry. You may need to log in first (`docker login`).
    ```bash
    docker push your-docker-repo/user-service:latest
    docker push your-docker-repo/schedule-service:latest
    docker push your-docker-repo/booking-service:latest
    ```

### Phase 2: Server Setup (On the EC2 Instance)

4.  **Launch EC2 Instance:**
    *   Launch a new **Amazon Linux 2023** EC2 instance.
    *   Create and assign a **Security Group** that allows inbound traffic for:
        *   **SSH (Port 22)** from your IP address.
        *   **Custom TCP (Port 8081)** from `0.0.0.0/0`.
        *   **Custom TCP (Port 8082)** from `0.0.0.0/0`.
        *   **Custom TCP (Port 8083)** from `0.0.0.0/0`.

5.  **Install Required Software:**
    *   Connect to your EC2 instance via SSH.
    *   Run the following commands:
    ```bash
    # Update packages and install Git & Docker
    sudo dnf update -y
    sudo dnf install git docker -y

    # Start and enable Docker service
    sudo systemctl start docker
    sudo systemctl enable docker
    sudo usermod -a -G docker ec2-user

    # Install Docker Compose
    sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose

    # IMPORTANT: Log out and log back in to apply Docker group permissions
    exit
    ```

### Phase 3: Deployment & Verification (On the EC2 Instance)

6.  **Clone Your Project:**
    After logging back into your EC2 instance, clone your Git repository.
    ```bash
    git clone https://github.com/your-username/your-repo.git
    cd your-repo
    ```

7.  **Launch the Application:**
    Use Docker Compose to pull your images and start all the containers in the background.
    ```bash
    # This command reads the docker-compose.yml file and starts everything
    docker-compose up -d
    ```

8.  **Verify:**
    Check that the services are running correctly.
    ```bash
    # View the logs of all running containers
    docker-compose logs -f

    # Check the status of the containers
    docker-compose ps
    ```
    You can now access the API endpoints using the EC2 instance's public IP address and the corresponding port for each service.
