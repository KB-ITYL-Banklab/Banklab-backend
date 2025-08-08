package com.banklab.financeContents.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 실시간 데이터 전용 DTO (불필요한 필드 제거)
 */
@Data
public class RealtimeDataDto {
    private String market;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private Date candleDateTime;
    
    private BigDecimal openingPrice;
    private BigDecimal tradePrice;
    private BigDecimal prevClosingPrice;
    private Double changeRate;
    private Double accTradeVolume24h;
    private Double accTradePrice24h;
    
    /**
     * FinanceUpbit을 RealtimeDataDto로 변환
     */
    public static RealtimeDataDto from(com.banklab.financeContents.domain.FinanceUpbit financeUpbit) {
        RealtimeDataDto dto = new RealtimeDataDto();
        dto.setMarket(financeUpbit.getMarket());
        dto.setCandleDateTime(financeUpbit.getCandleDateTime());
        dto.setOpeningPrice(financeUpbit.getOpeningPrice());
        dto.setTradePrice(financeUpbit.getTradePrice());
        dto.setPrevClosingPrice(financeUpbit.getPrevClosingPrice());
        dto.setChangeRate(financeUpbit.getChangeRate());
        dto.setAccTradeVolume24h(financeUpbit.getAccTradeVolume24h());
        dto.setAccTradePrice24h(financeUpbit.getAccTradePrice24h());
        return dto;
    }
}
