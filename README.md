# 🚀 Steganography Web Application (AWS & Cloud Deployment Ready)

A **Spring Boot 3** Web Application built to embed and extract secret text payloads inside PNG images using Least Significant Bit (**LSB**) steganography. Packaged as a dual **executable JAR / Tomcat WAR artifact** ready for deployment on **AWS (Elastic Beanstalk, EC2, App Runner)** and hosting on **GitHub**.

---

## 📁 Project Architecture

```
Jenkins steganography/
├── .gitignore                           # Git ignore rules for Maven artifacts and IDE files
├── pom.xml                              # Maven build configuration (packaging: war / executable jar)
├── README.md                            # Complete setup, GitHub, and AWS deployment guide
└── src/
    ├── main/
    │   ├── java/com/example/stegweb/
    │   │   ├── StegWebApplication.java  # Spring Boot entry point (extends SpringBootServletInitializer)
    │   │   ├── controller/
    │   │   │   └── StegWebController.java # Web UI routes and REST API endpoints (/api/encode, /api/decode)
    │   │   └── service/
    │   │       └── StegService.java     # LSB Steganography Engine (Bitwise payload manipulation)
    │   └── resources/
    │       ├── application.properties   # Spring Boot file upload and server configuration
    │       └── templates/
    │           └── index.html           # Dark obsidian metallic HTML5/CSS3 Web Interface
    └── test/
        └── java/com/example/stegweb/
            └── StegWebApplicationTests.java # JUnit 5 Integration & Unit tests
```

---

## 🛠️ Key Technical Features

1. **LSB Image Steganography Core Engine**:
   - Custom magic header verification (`0x53544547` / `"STEG"`) to detect valid stego images.
   - Bitwise channel modification ($R, G, B$) preserving visual image fidelity.
   - Defensive capacity checking against payload overflow.
2. **AWS Cloud & Tomcat Ready Packaging**:
   - Extends `SpringBootServletInitializer` in `StegWebApplication.java`.
   - Supports deployment as a standalone service (`java -jar`) or hosted WAR inside Tomcat / AWS Elastic Beanstalk.
3. **Obsidian Dark Web Interface**:
   - Modern pitch-black glassmorphism UI.
   - Drag & drop PNG upload with live image preview and capacity calculation.
   - Immediate message encoding with automated PNG file download.

---

## ⚙️ Building & Local Execution

### 1. Build the Deployable Artifact
Run Maven package from the project root directory:
```bash
mvn clean package
```
This generates `target/jenkins-steganography-1.0.0.war` (which functions as both an executable jar and Tomcat WAR).

### 2. Run Standalone Locally
```bash
java -jar target/jenkins-steganography-1.0.0.war
```
Or via Maven:
```bash
mvn spring-boot:run
```
Access the application at: **`http://localhost:8080/`**

---

## ☁️ Deployment Guide for AWS (Amazon Web Services)

### Option 1: AWS Elastic Beanstalk (Tomcat / Java Platform)
1. Open the **AWS Management Console** -> **Elastic Beanstalk**.
2. Click **Create Application**.
3. Set Platform to **Corretto 17** or **Tomcat 10**.
4. Select **Upload your code** and choose `target/jenkins-steganography-1.0.0.war`.
5. Click **Create App**. AWS will automatically provision environment instances and launch your web application.

### Option 2: AWS EC2 Instance (Linux / Ubuntu)
1. Launch an EC2 instance (Amazon Linux 2023 or Ubuntu 22.04 LTS).
2. SSH into your instance and install OpenJDK 17:
   ```bash
   sudo dnf install java-17-amazon-corretto -y  # Amazon Linux
   # OR
   sudo apt update && sudo apt install openjdk-17-jre -y  # Ubuntu
   ```
3. Upload `jenkins-steganography-1.0.0.war` to the EC2 instance via SCP:
   ```bash
   scp -i your-key.pem target/jenkins-steganography-1.0.0.war ec2-user@<your-ec2-ip>:~/
   ```
4. Run the web application:
   ```bash
   nohup java -jar jenkins-steganography-1.0.0.war --server.port=80 &
   ```
5. Ensure your EC2 **Security Group** allows inbound traffic on port 80/8080.

---

## 🐙 Push to GitHub Repository

To push this project to a new repository on your GitHub account:

1. Open terminal inside the `Jenkins steganography` folder.
2. Initialize local Git repository and commit files:
   ```bash
   git init
   git add .
   git commit -m "Initial commit of Steganography Web Application for AWS"
   ```
3. Go to [GitHub.com](https://github.com/new) and create a new repository named `Jenkins-steganography` (or `Jenkins steganography`).
4. Link your local repository to GitHub and push:
   ```bash
   git branch -M main
   git remote add origin https://github.com/YOUR_GITHUB_USERNAME/Jenkins-steganography.git
   git push -u origin main
   ```
