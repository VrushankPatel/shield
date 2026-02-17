package com.shield.module.tenant.mapper;

import com.shield.module.tenant.dto.TenantResponse;
import com.shield.module.tenant.entity.TenantEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TenantMapper {

    TenantResponse toResponse(TenantEntity entity);
}
