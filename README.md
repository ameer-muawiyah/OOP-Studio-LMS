# 🎓 OOP Studio: Advanced Learning Management System

> A full-stack, adaptive Learning Management System designed to assess and track Object-Oriented Programming proficiency through dynamic quizzes and live code execution. Developed as a 2nd-Semester Computer Science project at UET Peshawar.

---

## 🌟 Overview
**OOP Studio** bridges the gap between theoretical knowledge and practical application. Moving away from traditional, static testing, this platform introduces an adaptive testing environment built on a modern Spring Boot web architecture. It features a custom "Quantum Developer" dark-tech UI, real-time exam timers, and a secure Java backend that grades student code submissions on the fly.

---

## 🚀 Key Features

### 🧠 Adaptive MCQ Engine
* **Dynamic Difficulty:** Questions are categorized into EASY, MEDIUM, and HARD tiers, specifically focusing on core OOP concepts (Inheritance, Polymorphism, Encapsulation).
* **Live Session Timers:** Integrated 20-minute countdown timers that automatically submit the exam when the clock reaches 00:00.
* **Deferred Initialization:** Timers are bound to user actions (starting only when the first answer is selected) to ensure a stress-free reading period.

### 💻 Live Code Runner Workspace
* **In-Browser IDE:** A dedicated workspace for writing and executing Java code directly in the browser.
* **Secure Exam Locking:** The workspace is locked behind a "Start Test" explicit consent button, ensuring fair testing environments.
* **Pre-loaded Lab Tasks:** Features beginner-friendly lab concepts (e.g., *Sum of Two Numbers*, *Even or Odd*, *Factorials*) complete with automated test case validation.

### 📊 Professor Command Center
* **Supervisor Dashboard:** A secure login portal where professors can view real-time data rows of their assigned students.
* **Instant Roster Sync:** Built using a hybrid Thymeleaf and REST API flow to dynamically update student-to-teacher mappings.

---

## 🛠️ Tech Stack & Architecture

This project was intentionally built as a Web Application rather than a traditional desktop GUI to mirror modern enterprise industry standards.

* **Frontend Engine:** HTML5, CSS3 (Glassmorphism aesthetics), Vanilla JavaScript
* **Server-Side Rendering:** Thymeleaf (Dynamic data injection via Spring Models)
* **Client-Side Async:** JavaScript Fetch API (Real-time REST endpoint queries)
* **Backend Framework:** Java Spring Boot, Spring MVC, Spring Data JPA
* **Database Management:** MySQL with Hibernate ORM
* **Development Environment:** IntelliJ IDEA, Maven

---

## ⚙️ Getting Started (Local Setup)

To run this project locally on your machine, follow these steps:

### Prerequisites
* **Java Development Kit (JDK):** Version 17, 21, or 25 installed and configured to your system PATH.
* **MySQL Server:** Installed and running locally on port 3306.
* **Maven:** (Bundled with the project via mvnw).

### Installation

**1. Clone the repository:**
git clone https://github.com/your-username/oop-studio.git
cd oop-studio

**2. Configure the Database Credentials:**
Navigate to `src/main/resources/application-template.properties` and update the file with your local MySQL password:
spring.datasource.username=root
spring.datasource.password=YOUR_LOCAL_PASSWORD

**3. Run the Application:**
Open your terminal in the root directory and run the Spring Boot wrapper:
.\mvnw spring-boot:run

**4. Access the Platform:**
Open any modern web browser and navigate to: http://localhost:8080

*(Note: On initial startup, the DatabaseSeeder will automatically populate MySQL with the default OOP lab questions, coding tasks, and supervisor profiles.)*

---

## 📁 Project Structure Highlights

    Lms/
    ├── src/main/java/.../
    │   ├── controllers/      # Spring MVC routing & REST endpoints
    │   ├── models/           # Database entity classes (User, QuizResult)
    │   ├── repositories/     # Spring Data JPA interfaces
    │   └── services/         # Business logic & Code Execution engine
    ├── src/main/resources/
    │   ├── static/           # Custom CSS, Client-side JS, Images
    │   ├── templates/        # Thymeleaf HTML views (practice.html, etc.)
    │   └── application-template.properties # System & Database configurations

---

## 👨‍💻 Development Team

* **Ameer Muawiyah** - *Lead Full-Stack Architect & UI/UX Designer*
* **Hasnain Hamza** - *Backend Developer & Database Administrator*
* **Eman Atif** - *Frontend Engineer & Interactive Logic*
* **Aqsa Fazal** - *Adaptive Engine & Assessment Architect*
* **Mareena Khan** - *Code Runner Integrator & Quality Assurance*

> *"Building software the way the industry builds it."*