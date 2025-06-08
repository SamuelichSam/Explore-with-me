package ru.practicum.rating.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import ru.practicum.rating.dto.RatingDto;
import ru.practicum.rating.model.Rating;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface RatingMapper {
    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "userId", source = "user.id")
    RatingDto toDto(Rating rating);
}
