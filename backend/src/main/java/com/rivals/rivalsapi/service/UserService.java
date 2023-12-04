package com.rivals.rivalsapi.service;

import com.rivals.rivalsapi.dto.challenge.ChallengeDto;
import com.rivals.rivalsapi.dto.user.UserDto;
import com.rivals.rivalsapi.model.Challenge;
import com.rivals.rivalsapi.model.User;
import com.rivals.rivalsapi.repository.ChallengeRepository;
import com.rivals.rivalsapi.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.misc.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ChallengeRepository challengeRepository;
    private final UtilityService utilityService;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public Page<UserDto> getAllUsers(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber,pageSize);
        return userRepository.findAll(pageable)
                .map(UserDto::fromUser);
    }
    public UserDto followUser(String username) {
        Pair<User, User> userContext = utilityService.getContextUsers(username);
        User user = userContext.a;
        User target = userContext.b;

        if (user.equals(target)) throw new IllegalArgumentException("You can't follow yourself");
        if (user.getFollowing().contains(target)) throw new IllegalArgumentException("You're already following this user");
        user.follow(target);
        userRepository.save(user);
        logger.info("User {} started following {}", user.getUsername(), target.getUsername());
        return UserDto.fromUser(target);
    }

    public UserDto unfollowUser(String username) {
        Pair<User, User> userContext = utilityService.getContextUsers(username);
        User user = userContext.a;
        User target = userContext.b;

        if (user.equals(target)) throw new IllegalArgumentException("You can't unfollow yourself");
        if (!user.getFollowing().contains(target)) throw new IllegalArgumentException("You're not following this user");
        user.unfollow(target);
        userRepository.save(user);
        logger.info("User {} stopped following {}", user.getUsername(), target.getUsername());
        return UserDto.fromUser(target);
    }

    public List<UserDto> getFollowing() {
        User user = utilityService.getContextUser();
        return user.getFollowing().stream()
                .map(UserDto::fromUser)
                .collect(Collectors.toList());
    }

    public List<UserDto> getFollowers() {
        User user = utilityService.getContextUser();

        return user.getFollowers().stream()
                .map(UserDto::fromUser)
                .collect(Collectors.toList());
    }

    public Page<ChallengeDto> getStarredChallenges(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        List<ChallengeDto> challengeList = utilityService.getContextUser().getStarred().stream()
                .map(ChallengeDto::fromChallenge).toList();

        final int start = (int)pageable.getOffset();
        final int end = Math.min((start + pageable.getPageSize()), challengeList.size());
        if(start > end)  return new PageImpl<>(new ArrayList<>(), pageable, challengeList.size());
        return new PageImpl<>(challengeList.subList(start, end), pageable, challengeList.size());
    }

    public ChallengeDto starChallenge(Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new EntityNotFoundException("Challenge not found"));
        User user = utilityService.getContextUser();
        user.starChallenge(challenge);
        userRepository.save(user);
        logger.info("Challenge with id: {} has been starred by {}", challenge.getId(), user.getUsername());
        return ChallengeDto.fromChallenge(challenge);
    }

    public Object unstarChallenge(Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new EntityNotFoundException("Challenge not found"));
        User user = utilityService.getContextUser();
        user.unstarChallenge(challenge);
        userRepository.save(user);
        logger.info("Challenge with id: {} has been starred by {}", challenge.getId(), user.getUsername());
        return ChallengeDto.fromChallenge(challenge);
    }
}
