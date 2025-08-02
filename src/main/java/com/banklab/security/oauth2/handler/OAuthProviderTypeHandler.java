package com.banklab.security.oauth2.handler;

import com.banklab.security.oauth2.domain.OAuthProvider;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(OAuthProvider.class)
public class OAuthProviderTypeHandler extends BaseTypeHandler<OAuthProvider> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, OAuthProvider parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.name()); // Enum을 문자열로 변환하여 저장
    }

    @Override
    public OAuthProvider getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return OAuthProvider.valueOf(rs.getString(columnName)); // 데이터베이스에서 가져온 문자열을 Enum으로 변환
    }

    @Override
    public OAuthProvider getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return OAuthProvider.valueOf(rs.getString(columnIndex)); // 데이터베이스에서 가져온 문자열을 Enum으로 변환
    }

    @Override
    public OAuthProvider getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return OAuthProvider.valueOf(cs.getString(columnIndex)); // 데이터베이스에서 가져온 문자열을 Enum으로 변환
    }
}
