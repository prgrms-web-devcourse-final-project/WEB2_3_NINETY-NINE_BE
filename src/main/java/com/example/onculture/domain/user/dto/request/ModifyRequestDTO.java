package com.example.onculture.domain.user.dto.request;

import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ModifyRequestDTO {

    private String nickname;
    private String password;
    private String description = "";
    private Set<String> interests = new HashSet<>();
    private String profileImage = "";
}
