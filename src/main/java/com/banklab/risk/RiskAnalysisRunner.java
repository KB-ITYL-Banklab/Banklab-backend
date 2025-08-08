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
        System.out.println("=== 위험도 분석 실행기 (최적화 버전) ===");

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
                System.out.println("2. 오늘 업데이트된 상품 위험도 분석 실행 (최적화)");
                System.out.println("3. 모든 상품 위험도 분석 실행 (전체)");
                System.out.println("4. 위험도 분석 결과 삭제");
                System.out.println("5. 상품 개수 확인 (전체 상품 유형)");
                System.out.println("\n--- 개별 상품 유형 분석 ---");
                System.out.println("11. 예금 상품 분석");
                System.out.println("12. 적금 상품 분석");
                System.out.println("13. 신용대출 상품 분석");
                System.out.println("14. 연금 상품 분석");
                System.out.println("15. 주택담보대출 상품 분석");
                System.out.println("16. 전세자금대출 상품 분석");
                System.out.println("0. 종료");
                System.out.print("선택: ");

                String input = scanner.nextLine().trim();

                switch (input) {
                    case "1":
                        showCurrentRiskAnalysis(riskMapper);
                        break;
                    case "2":
                        runTodayUpdatedRiskAnalysis(riskAnalysisService);
                        break;
                    case "3":
                        runFullRiskAnalysis(riskAnalysisService);
                        break;
                    case "4":
                        deleteAllRiskAnalysis(riskMapper);
                        break;
                    case "5":
                        showAllProductCounts(productService);
                        break;
                    case "11":
                        runDepositRiskAnalysis(riskAnalysisService);
                        break;
                    case "12":
                        runSavingsRiskAnalysis(riskAnalysisService);
                        break;
                    case "13":
                        runCreditLoanRiskAnalysis(riskAnalysisService);
                        break;
                    case "14":
                        runAnnuityRiskAnalysis(riskAnalysisService);
                        break;
                    case "15":
                        runMortgageLoanRiskAnalysis(riskAnalysisService);
                        break;
                    case "16":
                        runRentHouseLoanRiskAnalysis(riskAnalysisService);
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

            // 상품 유형별 통계 출력
            long depositCount = ratings.stream().filter(r -> "DEPOSIT".equals(r.getProductType().name())).count();
            long savingsCount = ratings.stream().filter(r -> "SAVINGS".equals(r.getProductType().name())).count();
            long creditLoanCount = ratings.stream().filter(r -> "CREDITLOAN".equals(r.getProductType().name())).count();
            long annuityCount = ratings.stream().filter(r -> "ANNUITY".equals(r.getProductType().name())).count();
            long mortgageCount = ratings.stream().filter(r -> "MORTGAGE".equals(r.getProductType().name())).count();
            long renthouseCount = ratings.stream().filter(r -> "RENTHOUSE".equals(r.getProductType().name())).count();

            System.out.println("\n상품 유형별 분석 결과:");
            System.out.printf("- 예금: %d개, 적금: %d개, 신용대출: %d개\n", depositCount, savingsCount, creditLoanCount);
            System.out.printf("- 연금: %d개, 주택담보대출: %d개, 전세자금대출: %d개\n", annuityCount, mortgageCount, renthouseCount);

            // 위험등급별 통계 출력
            long lowCount = ratings.stream().filter(r -> "LOW".equals(r.getRiskLevel().name())).count();
            long mediumCount = ratings.stream().filter(r -> "MEDIUM".equals(r.getRiskLevel().name())).count();
            long highCount = ratings.stream().filter(r -> "HIGH".equals(r.getRiskLevel().name())).count();

            System.out.println("\n위험등급별 분석 결과:");
            System.out.printf("- 저위험(LOW): %d개, 중위험(MEDIUM): %d개, 고위험(HIGH): %d개\n",
                    lowCount, mediumCount, highCount);

            System.out.println("\n상위 10개 결과:");
            System.out.println("─".repeat(120));
            System.out.printf("%-15s %-15s %-30s %-15s %-25s %-15s\n",
                    "상품타입", "위험등급", "상품명", "금융회사", "분석일시", "상품ID");
            System.out.println("─".repeat(120));

            for (int i = 0; i < Math.min(10, ratings.size()); i++) {
                ProductRiskRating rating = ratings.get(i);
                String productName = "상품명 정보 없음";
                String companyName = "회사명 정보 없음";

                if (productName != null && productName.length() > 28) {
                    productName = productName.substring(0, 25) + "...";
                }
                if (companyName != null && companyName.length() > 23) {
                    companyName = companyName.substring(0, 20) + "...";
                }

                System.out.printf("%-15s %-15s %-30s %-15s %-25s %-15s\n",
                        rating.getProductType(),
                        rating.getRiskLevel(),
                        productName,
                        companyName,
                        rating.getEvaluatedAt().toString().substring(0, 19),
                        rating.getProductId());
            }

            if (ratings.size() > 10) {
                System.out.printf("... 외 %d개 더\n", ratings.size() - 10);
            }

        } catch (Exception e) {
            System.err.println("위험도 분석 결과 조회 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void runTodayUpdatedRiskAnalysis(RiskAnalysisService riskAnalysisService) {
        System.out.println("\n=== 오늘 업데이트된 상품 위험도 분석 실행 (최적화) ===");
        try {
            System.out.println("오늘 업데이트된 상품에 대한 위험도 분석을 시작합니다...");
            System.out.println("이 방법은 전체 분석보다 훨씬 빠르고 효율적입니다.");

            // 시작 시간 기록
            long startTime = System.currentTimeMillis();

            riskAnalysisService.batchAnalyzeTodayUpdatedProductsRisk();

            // 종료 시간 기록
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            System.out.println("오늘 업데이트된 상품 위험도 분석이 완료되었습니다.");
            System.out.printf("소요 시간: %.2f초\n", duration / 1000.0);

        } catch (Exception e) {
            System.err.println("위험도 분석 실행 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void runFullRiskAnalysis(RiskAnalysisService riskAnalysisService) {
        System.out.println("\n=== 모든 상품 위험도 분석 실행 (전체) ===");
        try {
            System.out.println("모든 상품에 대한 위험도 분석을 시작합니다...");
            System.out.println("이 작업은 시간이 걸릴 수 있습니다. (모든 상품 유형 포함)");

            // 시작 시간 기록
            long startTime = System.currentTimeMillis();

            riskAnalysisService.batchAnalyzeAllProductsRisk();

            // 종료 시간 기록
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            System.out.println("전체 상품 위험도 분석이 완료되었습니다.");
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

    private static void showAllProductCounts(ProductService productService) {
        System.out.println("\n=== 전체 상품 개수 확인 ===");
        try {
            int depositCount = 0;
            int savingsCount = 0;
            int creditLoanCount = 0;

            try {
                depositCount = productService.getAllDepositProducts().size();
            } catch (Exception e) {
                System.out.println("예금 상품 조회 중 오류: " + e.getMessage());
            }

            try {
                savingsCount = productService.getAllSavingsProducts().size();
            } catch (Exception e) {
                System.out.println("적금 상품 조회 중 오류: " + e.getMessage());
            }

            try {
                creditLoanCount = productService.getAllCreditLoanProducts().size();
            } catch (Exception e) {
                System.out.println("신용대출 상품 조회 중 오류: " + e.getMessage());
            }

            System.out.println("=== 기본 상품 유형 ===");
            System.out.printf("예금 상품: %d개\n", depositCount);
            System.out.printf("적금 상품: %d개\n", savingsCount);
            System.out.printf("신용대출 상품: %d개\n", creditLoanCount);

            System.out.println("\n=== 추가 상품 유형 ===");
            System.out.println("연금 상품: 개수 확인 중...");
            System.out.println("주택담보대출 상품: 개수 확인 중...");
            System.out.println("전세자금대출 상품: 개수 확인 중...");

            int totalCount = depositCount + savingsCount + creditLoanCount;
            System.out.printf("\n기본 상품 총합: %d개\n", totalCount);
            System.out.println("주의: 연금, 주택담보대출, 전세자금대출 상품은 별도로 확인이 필요합니다.");

        } catch (Exception e) {
            System.err.println("상품 개수 조회 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void runDepositRiskAnalysis(RiskAnalysisService riskAnalysisService) {
        runAnalysis(riskAnalysisService::batchAnalyzeDepositProductsRisk, "예금 상품");
    }

    private static void runSavingsRiskAnalysis(RiskAnalysisService riskAnalysisService) {
        runAnalysis(riskAnalysisService::batchAnalyzeSavingsProductsRisk, "적금 상품");
    }

    private static void runCreditLoanRiskAnalysis(RiskAnalysisService riskAnalysisService) {
        runAnalysis(riskAnalysisService::batchAnalyzeCreditLoanProductsRisk, "신용대출 상품");
    }

    private static void runAnnuityRiskAnalysis(RiskAnalysisService riskAnalysisService) {
        runAnalysis(riskAnalysisService::batchAnalyzeAnnuityProductsRisk, "연금 상품");
    }

    private static void runMortgageLoanRiskAnalysis(RiskAnalysisService riskAnalysisService) {
        runAnalysis(riskAnalysisService::batchAnalyzeMortgageLoanProductsRisk, "주택담보대출 상품");
    }

    private static void runRentHouseLoanRiskAnalysis(RiskAnalysisService riskAnalysisService) {
        runAnalysis(riskAnalysisService::batchAnalyzeRentHouseLoanProductsRisk, "전세자금대출 상품");
    }

    private static void runAnalysis(Runnable analysisTask, String analysisName) {
        System.out.printf("\n=== %s 위험도 분석 실행 ===\n", analysisName);
        try {
            System.out.printf("%s에 대한 위험도 분석을 시작합니다...\n", analysisName);
            long startTime = System.currentTimeMillis();
            analysisTask.run();
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            System.out.printf("%s 위험도 분석이 완료되었습니다.\n", analysisName);
            System.out.printf("소요 시간: %.2f초\n", duration / 1000.0);
        } catch (Exception e) {
            System.err.printf("%s 분석 실행 중 오류 발생: %s\n", analysisName, e.getMessage());
            e.printStackTrace();
        }
    }
}
