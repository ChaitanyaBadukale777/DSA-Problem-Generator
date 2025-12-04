# DSA-Problem-Generator

# Explanation

-- I built a DSA GrindHub Portal, which is a smart web application designed to help students and developers practice Data Structures and Algorithms using AI-generated problems.

The backend is developed using Spring Boot (Java), and the frontend is built with HTML, Tailwind CSS, and JavaScript. This combination ensures that the application is fast, secure, and highly responsive.

When a user clicks the button to generate a question, the frontend sends a request to the Spring Boot server. The server works as a secure middle layer between the client and the AI.

For security, the Gemini API key is stored in an environment variable, so it is never exposed to the frontend or the public source code.

The server then sends a structured prompt to the Gemini AI model and strictly enforces a pure JSON response format. This guarantees that every generated question always contains all required fields such as the problem statement, constraints, example input, example output, and topic.

After validating and cleaning the response, the Spring Boot server sends well-structured data back to the frontend, where it is displayed dynamically in a clean and user-friendly card layout.

This portal is highly useful in today’s learning environment because it gives users an unlimited supply of fresh practice problems instead of relying on static textbooks.

Overall, this project demonstrates my ability to build secure production-ready backend systems, integrate AI services, and design dynamic, interactive user interfaces.

--