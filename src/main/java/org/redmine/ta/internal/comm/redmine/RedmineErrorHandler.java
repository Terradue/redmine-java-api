package org.redmine.ta.internal.comm.redmine;

import java.util.List;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.redmine.ta.NotAuthorizedException;
import org.redmine.ta.NotFoundException;
import org.redmine.ta.RedmineAuthenticationException;
import org.redmine.ta.RedmineException;
import org.redmine.ta.RedmineFormatException;
import org.redmine.ta.RedmineProcessingException;
import org.redmine.ta.internal.RedmineJSONParser;
import org.redmine.ta.internal.comm.BasicHttpResponse;
import org.redmine.ta.internal.comm.Communicators;
import org.redmine.ta.internal.comm.ContentHandler;

public final class RedmineErrorHandler implements
		ContentHandler<BasicHttpResponse, BasicHttpResponse> {

	@Override
	public BasicHttpResponse processContent(BasicHttpResponse httpResponse)
			throws RedmineException {
		final int responseCode = httpResponse.getResponseCode();
		if (responseCode == HttpStatus.SC_UNAUTHORIZED) {
			throw new RedmineAuthenticationException(
					"Authorization error. Please check if you provided a valid API access key or Login and Password and REST API service is enabled on the server.");
		}
		if (responseCode == HttpStatus.SC_FORBIDDEN) {
			throw new NotAuthorizedException(
					"Forbidden. Please check the user has proper permissions.");
		}
		if (responseCode == HttpStatus.SC_NOT_FOUND) {
			throw new NotFoundException(
					"Server returned '404 not found'. response body:"
							+ getContent(httpResponse));
		}

		if (responseCode == HttpStatus.SC_UNPROCESSABLE_ENTITY) {
			List<String> errors;
			try {
				errors = RedmineJSONParser
						.parseErrors(getContent(httpResponse));
			} catch (JSONException e) {
				throw new RedmineFormatException("Bad redmine error responce",
						e);
			}
			throw new RedmineProcessingException(errors);
		}
		return httpResponse;
	}

	private String getContent(BasicHttpResponse entity) throws RedmineException {
		return Communicators.contentReader().processContent(entity);
	}

}
