package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.LocationDto;
import ru.practicum.entity.Location;

@UtilityClass
public class LocationMapper {
    public Location mapToLocation(LocationDto dto) {
        return Location.builder()
                .lon(dto.getLon())
                .lat(dto.getLat())
                .build();
    }

    public LocationDto mapToLocationDto(Location location) {
        return LocationDto.builder()
                .lon(location.getLon())
                .lat(location.getLat())
                .build();
    }
}
