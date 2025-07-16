package com.banklab.typetest.mapper;

import com.banklab.typetest.domain.UserInvestmentType;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class UserInvestmentTypeMapperTest {

    @Test
    void findByUserId_returnsNullIfNotExists() {
        UserInvestmentTypeMapper mapper = Mockito.mock(UserInvestmentTypeMapper.class);
        Mockito.when(mapper.findByUserId(99L)).thenReturn(null);

        UserInvestmentType result = mapper.findByUserId(99L);
        assertNull(result);
    }

    @Test
    void updateUserInvestmentType_updatesSuccessfully() {
        UserInvestmentTypeMapper mapper = Mockito.mock(UserInvestmentTypeMapper.class);
        UserInvestmentType userInvestmentType = new UserInvestmentType();
        userInvestmentType.setUserId(1L);
        userInvestmentType.setInvestmentTypeId(2L);

        Mockito.doNothing().when(mapper).updateUserInvestmentType(userInvestmentType);

        mapper.updateUserInvestmentType(userInvestmentType);

        Mockito.verify(mapper, Mockito.times(1)).updateUserInvestmentType(userInvestmentType);
    }
}
