-- MatchFit 100,000건 더미 게시글 INSERT
-- 실행 전 기존 1000건 있으면 그대로 유지 (AUTO_INCREMENT 이어서 적용됨)
--
-- 실행 방법 (터미널):
--   mysql -h 127.0.0.1 -P 3307 -u root -proot_password matchFit < k6/insert-100k-posts.sql
--
-- 주의: user_id=1 이 존재해야 합니다. 없으면 WHERE절로 확인:
--   SELECT id FROM user LIMIT 1;

DROP PROCEDURE IF EXISTS insert_100k_posts;

CREATE PROCEDURE insert_100k_posts()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE sports_val VARCHAR(20);
    DECLARE gender_val VARCHAR(10);
    DECLARE town_val   VARCHAR(20);
    DECLARE max_people INT;
    DECLARE cost_val   INT;
    DECLARE future_date DATETIME;

    WHILE i <= 100000 DO
        -- 종목 순환 (6가지)
        SET sports_val = CASE (i % 6)
            WHEN 0 THEN 'FOOTBALL'
            WHEN 1 THEN 'BASKETBALL'
            WHEN 2 THEN 'BADMINTON'
            WHEN 3 THEN 'TABLE_TENNIS'
            WHEN 4 THEN 'VOLLEYBALL'
            ELSE         'TENNIS'
        END;

        -- 성별 순환
        SET gender_val = CASE (i % 2)
            WHEN 0 THEN 'MALE'
            ELSE        'FEMALE'
        END;

        -- 지역 순환 (8가지, Town enum 이름 사용)
        SET town_val = CASE (i % 8)
            WHEN 0 THEN 'SEOUL'
            WHEN 1 THEN 'BUSAN'
            WHEN 2 THEN 'DAEGU'
            WHEN 3 THEN 'INCHEON'
            WHEN 4 THEN 'GWANGJU'
            WHEN 5 THEN 'DAEJEON'
            WHEN 6 THEN 'GYEONGGI'
            ELSE        'ULSAN'
        END;

        -- 최대 인원 (4~12명 순환)
        SET max_people = 4 + (i % 9);

        -- 비용 (0~50000원, 10000원 단위)
        SET cost_val = (i % 6) * 10000;

        -- 날짜: 2026-05-01 ~ 2026-08-31 범위에서 순환 분산
        SET future_date = DATE_ADD(
            '2026-05-01 09:00:00',
            INTERVAL ((i % 120) * 1440 + (i % 24) * 60) MINUTE
        );

        INSERT INTO post (
            title, description, location, date,
            max_people, gender, cost, sports, town,
            status, image_url, created_at, updated_at, user_id
        ) VALUES (
            CONCAT('[', sports_val, '] 같이 ', sports_val, ' 하실 분 구합니다 #', i),
            CONCAT(sports_val, ' 모집합니다. 참가비 ', cost_val, '원. 실력 무관 환영!'),
            CONCAT('실내체육관 ', (i % 10) + 1, '호'),
            future_date,
            max_people,
            gender_val,
            cost_val,
            sports_val,
            town_val,
            'OPEN',
            NULL,
            NOW(),
            NOW(),
            1
        );

        SET i = i + 1;
    END WHILE;
END;

CALL insert_100k_posts();
DROP PROCEDURE IF EXISTS insert_100k_posts;

-- 확인
SELECT COUNT(*) AS total_posts FROM post;
SELECT sports, COUNT(*) AS cnt FROM post GROUP BY sports ORDER BY cnt DESC;
SELECT status, COUNT(*) AS cnt FROM post GROUP BY status;
