package com.banklab.mission.event.listener;

import com.banklab.character.service.CharacterService;
import com.banklab.mission.event.MissionCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class CharacterEventListener {

    private final CharacterService characterService;
    private final ApplicationEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMissionCompleted(MissionCompletedEvent event) {
        // 경험치 지급 & 레벨업 여부 판단
        boolean leveledUp = characterService.addExpAndLevelUp(
                event.getMemberId(),
                event.getRewardExp()
        );

        // 레벨업 했다면 알림 이벤트 발행
//        if (leveledUp) {
//            eventPublisher.publishEvent(
//                    new LevelUpEvent(event.getMemberId(), characterService.getCharacter(event.getMemberId()).getName())
//            );
//        }
    }
}
