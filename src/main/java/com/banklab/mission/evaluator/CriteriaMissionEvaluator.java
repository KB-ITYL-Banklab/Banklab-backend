package com.banklab.mission.evaluator;

import com.banklab.account.dto.AccountDTO;
import com.banklab.account.service.AccountService;
import com.banklab.mission.domain.ConditionKey;
import com.banklab.mission.domain.MissionVO;
import com.banklab.stock.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class CriteriaMissionEvaluator implements MissionEvaluator {

    private final AccountService accountService;
    private final StockService stockService;

    @Override
    public boolean evaluate(Long memberId, MissionVO mission) {
        ConditionKey key = mission.getConditionKey();
        int target = mission.getTargetValue();

        List<AccountDTO> bankAccounts = accountService.getUserAccounts(memberId);
        int securitiesCount = stockService.getUserStocks(memberId).size();

        return switch (key) {
            case NO_NON_CASH_ASSET -> hasOnlyCashAssetOrNone(bankAccounts, securitiesCount);
            case SAVINGS_PRODUCT_COUNT -> countSavingsProducts(bankAccounts) >= target;
            case PURPOSED_ASSET_COUNT -> countPurposedAssets(bankAccounts, securitiesCount) >= target;
            case ASSET_CATEGORY_COUNT -> countAssetCategories(bankAccounts, securitiesCount) >= target;
            default -> throw new UnsupportedOperationException("Unknown key: " + key);
        };
    }

    // 입출금 외 금융 상품 존재 여부
    private boolean hasOnlyCashAssetOrNone(List<AccountDTO> bankAccounts, int securitiesCount) {
        if ((bankAccounts == null || bankAccounts.isEmpty()) && securitiesCount == 0) return true;

        // 은행계좌 전부 수시입출이어야 하며, 증권 없어야 함
        boolean onlyCash = bankAccounts.stream()
                .allMatch(acc -> "11".equals(acc.getResAccountDeposit()));
        return onlyCash && securitiesCount == 0;
    }

    // 정기예금/적금 상품 보유 수
    private long countSavingsProducts(List<AccountDTO> bankAccounts) {
        return bankAccounts.stream()
                .filter(acc -> "12".equals(acc.getResAccountDeposit()))
                .count();
    }

    // 입출금 제외 금융상품 보유 수
    private long countPurposedAssets(List<AccountDTO> bankAccounts, int securitiesCount) {
        long bankCount = bankAccounts.stream()
                .filter(acc -> !"11".equals(acc.getResAccountDeposit())) // 수시입출 제외
                .count();

        return bankCount + securitiesCount;
    }

    // 입출금 제외한 자산 종류 수
    private long countAssetCategories(List<AccountDTO> bankAccounts, int securitiesCount) {
        Set<String> categoryCodes = new HashSet<>();

        bankAccounts.stream()
                .map(AccountDTO::getResAccountDeposit)
                .filter(resAccountDeposit -> !"11".equals(resAccountDeposit))
                .forEach(categoryCodes::add);

        if (securitiesCount > 0) {
            categoryCodes.add("SECURITIES");
        }

        return categoryCodes.size();
    }
}
