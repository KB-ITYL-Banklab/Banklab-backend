package com.banklab.member.handler;

import com.banklab.member.domain.Gender;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(Gender.class)
public class GenderTypeHandler extends BaseTypeHandler<Gender> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Gender gender, JdbcType jdbcType) throws SQLException {
        ps.setString(i, gender.getCode());  // enum → DB 값 (MALE → "M")
    }

    @Override
    public Gender getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return Gender.fromCode(rs.getString(columnName));  // DB 값 → enum ("F" → FEMALE)
    }

    @Override
    public Gender getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return Gender.fromCode(rs.getString(columnIndex));
    }

    @Override
    public Gender getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return Gender.fromCode(cs.getString(columnIndex));
    }
}
