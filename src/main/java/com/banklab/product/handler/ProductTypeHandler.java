package com.banklab.product.handler;

import com.banklab.product.domain.ProductType;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProductTypeHandler extends BaseTypeHandler<ProductType> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, ProductType parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.name());
    }

    @Override
    public ProductType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        if (value != null) {
            return ProductType.valueOf(value);
        }
        return null;
    }

    @Override
    public ProductType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        if (value != null) {
            return ProductType.valueOf(value);
        }
        return null;
    }

    @Override
    public ProductType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        if (value != null) {
            return ProductType.valueOf(value);
        }
        return null;
    }
}
