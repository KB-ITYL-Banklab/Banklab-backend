package com.banklab.risk.mapper;

import com.banklab.risk.domain.ProductRiskRating;
import com.banklab.product.domain.ProductType;
import com.banklab.risk.domain.RiskLevel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ProductRiskRatingMapper {
    
    // 위험도 평가 저장
    void insertRiskRating(ProductRiskRating riskRating);
    
    // 위험도 평가 업데이트
    void updateRiskRating(ProductRiskRating riskRating);
    
    // 특정 상품 위험도 조회
    ProductRiskRating selectByProductTypeAndId(@Param("productType") ProductType productType, 
                                               @Param("productId") Long productId);
    
    // 위험도별 상품 목록 조회
    List<ProductRiskRating> selectByRiskLevel(@Param("riskLevel") RiskLevel riskLevel);
    
    // 기간별 위험도 평가 조회
    List<ProductRiskRating> selectByEvaluatedAtBetween(@Param("from") LocalDateTime from, 
                                                       @Param("to") LocalDateTime to);
    
    // 모든 위험도 평가 조회
    List<ProductRiskRating> selectAll();
    
    // ID로 위험도 평가 조회
    ProductRiskRating selectById(@Param("id") Long id);
    
    // 위험도 평가 삭제
    void deleteById(@Param("id") Long id);
    
    // 모든 위험도 평가 조회 (별칭)
    List<ProductRiskRating> findAll();
    
    // 모든 위험도 평가 삭제
    void deleteAll();
}
