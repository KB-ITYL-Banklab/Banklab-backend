package com.banklab.product;

import com.banklab.config.MailConfig;
import com.banklab.config.RedisConfig;
import com.banklab.config.RootConfig;
import com.banklab.product.batch.scheduler.ProductScheduler;
import com.banklab.security.config.SecurityConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class SchedulerRunner {
    public static void main(String[] args) {
        System.out.println("=== 스케줄러 시작 ===");

        try {
            // Spring 컨텍스트 로드
            AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext(RootConfig.class, SecurityConfig.class, RedisConfig.class, MailConfig.class);

            // ProductScheduler 가져오기
            ProductScheduler scheduler = context.getBean(ProductScheduler.class);

            System.out.println("스케줄러가 시작되었습니다.");
            System.out.println("예정된 스케줄:");
            System.out.println("- 예금 상품: 매일 02:00");
            System.out.println("- 적금 상품: 매일 02:05");
            System.out.println("- 신용대출 상품: 매일 02:10");
            System.out.println("- 연금 저축 상품: 매일 02:15");
            System.out.println("- 주택 담보 대출 상품: 매일 02:20");
            System.out.println();
            System.out.println("테스트용 수동 실행을 원하면 다음 중 하나를 선택하세요:");
            System.out.println("1. 예금 배치 실행");
            System.out.println("2. 적금 배치 실행");
            System.out.println("3. 신용대출 배치 실행");
            System.out.println("4. 연금 저축 배치 실행");
            System.out.println("5. 주택 담보 대출 배치 실행");
            System.out.println("6. 전체 배치 실행");

            // 사용자 입력 대기
            java.util.Scanner scanner = new java.util.Scanner(System.in);
            System.out.print("선택 (1-6, 또는 Enter를 눌러 스케줄러만 실행): ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    System.out.println("예금 배치를 수동 실행합니다...");
                    scheduler.runDepositBatch();
                    break;
                case "2":
                    System.out.println("적금 배치를 수동 실행합니다...");
                    scheduler.runSavingsBatch();
                    break;
                case "3":
                    System.out.println("신용대출 배치를 수동 실행합니다...");
                    scheduler.runCreditLoanBatch();
                    break;
                case "4":
                    System.out.println("연금 저축 배치를 수동 실행합니다...");
                    scheduler.runAnnuityBatch();
                    break;
                case "5":
                    System.out.println("주택 담보 대출 배치를 수동 실행합니다...");
                    scheduler.runMortgageLoanBatch();
                    break;
                case "6":
                    System.out.println("전체 배치를 순차 실행합니다...");
                    scheduler.runDepositBatch();
                    Thread.sleep(3000);
                    scheduler.runSavingsBatch();
                    Thread.sleep(3000);
                    scheduler.runCreditLoanBatch();
                    Thread.sleep(3000);
                    scheduler.runAnnuityBatch();
                    Thread.sleep(3000);
                    scheduler.runMortgageLoanBatch();
                    break;
                default:
                    System.out.println("스케줄러만 실행됩니다. 프로그램을 종료하려면 Ctrl+C를 누르세요.");
                    // 무한 대기 (스케줄러가 백그라운드에서 실행됨)
                    while (true) {
                        Thread.sleep(10000);
                    }
            }

            System.out.println("실행 완료!");

            // 컨텍스트 종료
            context.close();

        } catch (Exception e) {
            System.err.println("스케줄러 실행 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("=== 스케줄러 종료 ===");
    }
}
