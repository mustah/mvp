package com.elvaco.mvp.web.mapper;

import com.elvaco.mvp.web.dto.UserTokenDto;
import com.elvaco.mvp.web.security.MvpUserDetails;

import lombok.experimental.UtilityClass;

@UtilityClass
public class UserTokenDtoMapper {

  public static UserTokenDto toUserTokenDto(MvpUserDetails mvpUserDetails) {
    return new UserTokenDto(
      UserDtoMapper.toDto(mvpUserDetails.getUser()),
      mvpUserDetails.getToken()
    );
  }
}
