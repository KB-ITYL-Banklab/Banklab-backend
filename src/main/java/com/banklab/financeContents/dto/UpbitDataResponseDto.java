package com.banklab.financeContents.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * 업비트 데이터 응답용 DTO (Spring Legacy 호환)
 */
@Data
public class UpbitDataResponseDto {
    private Long id;
    private String market;
    
    // 날짜 포맷 지정 (JSON 응답에서 보기 좋게)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private Date candleDateTime;
    
    private BigDecimal openingPrice;
    private BigDecimal tradePrice;
    private BigDecimal prevClosingPrice;
    private Double changeRate;
    private Double accTradeVolume24h;
    private Double accTradePrice24h;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private Date createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private Date updatedAt;
    
    // 추가 정보 (계산된 필드)
    private String candleDateFormatted;      // "2025년 7월 15일"
    private String changeRatePercent;        // "-0.75%"
    private String tradePriceFormatted;      // "159,000,000원"
    
    /**
     * Domain을 ResponseDto로 변환하는 정적 메서드
     */
    public static UpbitDataResponseDto from(com.banklab.financeContents.domain.FinanceUpbit domain) {
        UpbitDataResponseDto dto = new UpbitDataResponseDto();
        dto.setId(domain.getId());
        dto.setMarket(domain.getMarket());
        dto.setCandleDateTime(domain.getCandleDateTime());
        dto.setOpeningPrice(domain.getOpeningPrice());
        dto.setTradePrice(domain.getTradePrice());
        dto.setPrevClosingPrice(domain.getPrevClosingPrice());
        dto.setChangeRate(domain.getChangeRate());
        dto.setAccTradeVolume24h(domain.getAccTradeVolume24h());
        dto.setAccTradePrice24h(domain.getAccTradePrice24h());
        dto.setCreatedAt(domain.getCreatedAt());
        dto.setUpdatedAt(domain.getUpdatedAt());
        
        // 추가 포맷팅 (Date 타입용)
        if (domain.getCandleDateTime() != null) {
            try {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy년 M월 d일 HH:mm");
                dto.setCandleDateFormatted(formatter.format(domain.getCandleDateTime()));
            } catch (Exception e) {
                dto.setCandleDateFormatted("날짜 형식 오류");
            }
        }
        
        if (domain.getChangeRate() != null) {
            dto.setChangeRatePercent(
                String.format("%.2f%%", domain.getChangeRate() * 100)
            );
        }
        
        if (domain.getTradePrice() != null) {
            dto.setTradePriceFormatted(
                String.format("%,d원", domain.getTradePrice().longValue())
            );
        }
        
        return dto;
    }
}
