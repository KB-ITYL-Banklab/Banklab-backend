package com.banklab.mission.event.listener;

import com.banklab.mission.event.MissionTriggerEvent;
import com.banklab.mission.service.MissionProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class MissionEventListener {
    private final MissionProgressService missionProgressService;

    // 트랜잭션이 정상 커밋된 이후에만 실행
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMissionTrigger(MissionTriggerEvent event) {
        // onEvent 내부에서 레벨 게이팅, 완료여부(log), evaluator 호출, 멱등 지급까지 처리
        missionProgressService.onEvent(
                event.getMemberId(),
                event.getConditionKey()
        );
    }
}
