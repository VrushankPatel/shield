package com.shield.module.unit.mapper;

import com.shield.module.unit.dto.UnitResponse;
import com.shield.module.unit.entity.UnitEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UnitMapper {

    UnitResponse toResponse(UnitEntity entity);
}
