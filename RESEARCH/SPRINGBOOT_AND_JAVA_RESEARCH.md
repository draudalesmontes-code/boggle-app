# Research Report

## Setting up JPA Entities and REST API communication (with CORS) for a Spring Boot + MySQL backend

### Summary of Work
I researched how to start defining database tables using Spring Data JPA (entities and repositories), and how to expose backend endpoints that a React frontend can call. I focused on the `@Table` annotation options for table-level constraints and naming, and on Spring’s starter examples for “data access with JPA” and “REST service + CORS” to understand the full flow: frontend request → backend controller/service → database persistence.

### Motivation
Our project needs a working vertical slice where the backend can store and retrieve game data (players, games, found words, etc.) from MySQL. I also needed clarity on what annotations and structure are required to map Java classes into SQL tables, and how to safely let the frontend communicate with the backend during development (cross-origin requests).

### Time Spent (Total: 3 hours)
- (≈ 1 hr 3 min): Reading `@Table` documentation and translating options into “what do I actually use in my entities.”
- (≈ 1 hr 12 min): Reviewing Spring Data JPA guide and mapping it to our project structure (entity → repository → service).
- (≈ 45 min): Reviewing REST + CORS guide to confirm how React calls Spring endpoints and what must be configured.

### Results
- Core requirements to use JPA in Spring Boot: define an `@Entity` class that maps to a table, create a repository interface for persistence operations, and configure the datasource + JPA behavior in `application.properties` so Spring can connect to MySQL and manage schema behavior. [2]
- How Java classes become SQL tables: the entity class is your mapping, while `@Table` lets you declare table-level details like the table name and constraints that apply across multiple columns. [1]
- Why the JavaDoc mattered: it helped me distinguish column-level rules (like `@Column(nullable=...)`) vs table-level rules (like `uniqueConstraints` across multiple columns), which is needed for composite uniqueness such as “a word must be unique per player per game.” [1]
- How the frontend and backend communicate: React sends HTTP requests (GET/POST/etc.) to Spring Boot endpoints; controllers receive the requests, call services/repositories, and the repositories persist or retrieve data from MySQL. [2]
- What CORS is doing for us in development: because React and Spring often run on different origins locally, the browser blocks requests unless the backend allows them; configuring CORS on the backend explicitly permits those cross-origin API calls. [3]

### Sources
[1] https://docs.oracle.com/javaee/7/api/javax/persistence/Table.html  
[2] https://spring.io/guides/gs/accessing-data-jpa  
[3] https://spring.io/guides/gs/rest-service-cors

### AI Statement
#### AI was used in collaboration to write file.
Prompt Used:

""" 
Help me make a research report with the template attached. 
Here are the links are searched with: 
1. https://docs.oracle.com/javaee/7/api/javax/persistence/Table.html 
2. https://spring.io/guides/gs/accessing-data-jpa 
3. https://spring.io/guides/gs/rest-service-cors 
- Things that I learned the overall requirements to set up database JPA and start code to define Java tables. Java doc guided me in what commands I can use and why 
- REST API Services Taught me how exactly Backend will communicate with frontend and then that the backend sends it to database Constraints: - must use markdown - Any long explanation must not supersede 8 sentences. 
"""

