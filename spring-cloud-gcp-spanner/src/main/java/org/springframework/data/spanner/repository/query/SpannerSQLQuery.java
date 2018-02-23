package org.springframework.data.spanner.repository.query;

import java.util.Arrays;

import org.springframework.data.repository.query.EvaluationContextProvider;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.spanner.core.SpannerOperations;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;

public class SpannerSQLQuery implements RepositoryQuery {

  private final QueryMethod queryMethod;
  private final EvaluationContextProvider evaluationContextProvider;
  private final SpannerOperations spannerOperations;
  private SpelExpressionParser spelExpressionParser;
  private final String sql;

  public SpannerSQLQuery(QueryMethod queryMethod, EvaluationContextProvider evaluationContextProvider,
      SpannerOperations spannerOperations, SpelExpressionParser spelExpressionParser, String sql) {
    this.queryMethod = queryMethod;
    this.evaluationContextProvider = evaluationContextProvider;
    this.spannerOperations = spannerOperations;
    this.spelExpressionParser = spelExpressionParser;
    this.sql = sql;
  }

  @Override
  public Object execute(Object[] parameters) {
    EvaluationContext evaluationContext = evaluationContextProvider
        .getEvaluationContext(queryMethod.getParameters(), parameters);

    System.out.println("My SQL query is called with parameters: " + Arrays.toString(parameters));
    System.out.println("My SQL query is : " + sql);
    System.out.println("My SQL queryMethod is: " + queryMethod);


    return "Hello";
  }

  @Override
  public QueryMethod getQueryMethod() {
    return queryMethod;
  }
}
