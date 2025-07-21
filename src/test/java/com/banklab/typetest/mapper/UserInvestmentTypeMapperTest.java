package com.banklab.typetest.mapper;

import com.banklab.typetest.domain.UserInvestmentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserInvestmentTypeMapper 테스트")
class UserInvestmentTypeMapperTest {

    @Mock
    private UserInvestmentTypeMapper mapper;

    @Test
    @DisplayName("존재하지 않는 사용자 ID로 조회 시 null을 반환한다")
    void 존재하지_않는_사용자ID_조회_시_null_반환_테스트() {
        // Given
        Long nonExistentUserId = 99L;
        when(mapper.findByUserId(nonExistentUserId)).thenReturn(null);

        // When
        UserInvestmentType result = mapper.findByUserId(nonExistentUserId);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("사용자 투자성향을 성공적으로 업데이트한다")
    void 사용자_투자성향_업데이트_테스트() {
        // Given
        UserInvestmentType userInvestmentType = createUserInvestmentType(1L, 2L);
        doNothing().when(mapper).updateUserInvestmentType(userInvestmentType);

        // When
        mapper.updateUserInvestmentType(userInvestmentType);

        // Then
        verify(mapper, times(1)).updateUserInvestmentType(userInvestmentType);
    }

    private UserInvestmentType createUserInvestmentType(Long userId, Long investmentTypeId) {
        UserInvestmentType userInvestmentType = new UserInvestmentType();
        userInvestmentType.setUserId(userId);
        userInvestmentType.setInvestmentTypeId(investmentTypeId);
        return userInvestmentType;
    }
}
