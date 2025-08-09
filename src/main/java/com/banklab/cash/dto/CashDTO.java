package com.banklab.cash.dto;

import com.banklab.cash.domain.CashVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CashDTO {

    private Long id;                // 현금 ID
    private Long memberId;          // 회원 ID
    private Long cashAmount;        // 현금 금액

    /**
     * DTO를 VO로 변환
     * @return CashVO 객체
     */
    public CashVO toVO() {
        return CashVO.builder()
                .id(this.id)
                .memberId(this.memberId)
                .cashAmount(this.cashAmount)
                .build();
    }

    /**
     * VO를 DTO로 변환하는 정적 메서드
     * @param cashVO CashVO 객체
     * @return CashDTO 객체
     */
    public static CashDTO fromVO(CashVO cashVO) {
        if (cashVO == null) return null;

        return CashDTO.builder()
                .id(cashVO.getId())
                .memberId(cashVO.getMemberId())
                .cashAmount(cashVO.getCashAmount())
                .build();
    }
}
