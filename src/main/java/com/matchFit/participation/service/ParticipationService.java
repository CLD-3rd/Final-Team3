package com.matchFit.participation.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.matchFit.participation.dto.response.GetMyPostsParticipationResponseDto;
import com.matchFit.participation.entity.ApplicationStatus;
import com.matchFit.participation.entity.Participation;
import com.matchFit.participation.repository.ParticipationRepository;
import com.matchFit.post.entity.Post;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationService {
    
    private final ParticipationRepository participationRepository;
    
    public List<GetMyPostsParticipationResponseDto> GetMyPostsParticipation(Long userId) {
        List<Participation> participations = participationRepository.findByUserIdWithPost(userId);
        
        return participations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    private GetMyPostsParticipationResponseDto convertToDto(Participation participation) {
        // Participation에서 신청 상태 가져오기
        ApplicationStatus applicationStatus = participation.getStatus();
        
        Post post = participation.getPost();
        String title = post.getTitle();
        LocalDateTime date = post.getDate();
        Integer maxPeople = post.getMaxPeople();
        
        // 현재 승인된 참가자 수 계산
        int currentPeople = participationRepository.countByPostIdAndStatus(
                post.getId(), 
                ApplicationStatus.APPROVED
        );
        
        return new GetMyPostsParticipationResponseDto(
                title,                              
                date.toString(),                    
                currentPeople,                      // 계산된 값
                maxPeople,                          
                applicationStatus                   
        );
    }
}