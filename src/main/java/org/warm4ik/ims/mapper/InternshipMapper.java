package org.warm4ik.ims.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.warm4ik.ims.dto.internship.InternshipResponseDto;
import org.warm4ik.ims.entity.Internship;

@Mapper(
    componentModel = "spring",
    uses = {CategoryMapper.class, ContactDataMapper.class})
public interface InternshipMapper {

  @Mappings({
    @Mapping(source = "id", target = "internshipId"),
    @Mapping(source = "categories", target = "categoriesData")
  })
  InternshipResponseDto internshipToResponseInternshipDto(Internship internship);
}
