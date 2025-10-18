package app.dto;

import java.util.Set;


//TODO: HotelDTO: id, name, address, rooms

public record HotelDTO(
        Integer id,
        String name,
        String address,
        Set<RoomDTO> rooms
) { }
