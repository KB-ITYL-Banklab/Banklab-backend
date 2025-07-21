package com.banklab.product.mapper;

import com.banklab.product.domain.DepositProduct;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DepositProductMapper {
    List<DepositProduct> findAllDepositProducts();
    void insertDepositProduct(DepositProduct product);
    void deleteAllDepositProducts();


}
