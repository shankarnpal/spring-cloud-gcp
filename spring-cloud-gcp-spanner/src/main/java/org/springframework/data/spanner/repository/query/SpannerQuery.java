package org.springframework.data.spanner.repository.query;

import java.util.Arrays;
import org.springframework.data.repository.query.EvaluationContextProvider;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.spanner.core.SpannerOperations;

public class SpannerQuery implements RepositoryQuery {

  private final QueryMethod queryMethod;
  private final EvaluationContextProvider evaluationContextProvider;
  private final SpannerOperations spannerOperations;

  public SpannerQuery(QueryMethod queryMethod, EvaluationContextProvider evaluationContextProvider,
      SpannerOperations spannerOperations) {
    this.queryMethod = queryMethod;
    this.evaluationContextProvider = evaluationContextProvider;
    this.spannerOperations = spannerOperations;
  }

  @Override
  public Object execute(Object[] parameters) {
    System.out.println("My query is called with parameters: " + Arrays.toString(parameters));
    System.out.println("My queryMethod is: " + queryMethod);
    
    return "Hello";
  }

  @Override
  public QueryMethod getQueryMethod() {
    return queryMethod;
  }
}
