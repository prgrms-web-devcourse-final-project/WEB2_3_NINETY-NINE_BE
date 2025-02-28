package com.example.onculture.domain.user.dto.request;

import com.example.onculture.domain.user.model.Interest;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
    private Set<Interest> interests = new HashSet<>();
    private String profileImage = "";
}
