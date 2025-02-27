package com.example.onculture.domain.user.dto.request;

import com.example.onculture.domain.user.model.Interest;
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
    private List<Interest> interests;
}
