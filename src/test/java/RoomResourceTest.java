import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import app.dao.HotelDAO;
import app.dao.IDAO;
import app.populators.HotelPopulator;
import app.populators.RoomPopulator;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;


import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

//TODO: Default for jUnit is PER_METHOD.
// Meaning JUnit makes an instance of this class is created for each @Test that is run
public class RoomResourceTest {

    //TODO: Dependencies
    private static Javalin app; // only non-null if we start the server here
    private  static EntityManagerFactory emf;
    private static IDAO dao;

    @BeforeAll
    static void init()  {
        // 1) Lock test mode BEFORE starting server or creating EMF
        HibernateConfig.setTest(true);

        // 2) Start server (this should read the same test config)
        app = ApplicationConfig.startServer(7070);

        // 3) Point test EMF/DAO at the same test DB
        emf = HibernateConfig.getEntityManagerFactoryForTest();
        dao = HotelDAO.getInstance(emf);

        // 4) Rest Assured base URL/URI
        RestAssured.baseURI = "http://localhost:7070";
        RestAssured.basePath = "/api/v1";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails(); // auto-log failures
    }

    @BeforeEach
    void setUp(){
        try(EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            /* TODO: Deletes all rows from both tables.
                Resets their primary key sequences back to 1.
                 Cascades the operation to any dependent tables
             */
            em.createNativeQuery("TRUNCATE TABLE room, hotel " +
                    "RESTART IDENTITY CASCADE").executeUpdate();
            em.getTransaction().commit();
        }


        // TODO: Seed Hotels and Rooms
        List<Hotel> hotels = HotelPopulator.populateHotels(dao);
        List<Room> rooms = RoomPopulator.populateRooms(dao, hotels);




    }

    @AfterAll
    static void closeDown() {
        if (app != null) {
            ApplicationConfig.stopServer(app);
        }
    }




    @Test
    void testGetRoomsForHotel_valid() {
        // Hotel with DB id=2 should exist from seed and have 3 rooms
        given()
                .pathParams("id", 2)
        .when()
            .get("/hotel/{id}/rooms")
        .then()
            .statusCode(200)
            .body("size()", equalTo(3))
            .body("number", hasItems(201, 202)); // room numbers for floor 2
    }

    @Test
    void testGetRoomsForHotel_notFound() {
        given()
                .pathParams("id", 666999)
        .when()
            .get("/hotel/{id}/rooms")
        .then()
            .statusCode(404);
    }

    @Test
    void testAddRoomToHotel_valid() {
        // Add a new room to hotel id=1
        given()
            .contentType("application/json")
            .accept("application/json")
            .body("{\"number\": 21,\n  \"price\": 100}")
        .when()
            .post("/hotel/{id}/rooms", 1)
        .then()
            .statusCode(anyOf(is(200), is(201)));

        // Verify count increased and the new number exists
        given()
        .when()
            .get("/hotel/{id}/rooms", 1)
        .then()
            .statusCode(200)
            .body("size()", equalTo(4))
            .body("number", hasItem(21));
    }

    @Test
    void testAddRoomToHotel_emptyBody_returns400() {
        given()
            .contentType("application/json")
            .accept("application/json")
            .body("")
        .when()
            .post("/hotel/{id}/rooms", 1)
        .then()
            .statusCode(400);
    }

    @Test
    void testDeleteRoomById() {
        // Fetch a room id to delete from hotel 1
        Integer roomId =
            given()
            .when()
                .get("/hotel/{id}/rooms", 1)
            .then()
                .statusCode(200)
                .extract()
                .path("[0].id");

        // Delete that room
        given()
        .when()
            .delete("/room/{roomId}", roomId)
        .then()
            .statusCode(anyOf(is(200), is(204)));

        // Verify count decreased by 1 (initial 3 -> now 2)
        given()
        .when()
            .get("/hotel/{id}/rooms", 1)
        .then()
            .statusCode(200)
            .body("size()", equalTo(2))
            .body("id", not(hasItem(roomId)));
    }

}