package app;

import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import app.populators.HotelPopulator;
import app.populators.RoomPopulator;
import jakarta.persistence.EntityManagerFactory;

public class Main {


    public static void main(String[] args) {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        IDAO dao = HotelDAO.getInstance(emf);


        //TODO: Populate DB with initial data
        // List<Hotel> hotels = HotelPopulator.populateHotels(dao);
       //  RoomPopulator.populateRooms(dao, hotels);

        ApplicationConfig.startServer(7070);

    }


}