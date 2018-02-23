package org.springframework.data.spanner.repository.query;

import java.util.Arrays;

import org.springframework.data.repository.query.EvaluationContextProvider;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.ResultProcessor;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.data.spanner.core.SpannerOperations;
import org.springframework.expression.EvaluationContext;

public class PartTreeSpannerQuery implements RepositoryQuery {

	private final QueryMethod queryMethod;

	private final EvaluationContextProvider evaluationContextProvider;

	private final SpannerOperations spannerOperations;

	private final ResultProcessor processor;

	private final PartTree tree;

	public PartTreeSpannerQuery(QueryMethod queryMethod, EvaluationContextProvider evaluationContextProvider,
			SpannerOperations spannerOperations) {
		this.queryMethod = queryMethod;
		this.processor = queryMethod.getResultProcessor();
		this.tree = new PartTree(queryMethod.getName(), processor.getReturnedType().getDomainType());
		this.evaluationContextProvider = evaluationContextProvider;
		this.spannerOperations = spannerOperations;
	}

	@Override
	public Object execute(Object[] parameters) {

		System.out.println("My query is called with parameters: " + Arrays.toString(parameters));
		System.out.println("My queryMethod is: " + queryMethod);

		System.out.println(tree);
		queryMethod.getNamedQueryName();
		return spannerOperations.find();
	}

	@Override
	public QueryMethod getQueryMethod() {
		return queryMethod;
	}
}
