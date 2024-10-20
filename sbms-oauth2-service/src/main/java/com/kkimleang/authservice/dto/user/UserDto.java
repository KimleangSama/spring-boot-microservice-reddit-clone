package com.kkimleang.authservice.dto.user;

import com.kkimleang.authservice.enumeration.AuthProvider;
import com.kkimleang.authservice.model.User;
import lombok.*;

import java.io.Serializable;
import java.util.Set;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserDto implements Serializable {
    private Long id;
    private String username;
    private String email;
    private String imageUrl;
    private AuthProvider provider;
    private Set<RoleDto> roles;

    public static UserDto fromUser(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        userDto.setImageUrl(user.getImageUrl());
        userDto.setProvider(user.getProvider());
        userDto.setRoles(RoleDto.fromRoles(user.getRoles()));
        return userDto;
    }
}
