package app.dao;

import app.exceptions.ApiException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class HotelDAO implements IDAO {

    private static EntityManagerFactory emf;
    private static HotelDAO instance;

    private HotelDAO() {

    }

    //TODO: Singleton
    public static HotelDAO getInstance(EntityManagerFactory emf) {
        if(instance == null) {
            instance = new HotelDAO();
            HotelDAO.emf = emf;
        }
            return instance;
    }

    @Override
    public List<Hotel> getAllHotels() {
        try(EntityManager em = emf.createEntityManager()) {
            List<Hotel> hotels = em.createQuery("SELECT DISTINCT h FROM Hotel h " +
                    "LEFT JOIN FETCH h.allRooms", Hotel.class)
                    .getResultList();
            return hotels;
        }
    }

    @Override
    public Hotel getHotelById(int id) {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<Hotel> query = em.createQuery("SELECT DISTINCT h FROM Hotel h " +
                            "LEFT JOIN FETCH h.allRooms " +
                            "WHERE h.id = :id", Hotel.class)
                    .setParameter("id", id);
            List<Hotel> result = query.getResultList();
            return result.isEmpty() ? null : result.get(0);

        }
    }

        @Override
        public Hotel createHotel (Hotel hotel){
            try (EntityManager em = emf.createEntityManager()) {
                em.getTransaction().begin();
                try{
                    em.persist(hotel);
                    em.getTransaction().commit();
                    return hotel;
                } catch(RuntimeException e) {
                    throw new ApiException(400, "error");
                }
            }
        }

    @Override
    public List<Hotel> createHotels(List<Hotel> hotels) {
        if (hotels == null || hotels.isEmpty()) {
            throw new ApiException(400, "No hotels to create");
        }
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            try {
                for (Hotel h : hotels) {
                    em.persist(h);
                }
                em.getTransaction().commit();
                return hotels; // IDs populated after commit
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                throw new ApiException(500, "Failed to create hotels: " + e.getMessage());
            }
        }
    }

        @Override
        public Hotel updateHotel (Hotel hotel){
            try (EntityManager em = emf.createEntityManager()) {
                em.getTransaction().begin();
                try{
                    Hotel updated = em.merge(hotel);
                    em.getTransaction().commit();
                    return updated;
                } catch(RuntimeException e) {
                    throw new ApiException(400, "error");
                }
            }
        }

        @Override
        public boolean deleteHotel (Hotel hotel){
            try (EntityManager em = emf.createEntityManager()) {
                em.getTransaction().begin();
                try {
                    Hotel hotelForDeletion = em.find(Hotel.class, hotel.getId());
                    if (hotelForDeletion != null) {
                        em.remove(hotelForDeletion);
                    }
                    em.getTransaction().commit();
                    return hotelForDeletion != null;
                } catch(RuntimeException e) {
                    throw new ApiException(400, "error");
                }
            }
        }

    @Override
    public boolean addRoom(Hotel hotel, Room room) {
        if (hotel.getId() == null) throw new ApiException(400, "Hotel id is required");

        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            Hotel managed = em.getReference(Hotel.class, hotel.getId());
            managed.addRoom(room);
            em.persist(room);
            managed.setRooms(managed.getAllRooms().size());

            em.getTransaction().commit();
            return true;
        } catch (RuntimeException e) {
            throw new ApiException(500, "Could not add room: " + e.getMessage());
        }
    }

    @Override
    public boolean removeRoom(Hotel hotel, Room room) {
        if (hotel == null || hotel.getId() == null) {
            throw new ApiException(400, "Hotel id is required");
        }
        if (room == null || room.getId() == null) {
            throw new ApiException(400, "Room id is required");
        }

        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            Room managedRoom = em.find(Room.class, room.getId());
            if (managedRoom == null) {
                em.getTransaction().rollback();
                return false;
            }

            Hotel managedHotel = managedRoom.getHotel();
            if (managedHotel == null || !managedHotel.getId().equals(hotel.getId())) {
                em.getTransaction().rollback();
                return false;
            }

            managedHotel.deleteRoom(managedRoom);
            managedHotel.setRooms(managedHotel.getAllRooms().size());
            em.remove(managedRoom);

            em.getTransaction().commit();
            return true;

        } catch (RuntimeException e) {
            throw new ApiException(500, "Could not remove room: " + e.getMessage());
        }
    }

    @Override
    public Room getRoomById(int id) {
        try (EntityManager em = emf.createEntityManager()) {
            Room room = em.find(Room.class, id);
            if (room != null && room.getHotel() != null) {
                room.getHotel().getId();
            }
            return room;
        }
    }

    @Override
    public List<Room> getRoomsForHotel(Hotel hotel) {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<Room> q;
            if (hotel.getId() != null) {
                q = em.createQuery(
                        "SELECT r FROM Room r JOIN FETCH r.hotel WHERE r.hotel.id = :id ORDER BY r.roomId",
                        Room.class);
                q.setParameter("id", hotel.getId());
            } else {
                return List.of();
            }

            return q.getResultList();
        }
    }

}
