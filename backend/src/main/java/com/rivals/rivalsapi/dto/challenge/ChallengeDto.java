package com.rivals.rivalsapi.dto.challenge;

import com.rivals.rivalsapi.dto.user.UserDto;
import com.rivals.rivalsapi.model.Challenge;
import com.rivals.rivalsapi.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeDto {
    private Long id;
    private UserDto creator;
    private LocalDateTime creationDate;
    private String title;
    private String description;
    private Integer stars;

    public static ChallengeDto fromChallenge(Challenge challenge) {
        Integer stars = challenge.getStars() == null ? 0 : challenge.getStars().size();
        return ChallengeDto.builder()
                .id(challenge.getId())
                .creator(UserDto.fromUser(challenge.getCreator()))
                .creationDate(challenge.getCreationDate())
                .title(challenge.getTitle())
                .description(challenge.getDescription())
                .stars(stars)
                .build();
    }
}
