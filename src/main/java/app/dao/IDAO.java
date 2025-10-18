package app.dao;

import java.util.List;

public interface IDAO {

    List<Hotel> getAllHotels();

    Hotel getHotelById(int id);

    Hotel createHotel(Hotel hotel);

    List<Hotel> createHotels(List<Hotel> hotels);

    Hotel updateHotel(Hotel hotel);

    boolean deleteHotel(Hotel hotel);

    boolean addRoom(Hotel hotel, Room room);

    boolean removeRoom(Hotel hotel, Room room);

    Room getRoomById(int id);

    List<Room> getRoomsForHotel(Hotel hotel);

}
