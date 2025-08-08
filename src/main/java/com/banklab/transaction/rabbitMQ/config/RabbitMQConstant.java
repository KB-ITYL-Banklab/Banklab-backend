package com.banklab.transaction.rabbitMQ.config;

public class RabbitMQConstant {
    public static final String EXCHANGE = "transaction.exchange";

    public static final String QUEUE_TRANSACTION_FETCH="transaction.fetch";
    public static final String QUEUE_TRANSACTION_SAVE="transaction.save";
    public static final String QUEUE_CATEGORIZE_INTERNAL="transaction.categorize";
    public static final String QUEUE_CATEGORIZE_EXTERNAL="transaction.categorize.gemini";
    public static final String QUEUE_CATEGORY_SAVE="transaction.category.save";
    public static final String QUEUE_SUMMARY_SAVE="transaction.summary.save";

    public static final String ROUTING_TRANSACTION_FETCH = "transaction.fetch";
    public static final String ROUTING_TRANSACTION_SAVE = "transaction.save";
    public static final String ROUTING_CATEGORIZE_INTERNAL = "transaction.categorize.internal";
    public static final String ROUTING_CATEGORIZE_EXTERNAL = "transaction.categorize.external";
    public static final String ROUTING_CATEGORY_SAVE = "transaction.category.save";
    public static final String ROUTING_SUMMARY_SAVE = "transaction.summary.save";
}
