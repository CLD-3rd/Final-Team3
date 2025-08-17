package com.matchFit.post.policy;

import com.matchFit.post.entity.Post;
import com.matchFit.post.entity.Status;

import java.time.LocalDate;

public class PostNotificationPolicy {

    /**
     * 매치가 내일인 경우 true 반환
     * - 포스트 상태가 CLOSED여야 함
     * - 포스트 날짜가 오늘 + 1일과 같음
     */
    public static boolean isMatchTomorrow(Post post) {
        return post.getStatus() == Status.CLOSED &&
               post.getDate().toLocalDate().equals(LocalDate.now().plusDays(1));
    }
}