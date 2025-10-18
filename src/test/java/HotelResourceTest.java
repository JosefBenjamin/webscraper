import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import app.dao.HotelDAO;
import app.dao.IDAO;
import app.dto.HotelDTO;
import app.dto.RoomDTO;
import app.populators.HotelPopulator;
import app.populators.RoomPopulator;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;


import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

//TODO: Default for jUnit is PER_METHOD.
// Meaning JUnit makes an instance of this class is created for each @Test that is run
public class HotelResourceTest {

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
    public void testGetRequest() {
        given()
                .when()
                .get("/hotel")
                .then()
                .statusCode(200);
    }

    @Test
    void testGetHotelById() {
                given()
                        .pathParams("id", 1)
                .when()
                        .get("/hotel/{id}")
                .then()
                        .statusCode(200)
                        .body("id", equalTo(1))
                        .body("name", equalTo("Hilton"))
                        .body("rooms.size()", equalTo(3));
    }

    @Test
    void testGetHotels() {
        HotelDTO[] hotelsDTOs =
                given()
                .when()
                        .get("/hotel")
                .then()
                        .log().all()
                        .statusCode(200)
                        .extract()
                        .as(HotelDTO[].class);

        assertThat("Expected at least one hotel", hotelsDTOs.length, greaterThanOrEqualTo(3));
    }


    // TODO: ### Get rooms for a hotel
    //  GET {{url}}/hotel/1/rooms
    //  Accept: application/json
    @Test
    public void testGetRoomsForAHotel() {
        RoomDTO[] roomDTOS =
                given()
                .when()
                    .get("/hotel/{id}/rooms", 2)
                .then()
                        // .log().all()
                        .statusCode(200)
                        .extract()
                        .as(RoomDTO[].class);

        assertThat("Expected at least three rooms", roomDTOS.length, greaterThanOrEqualTo(3));
    }

    //TODO: ### Create a hotel (valid) — body matches HotelDTO fields
    // POST {{url}}/hotel
    @Test
    public void testCreateHotel_valid(){
            given()
                .contentType("application/json")
                .accept("application/json")
                .body("{\"name\": \"Newly Created Hotel\",  \"address\": \"The New World\" }")
            .when()
                    .post("/hotel")
            .then()
                    .statusCode(anyOf(is(200), is(201), is(202)));

            given()
            .when()
                    .get("/hotel")
            .then()
                    .statusCode(200)
                    .body("size()", equalTo(6));

    }


    //TODO: ### Create a hotel (empty body -> expect 400)
    // POST {{url}}/hotel
    // Content-Type: application/json
    // Accept: application/json
    @Test
    public void createHotelEmpty_return400(){
        given()
                .contentType("application/json")
                .accept("application/json")
                .body("")
        .when()
                .post("/hotel")
        .then()
                .statusCode(400);

    }


    //TODO: ### Update a hotel (valid) — only mutable fields (name, address)
    // PUT {{url}}/hotel/1
    // Content-Type: application/json
    // Accept: application/json
    @Test
    public void updateHotel_valid(){
        given()
                .contentType("application/json")
                .accept("application/json")
                .body("{\"name\": \"The Updated Hotel\", \"address\": \"Milky Way, door to the left\" }")
        .when()
                .put("hotel/{id}", 1)
        .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("name", equalTo("The Updated Hotel"))
                .body("address", equalToIgnoringCase("milky way, door to the left"))
                .body("rooms.size()", equalTo(3));

    }


    //TODO: ### Update a hotel (empty body -> expect 400)
    // PUT {{url}}/hotel/1
    // Content-Type: application/json
    // Accept: application/json
    @Test
    public void updateHotelEmptyBody_return400(){
        given()
                .contentType("application/json")
                .accept("application/json")
                .body("")
        .when()
                .put("/hotel/{id}", 1)
        .then()
                .statusCode(400);
    }


    //TODO: ### Delete a hotel
    // DELETE {{url}}/hotel/1
    // Accept: application/json
    @Test
    public void testDeleteHotel_valid(){
        // Fetch a hotel id to delete from hotel 1
        Integer hotelId =
                given()
                        .when()
                        .get("/hotel/{id}", 1)
                        .then()
                        .statusCode(200)
                        .extract().path("id");

        // Delete that hotel
        given()
                .when()
                .delete("/hotel/{hotelId}", hotelId)
                .then()
                .statusCode(anyOf(is(200), is(204)));

        // Verify count decreased by 1 (initial 3 -> now 2)
        given()
                .when()
                .get("/hotel")
                .then()
                .statusCode(200)
                .body("size()", equalTo(4))
                .body("id", not(hasItem(hotelId)));
    }





}