package com.banklab.product.mapper;

import com.banklab.product.domain.SavingsOption;
import com.banklab.product.domain.SavingsProduct;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SavingsProductMapper {
    List<SavingsProduct> findAllSavingsProducts();
    void insertSavingsProduct(SavingsProduct product);
    void deleteAllSavingsProducts();
}
