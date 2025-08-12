package com.banklab.mission.event.listener;

import com.banklab.character.service.CharacterService;
import com.banklab.mission.event.MissionCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
public class CharacterEventListener {
    private final CharacterService characterService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMissionCompleted(MissionCompletedEvent event) {
        // 경험치 지급 & 레벨업 여부 판단
        characterService.addExpAndLevelUp(
                event.getMemberId(),
                event.getRewardExp()
        );
    }
}