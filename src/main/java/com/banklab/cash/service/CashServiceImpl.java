package com.banklab.cash.service;

import com.banklab.cash.domain.CashVO;
import com.banklab.cash.dto.CashDTO;
import com.banklab.cash.mapper.CashMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
public class CashServiceImpl implements CashService {

    private final CashMapper cashMapper;

    @Override
    public CashDTO getCashByMemberId(Long memberId) {
        log.info("현금 정보 조회 - memberId: {}", memberId);

        CashVO cashVO = cashMapper.selectCashByMemberId(memberId);

        // 현금 정보가 없으면 0원으로 초기화
        if (cashVO == null) {
            log.info("현금 정보 없음. 0원으로 초기화 - memberId: {}", memberId);
            return setCashAmount(memberId, 0L);
        }

        log.info("현금 정보 조회 완료 - memberId: {}, amount: {}", memberId, cashVO.getCashAmount());
        return CashDTO.fromVO(cashVO);
    }

    @Override
    @Transactional
    public CashDTO setCashAmount(Long memberId, Long cashAmount) {
        log.info("현금 금액 설정 - memberId: {}, amount: {}", memberId, cashAmount);

        if (cashAmount == null) {
            cashAmount = 0L;
        }

        if (cashAmount < 0) {
            throw new IllegalArgumentException("현금 금액은 음수일 수 없습니다.");
        }

        CashVO existingCash = cashMapper.selectCashByMemberId(memberId);

        if (existingCash == null) {
            // 새로 생성
            CashVO cashVO = CashVO.builder()
                    .memberId(memberId)
                    .cashAmount(cashAmount)
                    .build();

            int insertedRows = cashMapper.insertCash(cashVO);
            if (insertedRows == 0) {
                throw new RuntimeException("현금 정보 생성에 실패했습니다.");
            }
            log.info("현금 정보 생성 완료 - memberId: {}, amount: {}", memberId, cashAmount);
        } else {
            // 기존 정보 업데이트
            int updatedRows = cashMapper.updateCashAmount(memberId, cashAmount);
            if (updatedRows == 0) {
                throw new RuntimeException("현금 정보 업데이트에 실패했습니다.");
            }
            log.info("현금 정보 업데이트 완료 - memberId: {}, amount: {}", memberId, cashAmount);
        }

        return getCashByMemberId(memberId);
    }
}
