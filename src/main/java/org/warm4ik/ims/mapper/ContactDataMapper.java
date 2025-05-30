package org.warm4ik.ims.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.warm4ik.ims.dto.internship.InternshipContactDataDto;
import org.warm4ik.ims.entity.InternshipContactData;

@Mapper(componentModel = "spring")
public interface ContactDataMapper {

  @Mappings({
    @Mapping(source = "contactType", target = "type"),
    @Mapping(source = "contactValue", target = "value")
  })
  InternshipContactDataDto contactDataToInternshipContactDataDto(InternshipContactData contactData);

  @Mappings({
    @Mapping(source = "type", target = "contactType"),
    @Mapping(source = "value", target = "contactValue")
  })
  InternshipContactData internshipContactDataDtoToInternshipContactData(
      InternshipContactDataDto internshipContactDataDto);
}
