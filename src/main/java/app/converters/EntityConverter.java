package app.converters;

import java.util.Arrays;
import java.util.List;


public class EntityConverter {


    public Hotel fromHotel(HotelDTO hotelDTO) {
        if (hotelDTO == null) {
            return null;
        }
        Hotel h = new Hotel();
        if (hotelDTO.id() != null) h.setId(hotelDTO.id());
        h.setName(hotelDTO.name());
        h.setAddress(hotelDTO.address());
        if (hotelDTO.rooms() != null) {
            hotelDTO.rooms().stream()
                    .map(this::fromRoom)
                    .forEach(h::addRoom);
        }
        h.setRooms(h.getAllRooms().size());
        return h;
    }

    public Room fromRoom(RoomDTO roomDTO) {
        if (roomDTO == null) {
            return null;
        }
        Room r = new Room();
        if (roomDTO.id()  != null) r.setId(roomDTO.id());
        r.setRoomNumber(roomDTO.number());
        r.setPrice(roomDTO.price());
        if (roomDTO.hotelId() != null) {
            Hotel h = new Hotel();
            h.setId(roomDTO.hotelId());
            r.setHotel(h); // attach by id; service layer can load/verify
        }
        return r;
    }

    public List<Hotel> fromHotelList(HotelDTO[] hotelDTOS) {
        List<Hotel> hotels = Arrays.stream(hotelDTOS)
                .map((h) -> fromHotel(h)).toList();

        return hotels;
    }

    public List<Room> fromRoomList(RoomDTO[] roomDTOS) {
        List<Room> rooms = Arrays.stream(roomDTOS)
                .map((r) -> fromRoom(r)).toList();

        return rooms;
    }

}
