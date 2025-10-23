
### What the Context (ctx) object really is ###
In Javalin, every route handler gets a Context object.
You can think of it as your “HTTP toolbox” — it contains:

- The request (headers, body, query params, etc.)
- The response (status, headers, and body you’ll send back)

So ctx = the live HTTP exchange between your server and the client.

Example powers it gives you:

```
ctx.body();           // read request body as String
ctx.bodyAsClass(MyDTO.class);  // parse JSON into a DTO
ctx.status(201);      // set HTTP status code
ctx.json(someObject); // send JSON back to client
ctx.pathParam("id");  // get /api/item/{id}
ctx.attribute("x", obj) // Store an object for this request
ctx.attribute("x") // Retrieve it later
```


### What is the controller layer, what does it do, and why is it important? ###

Think of the controller as the **traffic officer between the outside world and your services**. Routes hand the request to the controller, the controller speaks HTTP, and the service layer does the heavy lifting.

| Step | What the controller does | Quick example |
| --- | --- | --- |
| **Accept** | Reads the incoming HTTP method, path, headers, and body. | User calls `POST /sources` with JSON. |
| **Validate & delegate** | Parses the payload into a DTO, checks basic rules, and forwards it to the service. | Convert JSON to `SourceCreateDTO`, call `sourceService.create(...)`. |
| **Handle errors** | Catches exceptions and turns them into meaningful HTTP status codes and messages. | Return `400 Bad Request` when validation fails, or `403 Forbidden` when access is denied. |
| **Send output** | Serializes the service result back to JSON (or another format) and sets the response code. | Send back the new `SourceDTO` with `201 Created`. |

Why it matters:
- **Separation of concerns**: controllers focus on HTTP, services focus on business logic.
- **Easier testing**: you can unit-test services without mocking HTTP, and test controllers with lightweight mocks.
- **Future flexibility**: if you expose the app via CLI, gRPC, or messaging tomorrow, only the controller layer changes—the services stay the same.

### Q: What's a transaction ###
A transaction is a unit of work that groups one or more database operations into an all-or-nothing block.
In short, it's what happens inbetween em.getTransaction().begin() and em.getTransaction().commit()

### Common HTTP status codes in Javalin ###

Javalin auto-responds with **`200 OK`** when you return JSON, so you only override the status when you need to tell the client something special.

| Action | Typical `ctx.status(...)` | Why you use it |
| --- | --- | --- |
| **Create (POST)** | `ctx.status(201)` (*Created*) | Lets the client know a brand-new resource was made. Often paired with the created object or a `Location` header. |
| **Read (GET)** | *(default)* `200 OK` | No need to set it manually. Returning JSON already tells Javalin to use 200. |
| **Update (PUT/PATCH)** | `ctx.status(200)` or `ctx.status(204)` | Use **200** when you return the updated resource; use **204 No Content** when the body is empty but the update succeeded. |
| **Delete (DELETE)** | `ctx.status(204)` | Resource is gone and there’s nothing to return. |
| **Bad Request** | `ctx.status(400)` | The request was malformed or missing fields. Explain the problem in the JSON response. |
| **Unauthorized** | `ctx.status(401)` | Authentication failed (missing/invalid token). Attach a message like “Please log in.” |
| **Forbidden** | `ctx.status(403)` | User is authenticated but lacks the required role/ownership. |
| **Not Found** | `ctx.status(404)` | Resource doesn’t exist (wrong ID, deleted item, etc.). |
| **Conflict** | `ctx.status(409)` | State conflict, like trying to create a duplicate source name for the same owner. |
| **Server Error** | `ctx.status(500)` | Unexpected failure on the server. Log it and send a generic error message back. |

### SourceService & Scrape logging – big picture ###

| Method | Action | Table(s) touched | Result |
| --- | --- | --- | --- |
| `startRunning` | Create a new crawl log | `crawl_logger` | Inserts a row with status **RUNNING** for the requested source/user. |
| `updateStatus` | Internal helper to tweak status/error | `crawl_logger` | Adjusts status or error fields when the crawler reports back. |
| `successLog` | Mark crawl as finished successfully | `crawl_logger` | Updates the row to **SUCCESS** and clears any error text. |
| `failLog` | Mark crawl as failed | `crawl_logger` | Sets status to **FAILED** and stores the error message. |
| `attachItems` | Persist scraped items | `scraped_data` (linked to `crawl_logger`) | Saves each scraped item JSON and ties it to the crawl log for history. |

### Cheat sheet to how to entity relationships in JPA annotaions ###

| Relationship | How to wire it | What to remember |
| --- | --- | --- |
| **One-to-One** | Put `@OneToOne` + `@JoinColumn` on the side that owns the row (the table with the foreign key). The other side just points back with `@OneToOne(mappedBy = "…")`. | When you set one side in code, set the other too (`partner.setUser(this); this.partner = partner;`) so both stay linked. |
| **Many-to-One / One-to-Many** | The “many” side owns the foreign key: `@ManyToOne` + `@JoinColumn`. The “one” side exposes a collection with `@OneToMany(mappedBy = "…")`. | Offer helpers like `addItem`/`removeItem` that update both sides. Use `orphanRemoval = true` when removing from the parent should delete the child row. |
| **Many-to-Many** | Use `@ManyToMany` with a `@JoinTable` to name the link table and columns. The other side uses `@ManyToMany(mappedBy = "…")`. | Helpers must add/remove from both sets so the join table stays consistent. |

Quick reminders: keep `@JoinColumn` only on the owning side, use lowercase names in queries to avoid case issues, and null-check everything before linking entities.

```java
// Quick mini-example for a one-to-many relationship
@Entity class Author {
    @Id Long id;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Book> books = new HashSet<>();

    public void addBook(Book book) {
        if (book == null) return;
        books.add(book);
        book.setAuthor(this);
    }

    public void removeBook(Book book) {
        if (book == null) return;
        books.remove(book);
        book.setAuthor(null);
    }
}

@Entity class Book {
    @Id Long id;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;

    public void setAuthor(Author author) {
        this.author = author;
    }
}
```

### Q: What annotations are lombok, and what are JPA annotation? ###

| Annotation | Library | Purpose in this project |
| --- | --- | --- |
| `@Getter` | Lombok | Generates getter methods so entities expose fields without manual code |
| `@Setter` | Lombok | Generates setter methods for mutable entity fields |
| `@NoArgsConstructor` | Lombok | Supplies the zero-argument constructor that JPA requires |
| `@AllArgsConstructor` | Lombok | Generates a constructor accepting all declared fields |
| `@Builder` | Lombok | Provides the fluent builder API used when creating entities |
| `@Builder.Default` | Lombok | Keeps default values intact when using Lombok’s builder |
| `@ToString` | Lombok | Auto-generates `toString()` for easier logging/debugging |
| `@ToString.Exclude` | Lombok | Excludes relationships from `toString()` to avoid recursion |
| `@EqualsAndHashCode` | Lombok | Generates `equals`/`hashCode`; `onlyExplicitlyIncluded` lets us opt fields in |
| `@EqualsAndHashCode.Include` | Lombok | Marks the fields that should define equality |
| `@Entity` | JPA (`jakarta.persistence`) | Marks the class as a persistent JPA entity |
| `@Table` | JPA | Overrides the table name for a mapped entity |
| `@Id` | JPA | Identifies the primary key field |
| `@GeneratedValue` | JPA | Configures how the primary key is generated (identity/sequence) |
| `@Column` | JPA | Describes column mapping (name, nullability, uniqueness, definition) |
| `@ManyToOne` | JPA | Defines the owning side of a many-to-one relationship |
| `@OneToMany` | JPA | Defines the collection side of a one-to-many relationship |
| `@ManyToMany` | JPA | Maps the many-to-many relationship between users and roles |
| `@JoinColumn` | JPA | Specifies the foreign key column for relationship mappings |
| `@JoinTable` | JPA | Configures the join table used in many-to-many mappings |
| `@Enumerated` | JPA | Stores enum values (like `CrawlStatus`) as strings |
| `@PrePersist` | JPA | Lifecycle callback invoked just before the entity is inserted |
| `@PreUpdate` | JPA | Lifecycle callback triggered before an update to refresh timestamps |




#### Q: What does serializing mean? ###
Serialization refers to the process of converting a Java object into a JSON format. 
This allows the object to be easily stored, transmitted, or shared between systems. 
E.g., using Jackson, you can serialize a Java object into a JSON string with minimal configuration, 
making it suitable for data exchange in RESTful APIs or other applications.


### Q: Is Deserializing then the opposite of serializing? ###
Indeed. the reverse process of serialization is called deserialization. 
Deserialization involves converting a JSON string back into a Java object.
For example, using Jackson, you can take a JSON string and map it to a Java class, 
allowing you to work with the data in your application.


### Q: What's the difference between createQuery and createNativeQuery in the DAOs? ###
- createQuery → JPQL → portable, entity-aware, database-independent. 
- createNativeQuery → SQL → powerful, but DB-specific and not automatically aware of entity mappings unless you specify the result type.



### Q: What is the difference between response and request message? ###

        **A request/response includes all three elements; 
        the request/response line, the headers, and the body(data, usually in JSON)**

        Request is when the user/client talks to the server. (Client → Server)
        Reponse is when the server send back/talks to the user/client. (Server → Client)


### Q: What does a request message look like? ###
GET /movies/popular HTTP/1.1      ← this is the **Request Line**
Host: api.themoviedb.org          ← header Base URL/Host
Authorization: Bearer 123abc456   ← header (123abc456) is the token 
Accept: application/json          ← header Client only accepts JSON 
User-Agent: MyMovieApp/1.0        ← header Who/What software/browser made the request
                                  ← blank line ends the headers
(body would go here, if any)


### Q: What does a response message look like? ###
HTTP/1.1 200 OK                         ← Status line
Date: Tue, 21 Oct 2025 18:10:00 GMT     ← header
Server: api.themoviedb.org              ← header Base URL/Host 
Content-Type: application/json          ← header (tells client what kind of data follows)
Content-Length: 112                     ← header (how many bytes in the body)
Cache-Control: public, max-age=3600     ← header (caching policy)
                                        ← blank line ends the headers
{                                       ← body (the data content)
"page": 1,
"results": [
{ "id": 550, "title": "Fight Club" },
{ "id": 680, "title": "Pulp Fiction" }
],
"total_results": 2
}

### Q: What's the difference between baseUrl from source, and url from ScrapedData? ###
💡 Simple analogy
![baseUrl_vs_url.png](src/main/resources/images/baseUrl_vs_url.png)


### Q: What is a JWT(JSON Web Token), what does it contain, and how does it differ from session tokens? ###
A JSON Web Token are like session tokens, in that they exist as a way to authenticate and recognize users. 
Because of the nature of the HTTP, the web is stateless, meaning each request must contain all necessary information for each request.
Session tokens do this by storing information about the user/client and giving them a SessionID they have to bring with every request.
This requires a centralized server to store and remember this information. 
JSON Web Tokens are self-contained; meaning instead of a centralized server, 
a JWT from the user/client contains all necessary authentication information.
When a user logs in, the server generates a JWT containing claims like the user’s ID, roles, or permissions, 
then signs it using a secret or private key.
On later requests, the client sends this token (often in the Authorization header as Bearer <token>), 
and the server simply verifies the signature — no database lookup needed to check a session.

**A JWT has three parts, separated by dots (.):**
header.payload.signature

**Anyone who intercepts one can read the header and payload (they’re just base64 encoded, not encrypted).
That’s why you must send them only over HTTPS, which encrypts the whole connection.**


**The server signs the token like this:**
signature = HMACSHA256(header + "." + payload, secretKey)
When the token comes back, the server recalculates that signature with the same secret key.
- If the signature matches, it knows the token wasn’t altered and was issued by itself.
- If it doesn’t match, it rejects it.
OBS: The servers SECRET_KEY never leaves the server!

The user must then send something like this at every (authorized) request:
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...

TL;DR When the DB verifies login, you get a long string (JWT) which is basically like a session, 
so you don't have to login with every request. 
You include the JWT like this: 
Authorization: Bearer <token>



Reference video used:
<---------------------------- https://www.youtube.com/watch?v=soGRyl9ztjI---------------------------->









### For learning purposes, shows how the projects  dependencies interact with each other ### 

| Layer | Accept | Validate | Handle errors | Send output |
| --- | --- | --- | --- | --- |
| **Presentation** (controllers/routes) | **Receives** HTTP request (method, path, headers, body) via Javalin | Basic request checks: body format, required params | Maps issues to HTTP status codes (`400`, `401`, `403`, `500`) and JSON error messages | Serializes DTOs back to JSON/HTTP responses |
| **Application** (services/business logic) | Takes DTOs from controllers | Runs domain rules: ownership, unique names, valid URLs/selectors | Throws custom exceptions (e.g., `EntityNotFoundException`, `PersistenceException`) so controllers can respond cleanly | Returns domain DTOs/entities to controllers |
| **Persistence** (Hibernate/JPA) | Accepts entity changes from services via DAOs | Validates entity annotations (nullability, constraints) before committing | Rolls back transactions and wraps SQL issues in runtime exceptions | Provides managed entities back to services |
| **JDBC driver** | Executes SQL generated by Hibernate | Validates SQL syntax at the driver level | Propagates database errors (constraint violations, connection issues) | Streams result sets to Hibernate |
| **Database** (PostgreSQL) | Accepts SQL commands | Enforces schema constraints, indexes, FK rules | Returns SQL error codes/messages to JDBC | Persists rows and answers queries |
