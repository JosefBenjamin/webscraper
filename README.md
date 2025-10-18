























### For learning purposes, shows how the projects dependencies interact with each other ### 

┌────────────────────────────────────────────────────────────┐
│                   Presentation Layer                       │
│────────────────────────────────────────────────────────────│
│   REST API (Javalin)                                       │
│   - Receives user input / HTTP requests                    │
│   - Converts to Java objects (DTOs, JSON, etc.)            │
│   - Calls the Application Layer                            │
└────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────────┐
│                    Application Layer                       │
│────────────────────────────────────────────────────────────│
│   Your Java Code                                           │
│   (Services, Controllers, Business Logic)                  │
│                                                            │
│   Example:                                                 │
│   hotelService.createHotel(hotelDto);                      │
│        │                                                   │
│        ▼                                                   │
│   hotelDAO.save(hotelEntity);                              │
│        │                                                   │
│        ▼                                                   │
└────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────────┐
│                  Persistence Layer                         │
│────────────────────────────────────────────────────────────│
│   Hibernate ORM (Object-Relational Mapping)                │
│   - Maps Java Entities ↔ Database Tables                   │
│   - Translates Entity objects to SQL commands              │
│   - Uses configuration (like HibernateConfig.java)         │
│   - Manages caching, sessions, transactions                │
│                                                            │
│   Example:                                                 │
│   session.save(hotelEntity)                                │
│     ↓                                                      │
│   Hibernate builds SQL:                                    │
│     INSERT INTO hotels (name, city, ...) VALUES (?, ?)     │
│     ↓                                                      │
│   Uses JDBC to send that SQL to the database               │
└────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────────┐
│                        JDBC                                │
│────────────────────────────────────────────────────────────│
│   - Low-level driver-based database access                 │
│   - Opens connections, executes SQL, fetches results       │
│   - Talks directly to the database server                  │
└────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────────┐
│                        DATABASE                            │
│────────────────────────────────────────────────────────────│
│   PostgreSQL                                               │
│   - Stores the actual data                                 │
│   - Responds to SQL queries from JDBC                      │
└────────────────────────────────────────────────────────────┘