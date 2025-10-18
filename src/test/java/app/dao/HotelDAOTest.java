package app.dao;

import app.config.HibernateConfig;
import app.exceptions.ApiException;
import app.populators.HotelPopulator;
import app.populators.RoomPopulator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

//TestInstance only makes one instance of this test class and reuse it for all tests
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HotelDAOTest {

    private static EntityManagerFactory emf;
    private IDAO dao;
    private List<Hotel> hotels;
    private List<Room> rooms;

    @BeforeAll
    void initOnce() {
        emf = HibernateConfig.getEntityManagerFactoryForTest();
        dao = HotelDAO.getInstance(emf);
    }

    @BeforeEach
    void setUp() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            //TODO: Truncates deletes all data in a table
            em.createNativeQuery("TRUNCATE TABLE room, hotel RESTART IDENTITY CASCADE").executeUpdate();
            em.getTransaction().commit();
        }
        catch (ApiException e) {
            throw new RuntimeException("Failed to truncate tables", e);
        }

        hotels = HotelPopulator.populateHotels(dao);
        rooms = RoomPopulator.populateRooms(dao, hotels);


    }

    @AfterAll
    void tearDown() {
        if(emf != null && emf.isOpen()) {
            emf.close();
        }
    }


    @Test
    void getAllHotels() {
        List<Hotel> all = dao.getAllHotels();

        //Asserts
        assertThat(all, is(notNullValue()));
        assertThat(all, hasSize(5));
        assertThat(all, everyItem(hasProperty("address", notNullValue())));
    }

    @Test
    void getHotelById() {
        //TODO: Arrange
        Hotel hotel = Hotel.builder()
                .hotelId(999)
                .name("Grand Budapest Hotel")
                .address("Somewhere in Budapest")
                .rooms(10)
                .build();


        //TODO: Act
        Hotel createdHotel = dao.createHotel(hotel);
        Hotel theSameHotel = dao.getHotelById(createdHotel.getId());


        //TODO: Assert
        assertNotNull(hotel);
        assertNotNull(createdHotel);
        assertNotNull(theSameHotel);

        assertEquals(createdHotel.getAddress(), theSameHotel.getAddress());
        assertEquals(createdHotel.getId(), theSameHotel.getId());
    }


    @Test
    void createHotel() {
        //TODO: Arrange

        Hotel hotel = Hotel.builder()
                .hotelId(999)
                .name("Grand Budapest Hotel")
                .address("Somewhere in Budapest")
                .rooms(10)
                .build();


        //TODO: Act
        Hotel createdHotel = dao.createHotel(hotel);


        //TODO: Assert
        assertNotNull(hotel);
        assertEquals(hotel.getName(), createdHotel.getName());
        assertNotNull(createdHotel.getRooms());
    }

    @Test
    void createHotels() {
        //TODO: Arrange

        Hotel hotel1 = Hotel.builder()
                .hotelId(999)
                .name("Grand Budapest Hotel")
                .address("Somewhere in Budapest")
                .rooms(10)
                .build();

        Hotel hotel2 = Hotel.builder()
                .hotelId(989)
                .name("The Underworld")
                .address("Somewhere in Hell")
                .rooms(20)
                .build();

        Hotel hotel3 = Hotel.builder()
                .hotelId(799)
                .name("Atlantis The Plam")
                .address("Somewhere in Dubai")
                .rooms(30)
                .build();


        //TODO: Act
        List<Hotel> createdHotels = new ArrayList<>();
        createdHotels.add(hotel1);
        createdHotels.add(hotel2);
        createdHotels.add(hotel3);

        List<Hotel> persistedHotels = dao.createHotels(createdHotels);


        //TODO: Assert
        assertNotNull(persistedHotels);
        assertEquals(persistedHotels.size(), createdHotels.size());
        assertEquals(persistedHotels.get(0).getName(), createdHotels.get(0).getName());
    }


    @Test
    void updateHotel() {
        //TODO Arrange: persist a hotel we can update

        Hotel hotel = Hotel.builder()
                .hotelId(999)
                .name("Grand Budapest Hotel")
                .address("Address 1")
                .rooms(20)
                .build();

        Hotel persisted = dao.createHotel(hotel);
        assertNotNull(persisted, "Expected persisted hotel not null");
        assertNotNull(persisted.getId(), "Expected generated PK id");

        //TODO Act: change fields and update
        persisted.setName("Grand Copenhagen Hotel");
        persisted.setRooms(22);
        persisted.setAddress("Address 2");

        Hotel updated = dao.updateHotel(persisted);
        assertNotNull(updated, "Expected updated hotel not null");

        //TODO Assert: verify via fresh read from DB
        Hotel reloaded = dao.getHotelById(updated.getId());
        assertNotNull(reloaded, "Expected reloaded hotel not null");
        assertEquals("Grand Copenhagen Hotel", reloaded.getName());
        assertEquals(22, reloaded.getRooms());
        assertEquals("Address 2", reloaded.getAddress());
    }

    @Test
    void deleteHotel() {
        //TODO Arrange:
        Hotel hotel = Hotel.builder()
                .hotelId(666)
                .name("The Imaginary Hotel")
                .address("In your dreams")
                .rooms(50)
                .build();

        Hotel persisted = dao.createHotel(hotel);
        assertNotNull(persisted, "Idk if the Hotel is persisted, but at least it isn't null");


        //TODO Act:
        boolean hasHotelBeenDeleted = dao.deleteHotel(persisted);


        //TODO Assert:
        assertTrue(hasHotelBeenDeleted, "If true, DAO delete method has been executed correctly");
        try {
            Hotel after = dao.getHotelById(persisted.getId());
            assertNull(after, "Expected hotel to be null after deletion");
        } catch (ApiException e) {
            e.getMessage();
        }
    }

    @Test
    void addRoom() {
        //TODO: Arrange
        Hotel hotel = Hotel.builder()
                .hotelId(999)
                .name("Grand Budapest Hotel")
                .address("Somewhere in Budapest")
                .rooms(10)
                .build();

        Room room = Room.builder()
                .roomId(999)
                .price(250)
                .roomNumber(13)
                .build();



        //TODO: Act
        Hotel createdHotel = dao.createHotel(hotel);
        boolean createdRoom = dao.addRoom(createdHotel, room);
        Room persistedRoom = dao.getRoomById(room.getId());


        //TODO: Assert
        assertNotNull(hotel);
        assertNotNull(createdHotel);
        assertNotNull(room);

        assertTrue(createdRoom);
        assertEquals(room.getRoomNumber(), persistedRoom.getRoomNumber());
    }

    @Test
    void removeRoom() {
        //TODO: Arrange
        Hotel hotel = Hotel.builder()
                .hotelId(999)
                .name("Grand Budapest Hotel")
                .address("Somewhere in Budapest")
                .rooms(10)
                .build();

        Room room = Room.builder()
                .roomId(999)
                .price(250)
                .roomNumber(13)
                .build();



        //TODO: Act
        Hotel createdHotel = dao.createHotel(hotel);
        boolean createdRoom = dao.addRoom(createdHotel, room);
        boolean deletedRoom = dao.removeRoom(createdHotel, room);


        //TODO: Assert
        assertNotNull(hotel);
        assertNotNull(createdHotel);
        assertNotNull(room);

        assertTrue(createdRoom);
        assertTrue(deletedRoom);
        try {
            Hotel after = dao.getHotelById(room.getId());
            assertNull(after, "Expected hotel to be null after deletion");
        } catch (ApiException e) {
            e.getMessage();
        }
    }

    @Test
    void getRoomById() {
        //TODO: Arrange
        Hotel hotel = Hotel.builder()
                .hotelId(999)
                .name("Grand Budapest Hotel")
                .address("Somewhere in Budapest")
                .rooms(10)
                .build();

        Room room = Room.builder()
                .roomId(999)
                .price(250)
                .roomNumber(13)
                .build();



        //TODO: Act
        Hotel createdHotel = dao.createHotel(hotel);
        boolean createdRoom = dao.addRoom(createdHotel, room);
        Room persistedRoom = dao.getRoomById(room.getId());


        //TODO: Assert
        assertNotNull(hotel);
        assertNotNull(createdHotel);
        assertNotNull(room);

        assertTrue(createdRoom);
        assertEquals(room.getId(), persistedRoom.getId());
    }

    @Test
    void getRoomsForHotel() {
        //TODO: Arrange
        Hotel hotel = Hotel.builder()
                .hotelId(999)
                .name("Grand Budapest Hotel")
                .address("Somewhere in Budapest")
                .rooms(10)
                .build();

        Room room = Room.builder()
                .roomId(999)
                .price(250)
                .roomNumber(13)
                .build();



        //TODO: Act
        Hotel createdHotel = dao.createHotel(hotel);
        boolean createdRoom = dao.addRoom(createdHotel, room);
        List<Room> rooms = dao.getRoomsForHotel(createdHotel);


        //TODO: Assert
        assertNotNull(createdHotel);
        assertNotNull(room);
        assertTrue(createdRoom);

        assertEquals(rooms.size(), 1);

    }

    @Test
    void hotelRoomRelationship_persistsAndLinks() {
        //TODO Arrange
        Hotel hotel = Hotel.builder()
                .hotelId(1000)
                .name("Test Hotel")
                .address("123 Test St")
                .rooms(0)
                .build();

        Room room1 = Room.builder()
                .roomId(1001)
                .price(150)
                .roomNumber(101)
                .build();

        Room room2 = Room.builder()
                .roomId(1002)
                .price(200)
                .roomNumber(102)
                .build();

        // Use relationship helpers to associate rooms with hotel
        hotel.addRoom(room1);
        hotel.addRoom(room2);

        //TODO Act
        Hotel persistedHotel = dao.createHotel(hotel);
        Hotel reloadedHotel = dao.getHotelById(persistedHotel.getId());

        //TODO Assert
        assertNotNull(persistedHotel);
        assertNotNull(reloadedHotel);
        assertNotNull(reloadedHotel.getAllRooms());
        assertEquals(2, reloadedHotel.getAllRooms().size());
        assertTrue(reloadedHotel.getAllRooms().contains(room1));
        assertTrue(reloadedHotel.getAllRooms().contains(room2));

        for (Room r : reloadedHotel.getAllRooms()) {
            assertNotNull(r.getHotel());
            assertEquals(reloadedHotel.getId(), r.getHotel().getId());
        }
    }

}