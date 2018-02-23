package org.springframework.data.spanner.repository.query;

import java.lang.reflect.Method;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.EvaluationContextProvider;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.spanner.core.SpannerOperations;
import org.springframework.expression.spel.standard.SpelExpressionParser;

public class SpannerQueryLookupStrategy implements QueryLookupStrategy {

  private EvaluationContextProvider evaluationContextProvider;
  private SpannerOperations spannerOperations;

  /**
   * For now I'll take key as always CREATE
   * @param key
   * @param evaluationContextProvider
   * @param spannerOperations
   */
  public SpannerQueryLookupStrategy(Key key, EvaluationContextProvider evaluationContextProvider, SpannerOperations spannerOperations) {
    this.evaluationContextProvider = evaluationContextProvider;
    this.spannerOperations = spannerOperations;
  }

  @Override
  public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata,
      ProjectionFactory factory, NamedQueries namedQueries) {
    QueryMethod queryMethod = new QueryMethod(method, metadata, factory);

    if (namedQueries.hasQuery(queryMethod.getNamedQueryName())) {
      String sql = namedQueries.getQuery(queryMethod.getNamedQueryName());
      return new SpannerSQLQuery(queryMethod, evaluationContextProvider, spannerOperations, new SpelExpressionParser(), sql);
    }


    return new PartTreeSpannerQuery(queryMethod, evaluationContextProvider, spannerOperations);
  }
}
