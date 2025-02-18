package com.example.onculture.domain.user.dto.request;

import com.example.onculture.domain.user.domain.Gender;
import com.example.onculture.domain.user.domain.Interest;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModifyRequestDTO {

    private String nickname;
    private String description;
    private String birth;
    private Gender gender;
    private List<Interest> interests;
}
