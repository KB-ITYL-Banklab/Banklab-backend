package com.banklab.risk;

import com.banklab.config.MailConfig;
import com.banklab.config.RedisConfig;
import com.banklab.config.RootConfig;
import com.banklab.product.service.ProductService;
import com.banklab.risk.mapper.ProductRiskRatingMapper;
import com.banklab.risk.domain.ProductRiskRating;
import com.banklab.risk.service.RiskAnalysisService;
import com.banklab.security.config.SecurityConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.Scanner;

public class RiskAnalysisRunner {
    public static void main(String[] args) {
        System.out.println("=== 위험도 분석 실행기 ===");

        try {
            // Spring 컨텍스트 로드
            AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext(RootConfig.class, SecurityConfig.class, RedisConfig.class, MailConfig.class);

            // 필요한 서비스 가져오기
            RiskAnalysisService riskAnalysisService = context.getBean(RiskAnalysisService.class);
            ProductService productService = context.getBean(ProductService.class);
            ProductRiskRatingMapper riskMapper = context.getBean(ProductRiskRatingMapper.class);


            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.println("\n=== 위험도 분석 메뉴 ===");
                System.out.println("1. 현재 위험도 분석 결과 조회");
                System.out.println("2. 모든 상품 위험도 분석 실행");
                System.out.println("3. 위험도 분석 결과 삭제");
                System.out.println("4. 상품 개수 확인");
                System.out.println("0. 종료");
                System.out.print("선택: ");

                String input = scanner.nextLine().trim();

                switch (input) {
                    case "1":
                        showCurrentRiskAnalysis(riskMapper);
                        break;
                    case "2":
                        runRiskAnalysis(riskAnalysisService);
                        break;
                    case "3":
                        deleteAllRiskAnalysis(riskMapper);
                        break;
                    case "4":
                        showProductCounts(productService);
                        break;
                    case "0":
                        System.out.println("프로그램을 종료합니다.");
                        context.close();
                        return;
                    default:
                        System.out.println("잘못된 선택입니다. 다시 선택해주세요.");
                }
            }

        } catch (Exception e) {
            System.err.println("위험도 분석 실행 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void showCurrentRiskAnalysis(ProductRiskRatingMapper riskMapper) {
        System.out.println("\n=== 현재 위험도 분석 결과 ===");
        try {
            List<ProductRiskRating> ratings = riskMapper.findAll();

            if (ratings.isEmpty()) {
                System.out.println("위험도 분석 결과가 없습니다.");
                return;
            }

            System.out.printf("총 %d개의 위험도 분석 결과가 있습니다.\n", ratings.size());
            System.out.println("상위 10개 결과:");
            System.out.println("─".repeat(120));
            System.out.printf("%-15s %-15s %-30s %-15s %-25s %-15s\n",
                "상품타입", "위험등급", "상품명", "금융회사", "분석일시", "상품ID");
            System.out.println("─".repeat(120));

            for (int i = 0; i < Math.min(10, ratings.size()); i++) {
                ProductRiskRating rating = ratings.get(i);
                String productName = rating.getProductName();
                if (productName != null && productName.length() > 28) {
                    productName = productName.substring(0, 25) + "...";
                }
                String companyName = rating.getCompanyName();
                if (companyName != null && companyName.length() > 23) {
                    companyName = companyName.substring(0, 20) + "...";
                }

                System.out.printf("%-15s %-15s %-30s %-15s %-25s %-15s\n",
                    rating.getProductType(),
                    rating.getRiskLevel(),
                    productName,
                    companyName,
                    rating.getAnalyzedAt().toString().substring(0, 19),
                    rating.getProductId());
            }

            if (ratings.size() > 10) {
                System.out.printf("... 외 %d개 더\n", ratings.size() - 10);
            }

        } catch (Exception e) {
            System.err.println("위험도 분석 결과 조회 중 오류 발생: " + e.getMessage());
        }
    }

    private static void runRiskAnalysis(RiskAnalysisService riskAnalysisService) {
        System.out.println("\n=== 모든 상품 위험도 분석 실행 ===");
        try {
            System.out.println("모든 상품에 대한 위험도 분석을 시작합니다...");
            System.out.println("이 작업은 시간이 걸릴 수 있습니다.");

            // 시작 시간 기록
            long startTime = System.currentTimeMillis();
            
            riskAnalysisService.batchAnalyzeAllProductsRisk();
            
            // 종료 시간 기록
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            System.out.println("위험도 분석이 완료되었습니다.");
            System.out.printf("소요 시간: %.2f초\n", duration / 1000.0);

        } catch (Exception e) {
            System.err.println("위험도 분석 실행 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void deleteAllRiskAnalysis(ProductRiskRatingMapper riskMapper) {
        System.out.println("\n=== 위험도 분석 결과 삭제 ===");
        try {
            System.out.print("정말로 모든 위험도 분석 결과를 삭제하시겠습니까? (y/N): ");
            Scanner scanner = new Scanner(System.in);
            String confirm = scanner.nextLine().trim().toLowerCase();

            if ("y".equals(confirm) || "yes".equals(confirm)) {
                riskMapper.deleteAll();
                System.out.println("모든 위험도 분석 결과가 삭제되었습니다.");
            } else {
                System.out.println("삭제가 취소되었습니다.");
            }

        } catch (Exception e) {
            System.err.println("위험도 분석 결과 삭제 중 오류 발생: " + e.getMessage());
        }
    }

    private static void showProductCounts(ProductService productService) {
        System.out.println("\n=== 상품 개수 확인 ===");
        try {
            int depositCount = productService.getAllDepositProducts().size();
            int savingsCount = productService.getAllSavingsProducts().size();
            int creditLoanCount = productService.getAllCreditLoanProducts().size();
            int totalCount = depositCount + savingsCount + creditLoanCount;

            System.out.printf("예금 상품: %d개\n", depositCount);
            System.out.printf("적금 상품: %d개\n", savingsCount);
            System.out.printf("신용대출 상품: %d개\n", creditLoanCount);
            System.out.printf("총합: %d개\n", totalCount);

        } catch (Exception e) {
            System.err.println("상품 개수 조회 중 오류 발생: " + e.getMessage());
        }
    }
}
