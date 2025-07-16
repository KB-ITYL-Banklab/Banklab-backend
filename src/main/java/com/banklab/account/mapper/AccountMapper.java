package com.banklab.account.mapper;

import com.banklab.account.domain.AccountVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AccountMapper {

    int insertAccount(AccountVO accountVO);

    List<AccountVO> selectAccountsByUserId(@Param("userId") String userId);
}
