package com.shield.module.user.mapper;

import com.shield.module.user.dto.UserResponse;
import com.shield.module.user.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(UserEntity entity);
}
