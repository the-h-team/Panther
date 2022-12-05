package com.github.sanctum.panther.net.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.net.URIBuilder;

class HttpGetterImpl<T> implements HttpGetter<T> {

	protected final String url;
	protected final String subPath;
	protected final Map<String, String> arguments;
	protected final Function<String, T> dataBuilder;
	boolean loaded;
	T rawData;

	public HttpGetterImpl(final String url, final String subPath, final Map<String, String> arguments, final Function<String, T> dataBuilder) {
		this.url = url;
		this.subPath = subPath;
		this.arguments = arguments;
		this.dataBuilder = dataBuilder;
	}

	@Override
	public void load() {
		try {
			CloseableHttpClient httpclient = HttpClients.createDefault();
			//Create a request with selected target
			var request = new HttpGet(url);
			//Set the subpath of the page to our desired document
			if (subPath != null) {
				request.setPath(subPath);
			}
			//Set the URI Arguments
			URI uri;
			try {
				var builder = new URIBuilder(request.getUri());
				arguments.forEach(builder::addParameter);
				uri = builder.build();
			} catch (URISyntaxException e) {
				e.printStackTrace();
				return;
			}
			request.setUri(uri);
			//Try with resources - The connection of the request automatically gets closed when leaving this try block,
			//especially in case of errors
			try (CloseableHttpResponse response1 = httpclient.execute(request)) {
				//Print status code. E.g. 404 NOT FOUND or 200 OK
				System.out.println(response1.getCode() + " " + response1.getReasonPhrase());
				HttpEntity entity1 = response1.getEntity();
				BufferedReader connectionReader = new BufferedReader(new InputStreamReader(entity1.getContent(), StandardCharsets.UTF_8));
				String toParse = connectionReader.lines().collect(Collectors.joining(System.lineSeparator()));

				rawData = dataBuilder.apply(toParse);
				synchronized (this) {
					loaded = true;
				}
				//Obligatory Exception handling since a lot can go wrong when working with network connections
			} catch (IOException e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			System.err.println("Site could not be loaded!");
			e.printStackTrace();
		}
	}

	@Override
	public T getData() {
		validateLoaded();
		return doGet();
	}

	protected T doGet() {
		return rawData;
	}

	@Override
	public T loadAndGet() {
		load();
		return doGet();
	}

	@Override
	public synchronized boolean isLoaded() {
		return loaded;
	}

	protected void validateLoaded() throws IllegalStateException {
		if (!isLoaded()) {
			throw new IllegalStateException("Data is not loaded yet!");
		}
	}
}
