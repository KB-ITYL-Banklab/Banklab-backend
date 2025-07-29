package com.banklab.risk.handler;

import com.banklab.risk.domain.RiskLevel;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RiskLevelHandler extends BaseTypeHandler<RiskLevel> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, RiskLevel parameter, JdbcType jdbcType) throws SQLException {
        // RiskLevel Enum을 DB에 저장할 때 String 값으로 저장
        ps.setString(i, parameter.name());
    }

    @Override
    public RiskLevel getNullableResult(ResultSet rs, String columnName) throws SQLException {
        // DB에서 가져온 값을 Enum으로 변환
        String value = rs.getString(columnName);
        if (value != null) {
            return RiskLevel.valueOf(value);
        }
        return null;
    }

    @Override
    public RiskLevel getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        // DB에서 가져온 값을 Enum으로 변환
        String value = rs.getString(columnIndex);
        if (value != null) {
            return RiskLevel.valueOf(value);
        }
        return null;
    }

    @Override
    public RiskLevel getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        // CallableStatement에서 값을 가져올 때 처리
        String value = cs.getString(columnIndex);
        if (value != null) {
            return RiskLevel.valueOf(value);
        }
        return null;
    }
}
