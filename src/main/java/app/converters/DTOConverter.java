package app.converters;

import java.util.List;
import java.util.stream.Collectors;

public class DTOConverter {

    public HotelDTO fromHotel(Hotel hotel) {
       return new HotelDTO(
                hotel.getId(),
                hotel.getName(),
                hotel.getAddress(),
                hotel.getAllRooms().stream()
                        .map((room) -> this.fromRoom(room))
                        .collect(Collectors.toSet())
        );
    }

    public RoomDTO fromRoom(Room room) {
        return new RoomDTO(
                room.getId(),
                room.getHotel().getId(),
                room.getRoomNumber(),
                room.getPrice());
    }

    public HotelDTO[] fromHotelList(List<Hotel> hotels) {
        HotelDTO[] convertedHotels = hotels.stream()
                .map((h) ->  fromHotel(h))
                //(int n) -> new HotelDTO[n]
                .toArray(HotelDTO[]::new);

        return convertedHotels;
    }

    public RoomDTO[] fromRoomList(List<Room> rooms) {
        RoomDTO[] roomDTOS = rooms.stream().map((r) -> fromRoom(r))
                //(int n) -> new RoomDTO[n]
                .toArray(RoomDTO[]::new);

        return roomDTOS;
    }


}
