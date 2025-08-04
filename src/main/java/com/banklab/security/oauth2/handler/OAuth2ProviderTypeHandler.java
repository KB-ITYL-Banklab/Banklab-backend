package com.banklab.security.oauth2.handler;

import com.banklab.security.oauth2.domain.OAuth2Provider;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(OAuth2Provider.class)
public class OAuth2ProviderTypeHandler extends BaseTypeHandler<OAuth2Provider> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, OAuth2Provider parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.name()); // Enum을 문자열로 변환하여 저장
    }

    @Override
    public OAuth2Provider getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return OAuth2Provider.valueOf(rs.getString(columnName)); // 데이터베이스에서 가져온 문자열을 Enum으로 변환
    }

    @Override
    public OAuth2Provider getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return OAuth2Provider.valueOf(rs.getString(columnIndex)); // 데이터베이스에서 가져온 문자열을 Enum으로 변환
    }

    @Override
    public OAuth2Provider getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return OAuth2Provider.valueOf(cs.getString(columnIndex)); // 데이터베이스에서 가져온 문자열을 Enum으로 변환
    }
}
