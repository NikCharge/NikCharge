# NikCharge

NikCharge is a web platform designed to streamline and optimize the electric vehicle (EV) charging experience for drivers, station employees, and station managers. It unifies station discovery, real-time availability, reservations, payment processing, and operational management into a single, user-friendly solution.

## Evaluation
- **Final Grade:** **16 / 20**

## Features
- Station Search & Real-Time Availability: Find and filter charging stations by location, charger type, and price, with up-to-date availability.
- Reservation System: Secure charging spots in advance with clear time slots and instant confirmation.
- Charging Session Management: Start, stop, and track charging sessions via unique codes, with transparent billing and session summaries.
- Payment Integration: Supports simulated payments using Stripe.
- User Dashboard: Access charging history, energy consumption, and spending insights.
- Station Operations Dashboard: Employees can update charger status and flag for maintenance.
- RESTful API: Well-documented, modular API with Swagger support.

## Tech Stack
- Frontend: React (Vite)
- Backend: Spring Boot (Java)
- Database: PostgreSQL
- Payments: Stripe API
- Containerization: Docker & Docker Compose
- API Documentation: Swagger

## Getting Started

### Prerequisites
- Docker & Docker Compose installed
- Node.js and npm (for local frontend development)

### Quick Start
1. Clone the repository
```
git clone https://github.com/your-org/nikcharge.git
cd nikcharge
```

2. Set environment variables
Copy the provided .env template and configure as needed.

3. Start the platform
``` 
docker-compose up --build
```

4. Access the services:
- Frontend: http://localhost:80
- Backend API & Swagger: http://localhost:8080/swagger-ui/index.html
- Database: PostgreSQL running on port 5432

## Documentation
- API Documentation: Swagger UI available at /swagger-ui/index.html when the backend is running.
- User Documentation: See /documentation for requirements and design documentation.
- Jira SCRUM Boards: https://nikcharge.atlassian.net/jira/software/projects/SCRUM/boards/1

## Post-Presentation Changes
After the initial project presentation, the latest pushes primarily addressed bug fixes in the payment system to ensure a smoother and more reliable checkout experience. There were multiple merges from the fix-payment-bug branch into the main develop branch to consolidate these improvements. Additional enhancements were made to the dashboard interface and functionality, as well as production fixes, including adjustments to the Nginx configuration for better deployment stability. Updates were also applied to the employee module, further refining operational workflows and system reliability.
