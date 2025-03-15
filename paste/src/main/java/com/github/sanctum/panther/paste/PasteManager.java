package com.github.sanctum.panther.paste;

import com.github.sanctum.panther.annotation.Note;
import com.github.sanctum.panther.paste.operative.PasteResponse;
import com.github.sanctum.panther.paste.option.Context;
import com.github.sanctum.panther.paste.option.Expiration;
import com.github.sanctum.panther.paste.option.Visibility;
import com.github.sanctum.panther.paste.type.HasteOptions;
import com.github.sanctum.panther.paste.type.Hastebin;
import com.github.sanctum.panther.paste.type.PasteOptions;
import com.github.sanctum.panther.paste.type.Pastebin;
import com.github.sanctum.panther.paste.type.PastebinUser;
import com.github.sanctum.panther.recursive.Service;
import com.github.sanctum.panther.recursive.ServiceFactory;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An object manager for paste bin and haste bin api.
 */
public interface PasteManager extends Service {

	/**
	 * Gets the paste manager instance.
	 *
	 * @return the paste manager instance
	 * @implNote This is an overridable provision! An alternative
	 * implementation may be registered by you or another assembly.
	 */
	static PasteManager getInstance() {
		// skip using an optional, fast stop if a provider is already cached.
		PasteManager manager = ServiceFactory.getInstance().getService(PasteManager.class);
		if (manager != null) {
			return manager;
		}
		PasteManager instance = new PasteManager() {
			final Obligation obligation = () -> "To provide easy access to both hastebin and pastebin web api.";

			@Override
			public @NotNull Obligation getObligation() {
				return obligation;
			}

			@Override
			public @NotNull Hastebin newHaste() {
				return new Hastebin() {

					private final HasteOptions options;

					{
						this.options = HasteOptions.empty();
					}

					@Override
					public @NotNull String getApiKey() {
						return "NA";
					}

					@Override
					public @NotNull HasteOptions getOptions() {
						return options;
					}

					@Override
					public PasteResponse read(String id) {
						return new PasteResponse() {
							@Override
							public String get() {
								return getAll()[0];
							}

							@Override
							public String[] getAll() {
								String response = "Unable to receive proper response.";
								try {
									String requestURL = id.contains("http") && !id.contains("raw") ? id.replace("hastebin.skyra.pw", "hastebin.skyra.pw/raw") : "https://hastebin.skyra.pw/raw/" + id;
									URL url = new URL(requestURL);
									HttpURLConnection conn = (HttpURLConnection) url.openConnection();
									conn.setDoInput(true);
									conn.setInstanceFollowRedirects(false);
									conn.setRequestMethod("GET");
									conn.setRequestProperty("User-Agent", "Hastebin Java Api");
									conn.setUseCaches(false);
									return new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)).lines().toArray(String[]::new);
								} catch (Exception ex) {
									ex.printStackTrace();
								}
								return response.split(" ");
							}

						};
					}

					@Override
					public PasteResponse write(String... info) {
						return () -> {
							String response = "Unable to receive proper response.";
							StringBuilder builder = new StringBuilder();
							for (String s : info) {
								builder.append(s).append("\n");
							}
							try {
								byte[] postData = builder.toString().getBytes(StandardCharsets.UTF_8);
								int postDataLength = postData.length;

								URL url = new URL("https://hastebin.skyra.pw/documents");
								HttpURLConnection conn = (HttpURLConnection) url.openConnection();
								conn.setDoOutput(true);
								conn.setInstanceFollowRedirects(false);
								conn.setRequestMethod("POST");
								conn.setRequestProperty("User-Agent", "Hastebin Java Api");
								conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
								conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
								conn.setUseCaches(false);
								DataOutputStream wr;
								try {
									wr = new DataOutputStream(conn.getOutputStream());
									wr.write(postData);
									wr.flush();
									BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
									response = reader.readLine();
								} catch (IOException e) {
									e.printStackTrace();
								}

								if (response.contains("\"key\"")) {
									response = response.substring(response.indexOf(":") + 2, response.length() - 2);

									String postURL = getOptions().isRaw() ? "https://hastebin.skyra.pw/raw/" : "https://hastebin.skyra.pw/";
									response = postURL + response;
								}
							} catch (Exception ex) {
								ex.printStackTrace();
							}
							return response;
						};
					}

					@Override
					public PasteResponse write(Collection<? extends CharSequence> info) {
						return () -> {
							String response = "Unable to receive proper response.";
							StringBuilder builder = new StringBuilder();
							for (CharSequence s : info) {
								builder.append("*").append(" ").append(s).append("\n");
							}
							try {
								byte[] postData = builder.toString().getBytes(StandardCharsets.UTF_8);
								int postDataLength = postData.length;

								String requestURL = "https://hastebin.skyra.pw/documents";
								URL url = new URL(requestURL);
								HttpURLConnection conn = (HttpURLConnection) url.openConnection();
								conn.setDoOutput(true);
								conn.setInstanceFollowRedirects(false);
								conn.setRequestMethod("POST");
								conn.setRequestProperty("User-Agent", "Hastebin Java Api");
								conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
								conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
								conn.setUseCaches(false);
								DataOutputStream wr;
								try {
									wr = new DataOutputStream(conn.getOutputStream());
									wr.write(postData);
									wr.flush();
									BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
									response = reader.readLine();
								} catch (IOException e) {
									e.printStackTrace();
								}

								if (response.contains("\"key\"")) {
									response = response.substring(response.indexOf(":") + 2, response.length() - 2);

									String postURL = getOptions().isRaw() ? "https://hastebin.skyra.pw/raw/" : "https://hastebin.skyra.pw/";
									response = postURL + response;
								}
							} catch (Exception ex) {
								ex.printStackTrace();
							}
							return response;
						};
					}
				};
			}

			@Override
			public @NotNull Pastebin newPaste(@NotNull String apiKey) {
				return new Pastebin() {

					private final PasteOptions options;

					{
						this.options = new PasteOptions() {

							private Context language = () -> "text";
							private Context folder;
							private Expiration expiration = Expiration.TEN_MINUTE;
							private Visibility visibility = Visibility.PUBLIC;

							@Override
							public @NotNull Context getLanguage() {
								return language;
							}

							@Override
							public @Nullable Context getFolder() {
								return folder;
							}

							@Override
							public @NotNull Expiration getExpiration() {
								return expiration;
							}

							@Override
							public @NotNull Visibility getVisibility() {
								return visibility;
							}

							@Override
							public void setFolder(@NotNull Context context) {
								this.folder = context;
							}

							@Override
							public void setLanguage(@NotNull Context context) {
								this.language = context;
							}

							@Override
							public void setExpiration(@NotNull Expiration expiration) {
								this.expiration = expiration;
							}

							@Override
							public void setVisibility(@NotNull Visibility visibility) {
								this.visibility = visibility;
							}
						};
					}

					@Override
					public PasteResponse read(String id) {
						String response = "Unable to receive proper response.";
						try {
							URL url = new URL("https://pastebin.com/raw/");
							URLConnection con = url.openConnection();
							HttpURLConnection http = (HttpURLConnection) con;
							http.setRequestMethod("POST");
							http.setDoOutput(true);
							http.setDoInput(true);

							Map<String, String> arguments = new HashMap<>();
							arguments.put("paste_key", id);

							StringJoiner sj = new StringJoiner("&");
							for (Map.Entry<String, String> entry : arguments.entrySet()) {
								sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "="
										+ URLEncoder.encode(entry.getValue(), "UTF-8"));
							}

							http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
							http.connect();
							OutputStream os = http.getOutputStream();
							os.write(sj.toString().getBytes(StandardCharsets.UTF_8));
							InputStream is = http.getInputStream();
							response = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));

						} catch (IOException urlException) {
							urlException.printStackTrace();
						}
						String finalResponse = response;
						return () -> finalResponse;
					}

					@Override
					public @NotNull String getApiKey() {
						return apiKey;
					}

					@Override
					public @NotNull PasteOptions getOptions() {
						return options;
					}

					@Override
					public @Nullable PastebinUser login(String username, String password) {
						try {
							URL url = new URL("https://pastebin.com/api/api_login.php");
							URLConnection con = url.openConnection();
							HttpURLConnection http = (HttpURLConnection) con;
							http.setRequestMethod("POST");
							http.setDoOutput(true);
							http.setDoInput(true);

							Map<String, String> arguments = new HashMap<>();
							arguments.put("api_dev_key", getApiKey());
							arguments.put("api_user_name", username);
							arguments.put("api_user_password", password);

							StringJoiner sj = new StringJoiner("&");
							for (Map.Entry<String, String> entry : arguments.entrySet()) {
								sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "="
										+ URLEncoder.encode(entry.getValue(), "UTF-8"));
							}

							http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
							http.connect();
							OutputStream os = http.getOutputStream();
							os.write(sj.toString().getBytes(StandardCharsets.UTF_8));
							InputStream is = http.getInputStream();
							final String result = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
							if (result == null || result.isEmpty()) return null;
							return new PastebinUser() {

								private final String userId;
								private final PasteOptions options;

								{
									this.userId = result;
									this.options = new PasteOptions() {

										private Context language = () -> "text";
										private Context folder;
										private Expiration expiration = Expiration.NEVER;
										private Visibility visibility = Visibility.PUBLIC;

										@Override
										public @NotNull Context getLanguage() {
											return language;
										}

										@Override
										public @Nullable Context getFolder() {
											return folder;
										}

										@Override
										public @NotNull Expiration getExpiration() {
											return expiration;
										}

										@Override
										public @NotNull Visibility getVisibility() {
											return visibility;
										}

										@Override
										public void setFolder(@NotNull Context context) {
											this.folder = context;
										}

										@Override
										public void setLanguage(@NotNull Context context) {
											this.language = context;
										}

										@Override
										public void setExpiration(@NotNull Expiration expiration) {
											this.expiration = expiration;
										}

										@Override
										public void setVisibility(@NotNull Visibility visibility) {
											this.visibility = visibility;
										}
									};
								}

								@Override
								public @NotNull String getId() {
									return userId;
								}

								@Override
								public boolean remove(@NotNull String id) {
									try {
										URL url = new URL("https://pastebin.com/api/api_post.php");
										URLConnection con = url.openConnection();
										HttpURLConnection http = (HttpURLConnection) con;
										http.setRequestMethod("POST");
										http.setDoOutput(true);
										http.setDoInput(true);

										Map<String, String> arguments = new HashMap<>();
										arguments.put("api_dev_key", getApiKey());
										arguments.put("api_user_key", getId());
										arguments.put("api_option", "delete");
										arguments.put("api_paste_key", id);


										StringJoiner sj = new StringJoiner("&");
										for (Map.Entry<String, String> entry : arguments.entrySet()) {
											sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "="
													+ URLEncoder.encode(entry.getValue(), "UTF-8"));
										}

										byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);

										http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
										http.connect();

										OutputStream os = http.getOutputStream();
										os.write(out);
										return true;
									} catch (IOException urlException) {
										urlException.printStackTrace();
										return false;
									}
								}

								@Override
								public @NotNull String getApiKey() {
									return apiKey;
								}

								@Override
								public @NotNull PasteOptions getOptions() {
									return options;
								}

								@Override
								public PasteResponse read(String id) {
									return () -> {
										String response = "Unable to receive proper response.";
										try {
											URL url = new URL("https://pastebin.com/api/api_post.php");
											URLConnection con = url.openConnection();
											HttpURLConnection http = (HttpURLConnection) con;
											http.setRequestMethod("POST");
											http.setDoOutput(true);
											http.setDoInput(true);

											Map<String, String> arguments = new HashMap<>();
											arguments.put("api_dev_key", getApiKey());
											arguments.put("api_user_key", getId());
											arguments.put("api_option", "show_paste");
											arguments.put("api_paste_key", id);


											StringJoiner sj = new StringJoiner("&");
											for (Map.Entry<String, String> entry : arguments.entrySet()) {
												sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "="
														+ URLEncoder.encode(entry.getValue(), "UTF-8"));
											}

											byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);

											http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
											http.connect();

											OutputStream os = http.getOutputStream();
											os.write(out);
											InputStream is = http.getInputStream();
											return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
										} catch (IOException urlException) {
											urlException.printStackTrace();
										}
										return response;
									};
								}

								@Override
								public PasteResponse write(String... info) {
									return () -> {
										String response = "Unable to receive proper response.";
										StringBuilder builder = new StringBuilder();
										for (String s : info) {
											builder.append(s).append("\n");
										}
										try {
											URL url = new URL("https://pastebin.com/api/api_post.php");
											URLConnection con = url.openConnection();
											HttpURLConnection http = (HttpURLConnection) con;
											http.setRequestMethod("POST");
											http.setDoOutput(true);
											http.setDoInput(true);

											Map<String, String> arguments = new HashMap<>();
											arguments.put("api_dev_key", getApiKey());
											arguments.put("api_user_key", getId());
											arguments.put("api_option", "paste");
											arguments.put("api_paste_code", builder.toString());
											arguments.put("api_paste_private", getOptions().getVisibility().toString());
											arguments.put("api_paste_expire_date", getOptions().getExpiration().toString());
											arguments.put("api_paste_format", getOptions().getLanguage().get().toLowerCase(Locale.ROOT));
											if (getOptions().getFolder() != null) {
												arguments.put("api_folder_key", getOptions().getFolder().get());
											}

											StringJoiner sj = new StringJoiner("&");
											for (Map.Entry<String, String> entry : arguments.entrySet()) {
												sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "="
														+ URLEncoder.encode(entry.getValue(), "UTF-8"));
											}

											byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);

											http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
											http.connect();

											OutputStream os = http.getOutputStream();
											os.write(out);
											InputStream is = http.getInputStream();
											return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
										} catch (IOException urlException) {
											urlException.printStackTrace();
										}
										return response;
									};
								}

								@Override
								public PasteResponse write(Collection<? extends CharSequence> info) {
									return () -> {
										String response = "Unable to receive proper response.";
										StringBuilder builder = new StringBuilder();
										for (CharSequence s : info) {
											builder.append(s).append("\n");
										}
										try {
											URL url = new URL("https://pastebin.com/api/api_post.php");
											URLConnection con = url.openConnection();
											HttpURLConnection http = (HttpURLConnection) con;
											http.setRequestMethod("POST");
											http.setDoOutput(true);
											http.setDoInput(true);

											Map<String, String> arguments = new HashMap<>();
											arguments.put("api_dev_key", getApiKey());
											arguments.put("api_user_key", getId());
											arguments.put("api_option", "paste");
											arguments.put("api_paste_code", builder.toString());
											arguments.put("api_paste_private", getOptions().getVisibility().toString());
											arguments.put("api_paste_expire_date", getOptions().getExpiration().toString());
											arguments.put("api_paste_format", getOptions().getLanguage().get().toLowerCase(Locale.ROOT));
											if (getOptions().getFolder() != null) {
												arguments.put("api_folder_key", getOptions().getFolder().get());
											}

											StringJoiner sj = new StringJoiner("&");
											for (Map.Entry<String, String> entry : arguments.entrySet()) {
												sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "="
														+ URLEncoder.encode(entry.getValue(), "UTF-8"));
											}

											byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);

											http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
											http.connect();

											OutputStream os = http.getOutputStream();
											os.write(out);
											InputStream is = http.getInputStream();
											return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
										} catch (IOException urlException) {
											urlException.printStackTrace();
										}
										return response;
									};
								}
							};
						} catch (IOException urlException) {
							urlException.printStackTrace();
							return null;
						}
					}

					@Override
					public PasteResponse write(String... info) {
						return () -> {
							String response = "Unable to receive proper response.";
							StringBuilder builder = new StringBuilder();
							for (String s : info) {
								builder.append(s).append("\n");
							}
							try {
								URL url = new URL("https://pastebin.com/api/api_post.php");
								URLConnection con = url.openConnection();
								HttpURLConnection http = (HttpURLConnection) con;
								http.setRequestMethod("POST");
								http.setDoOutput(true);
								http.setDoInput(true);

								Map<String, String> arguments = new HashMap<>();
								arguments.put("api_dev_key", getApiKey());
								arguments.put("api_option", "paste");
								arguments.put("api_paste_code", builder.toString());
								arguments.put("api_paste_private", getOptions().getVisibility().toString());
								arguments.put("api_paste_expire_date", getOptions().getExpiration().toString());
								arguments.put("api_paste_format", getOptions().getLanguage().get().toLowerCase(Locale.ROOT));
								if (getOptions().getFolder() != null) {
									arguments.put("api_folder_key", getOptions().getFolder().get());
								}

								StringJoiner sj = new StringJoiner("&");
								for (Map.Entry<String, String> entry : arguments.entrySet()) {
									sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "="
											+ URLEncoder.encode(entry.getValue(), "UTF-8"));
								}

								byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);

								http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
								http.connect();

								OutputStream os = http.getOutputStream();
								os.write(out);
								InputStream is = http.getInputStream();
								return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
							} catch (IOException urlException) {
								urlException.printStackTrace();
							}
							return response;
						};
					}

					@Override
					public PasteResponse write(Collection<? extends CharSequence> info) {
						return () -> {
							String response = "Unable to receive proper response.";
							StringBuilder builder = new StringBuilder();
							for (CharSequence s : info) {
								builder.append(s).append("\n");
							}
							try {
								URL url = new URL("https://pastebin.com/api/api_post.php");
								URLConnection con = url.openConnection();
								HttpURLConnection http = (HttpURLConnection) con;
								http.setRequestMethod("POST");
								http.setDoOutput(true);
								http.setDoInput(true);

								Map<String, String> arguments = new HashMap<>();
								arguments.put("api_dev_key", getApiKey());
								arguments.put("api_option", "paste");
								arguments.put("api_paste_code", builder.toString());
								arguments.put("api_paste_private", getOptions().getVisibility().toString());
								arguments.put("api_paste_expire_date", getOptions().getExpiration().toString());
								arguments.put("api_paste_format", getOptions().getLanguage().get().toLowerCase(Locale.ROOT));
								if (getOptions().getFolder() != null) {
									arguments.put("api_folder_key", getOptions().getFolder().get());
								}

								StringJoiner sj = new StringJoiner("&");
								for (Map.Entry<String, String> entry : arguments.entrySet()) {
									sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "="
											+ URLEncoder.encode(entry.getValue(), "UTF-8"));
								}

								byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);

								http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
								http.connect();

								OutputStream os = http.getOutputStream();
								os.write(out);
								InputStream is = http.getInputStream();
								return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
							} catch (IOException urlException) {
								urlException.printStackTrace();
							}
							return response;
						};
					}
				};
			}
		};
		ServiceFactory.getInstance().newLoader(PasteManager.class).supply(instance);
		return instance;
	}

	@NotNull Hastebin newHaste();

	// is this printed at runtime? if not FIXME remove/convert to doc
	@Note("This method requires a unique api key! Make sure you have an account registered")
	@NotNull Pastebin newPaste(@NotNull String apiKey);

}
