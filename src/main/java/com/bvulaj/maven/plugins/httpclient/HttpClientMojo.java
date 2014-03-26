/**
 * 
 */
package com.bvulaj.maven.plugins.httpclient;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * @author Brandon Vulaj
 * 
 */
@Mojo(name = "post")
public abstract class HttpClientMojo extends AbstractMojo {

	@Parameter(property = "httpclient.url", required = true)
	private URL url;

	@Parameter(property = "httpclient.params")
	private Properties requestParams;

	public void execute() throws MojoExecutionException, MojoFailureException {
		Form form = Form.form();
		for (Entry<Object, Object> param : requestParams.entrySet()) {
			form.add((String) param.getKey(), (String) param.getValue());
		}
		try {
			Request.Post(url.toURI()).bodyForm(form.build()).execute().handleResponse(new ResponseHandler<Void>() {

				public Void handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
					StatusLine line = response.getStatusLine();
					if (line.getStatusCode() != 200) {
						throw new HttpResponseException(line.getStatusCode(), line.getReasonPhrase());
					}
					getLog().info(line.getStatusCode() + " / " + line.getReasonPhrase());
					if (response.getEntity() != null) {
						StringWriter writer = new StringWriter();
						IOUtils.copy(response.getEntity().getContent(), writer);
						getLog().info(writer.toString());
					}
					return null;
				}
			});
		} catch (URISyntaxException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		} catch (ClientProtocolException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		} catch (IOException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}
}
