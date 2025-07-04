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

This phase covers building the application and the Docker images on your local development machine.

1.  **Build Executable JARs:**
    For each of the three services (`user-service`, `schedule-service`, `booking-service`), you must first compile the code and package it into an executable `.jar` file.

    *   **If you have Maven installed locally:** Navigate into each service's directory and run `mvn clean package -DskipTests`.
    *   **If you do NOT have Maven installed locally:** You can use Docker to run the build command. Navigate to the service's directory and run the following, which will save the output to your local machine.
        ```bash
        # Example for user-service:
        docker run --rm -v "$(pwd):/app" -v "$HOME/.m2:/root/.m2" -w /app maven:3.8.5-openjdk-17 mvn clean package -DskipTests
        ```
    Repeat this process for all three services.

2.  **Build Docker Images:**
    With the `.jar` files created in the `target` directory of each service, you can now build the Docker images. This step no longer requires an internet connection as it just copies the local `.jar` file.
    **Note:** Replace `your-docker-repo` with your Docker Hub username or Amazon ECR URI.
    ```bash
    # In user-service/
    docker build -t your-docker-repo/user-service:latest .

    # In schedule-service/
    docker build -t your-docker-repo/schedule-service:latest .

    # In booking-service/
    docker build -t your-docker-repo/booking-service:latest .
    ```

3.  **Create Repositories on Docker Hub:**
    Before you can push your images, you must manually create a repository for each one on the Docker Hub website.
    *   Go to [hub.docker.com](https://hub.docker.com) and log in.
    *   Click **Create Repository**.
    *   Enter the repository name exactly as it appears in your image tag (e.g., `user-service`).
    *   Set the visibility to **Public**.
    *   Click **Create**.
    *   Repeat this process for `schedule-service` and `booking-service`.

4.  **Push Docker Images:**
    Once the repositories exist online, you can push your images. You may need to log in first (`docker login`).
    ```bash
    docker push your-docker-repo/user-service:latest
    docker push your-docker-repo/schedule-service:latest
    docker push your-docker-repo/booking-service:latest
    ```

### Phase 2: Server Setup (On the EC2 Instance)

#### Server Requirements
*   **Instance Type:** A `t2.small` or `t3.small` instance (1 vCPU, 2GB RAM) is the absolute minimum. A **`t2.medium` or `t3.medium` (2 vCPU, 4GB RAM) is strongly recommended** for stable performance. A 1GB RAM instance is not sufficient and will cause services to fail.
*   **Operating System:** Amazon Linux 2023

4.  **Launch EC2 Instance:**
    *   Launch a new EC2 instance based on the requirements above.
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

7.  **Update and Launch the Application:**
    Before launching, you must edit the `docker-compose.yml` file to use your Docker Hub username.
    ```bash
    # Open the file for editing
    nano docker-compose.yml
    ```
    In the editor, change the `image:` line for each service from `your-docker-repo/...` to `saikusal/...`. For example:
    `image: your-docker-repo/user-service:latest`
    becomes
    `image: saikusal/user-service:latest`

    Save the file (Ctrl+O, Enter) and exit (Ctrl+X).

    Now, use Docker Compose to pull your images and start all the containers.
    ```bash
    # Log in to Docker Hub on the server to be able to pull images
    docker login

    # This command reads the updated docker-compose.yml file and starts everything
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
    You can now access the API endpoints using the EC2 instance's public IP address and a corresponding port for each service.

---

## 3. Troubleshooting

### Error: `pipe/dockerDesktopLinuxEngine: The system cannot find the file specified`

*   **Cause:** The Docker command-line tool cannot connect to the Docker Desktop backend engine.
*   **Solution:** Make sure the **Docker Desktop** application is running on your machine before you execute any `docker` commands.

### Error: `Non-resolvable parent POM` or `Could not transfer artifact`

*   **Cause:** This is a network error. Maven, running inside the Docker container, cannot connect to the internet to download dependencies. This is usually caused by a corporate proxy, VPN, or firewall.
*   **Solution:**
    1.  **Check for Proxies/VPNs:** If you are on a corporate network, configure Docker Desktop to use your company's proxy settings (**Settings > Resources > Proxies**).
    2.  **Check Firewalls:** Temporarily disable your firewall to see if it resolves the issue. If it does, add a permanent exception for Docker Desktop.
    3.  **Test Network Connectivity:** Run `docker run --rm busybox ping -c 1 repo.maven.apache.org`. If this fails, it confirms a network issue within Docker.
