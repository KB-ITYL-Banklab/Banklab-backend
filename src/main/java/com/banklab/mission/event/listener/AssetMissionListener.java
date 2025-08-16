package com.banklab.mission.event.listener;

import com.banklab.mission.event.AssetSyncedEvent;
import com.banklab.mission.service.MissionProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class AssetMissionListener {

    private final MissionProgressService missionProgressService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAssetSynced(AssetSyncedEvent e) {
        // 자산 기반(CRITERIA) 미션만 평가·보상
        missionProgressService.onCriteriaChanged(e.getMemberId());
    }
}

