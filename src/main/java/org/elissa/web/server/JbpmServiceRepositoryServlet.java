package org.elissa.web.server;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.drools.core.util.ConfFileUtils;
import org.elissa.web.profile.IDiagramProfile;
import org.elissa.web.profile.IDiagramProfileService;
import org.elissa.web.profile.impl.ExternalInfo;
import org.elissa.web.profile.impl.ProfileServiceImpl;
import org.jbpm.process.workitem.WorkDefinitionImpl;
import org.jbpm.process.workitem.WorkItemRepository;
import org.json.JSONObject;

import sun.misc.BASE64Encoder;


/**
 * Servlet for interaction with the jbpm service repository.
 * @author tsurdilo
 */
public class JbpmServiceRepositoryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger _logger = Logger
			.getLogger(JbpmServiceRepositoryServlet.class);
	private static final String displayRepoContent = "display";
	private static final String installRepoContent = "install";

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String uuid = req.getParameter("uuid");
		String profileName = req.getParameter("profile");
		String action = req.getParameter("action");
		String assetsToInstall = req.getParameter("asset");
		String categoryToInstall = req.getParameter("category");

		IDiagramProfile profile = getProfile(req, profileName);

		Map<String, WorkDefinitionImpl> workitemsFromRepo = WorkItemRepository.getWorkDefinitions(profile.getServiceRepositoryLocation());
		if(action != null && action.equalsIgnoreCase(displayRepoContent)) {
			if(workitemsFromRepo != null && workitemsFromRepo.size() > 0) {
				Map<String, List<String>> retMap = new HashMap<String, List<String>>();
				for(String key : workitemsFromRepo.keySet()) {
					WorkDefinitionImpl wd = workitemsFromRepo.get(key);
					List<String> keyList = new ArrayList<String>();
					keyList.add(wd.getName() == null ? "" : wd.getName());
					keyList.add(wd.getDisplayName() == null ? "" : wd.getDisplayName());
					keyList.add(profile.getServiceRepositoryLocation() + "/" + wd.getName() + "/" + wd.getIcon());
					keyList.add(wd.getCategory() == null ? "" : wd.getCategory());
					keyList.add(wd.getExplanationText() == null ? "" : wd.getExplanationText());
					keyList.add(profile.getServiceRepositoryLocation() + "/" + wd.getName() + "/" + wd.getDocumentation());
					StringBuffer bn = new StringBuffer();
					if(wd.getParameterNames() != null) {
						String delim = "";
					    for (String name : wd.getParameterNames()) {
					        bn.append(delim).append(name);
					        delim = ",";
					    }
					}
					keyList.add(bn.toString());
					StringBuffer br = new StringBuffer();
					if(wd.getResultNames() != null) {
						String delim = "";
					    for (String resName : wd.getResultNames()) {
					        br.append(delim).append(resName);
					        delim = ",";
					    }
					}
					keyList.add(br.toString());
					retMap.put(key, keyList);
				}
				JSONObject jsonObject = new JSONObject();
				for (Entry<String,List<String>> retMapKey : retMap.entrySet()) {
					try {
						jsonObject.put(retMapKey.getKey(), retMapKey.getValue());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				resp.setCharacterEncoding("UTF-8");
				resp.setContentType("application/json");
				resp.getWriter().write(jsonObject.toString());
			} else {
				_logger.error("Invalid or empty service repository.");
			}
		} else if(action != null && action.equalsIgnoreCase(installRepoContent)) {
			resp.setCharacterEncoding("UTF-8");
			resp.setContentType("application/json");
			if(workitemsFromRepo != null && workitemsFromRepo.size() > 0) {
				boolean gotPackage = false;
				String pkg = "";
				for(String key : workitemsFromRepo.keySet()) {
					if(key.equals(assetsToInstall) && categoryToInstall.equals(workitemsFromRepo.get(key).getCategory())) {
						String workitemDefinitionURL = profile.getServiceRepositoryLocation() + "/" + workitemsFromRepo.get(key).getName() + "/" + workitemsFromRepo.get(key).getName() + ".wid";
						String iconFileURL = profile.getServiceRepositoryLocation() + "/" + workitemsFromRepo.get(key).getName() + "/" + workitemsFromRepo.get(key).getIcon();
						String workItemDefinitionContent = ConfFileUtils.URLContentsToString(new URL(workitemDefinitionURL));
						String iconName = workitemsFromRepo.get(key).getIcon();
						String widName = workitemsFromRepo.get(key).getName();
						byte[] iconContent = null;
						try {
							iconContent = getImageBytes(new URL(iconFileURL)
							.openStream());
						} catch (Exception e1) {
							_logger.error("Could not read icon image: " + e1.getMessage());
						}
						// install wid and icon to guvnor
						List<String> packageNames = findPackages(uuid, profile);
						for(String nextPackage : packageNames) {
							String packageAssetURL = ExternalInfo.getExternalProtocol(profile) + "://" + ExternalInfo.getExternalHost(profile) +
									"/" + profile.getExternalLoadURLSubdomain().substring(0, profile.getExternalLoadURLSubdomain().indexOf("/")) +
									"/rest/packages/" + nextPackage + "/assets/";
							try {
								XMLInputFactory factory = XMLInputFactory.newInstance();
								XMLStreamReader reader = factory.createXMLStreamReader(getInputStreamForURL(packageAssetURL, profile));

								while (reader.hasNext()) {
									int next = reader.next();
									if (next == XMLStreamReader.START_ELEMENT) {
										if ("uuid".equals(reader.getLocalName())) {
											String eleText = reader.getElementText();
											if(uuid.equals(eleText)) {
												pkg = nextPackage;
												gotPackage = true;
											}
										}
									}
								}
							} catch (Exception e) {
								// we dont want to barf..just log that error happened
								_logger.error(e.getMessage());
							} 
							if(gotPackage) {
								String widURL = ExternalInfo.getExternalProtocol(profile)
						                + "://"
						                + ExternalInfo.getExternalHost(profile)
						                + "/"
						                + profile.getExternalLoadURLSubdomain().substring(0,
						                        profile.getExternalLoadURLSubdomain().indexOf("/"))
						                + "/rest/packages/" + pkg + "/assets/" + widName + ".wid";
								String iconURL = ExternalInfo.getExternalProtocol(profile)
						                + "://"
						                + ExternalInfo.getExternalHost(profile)
						                + "/"
						                + profile.getExternalLoadURLSubdomain().substring(0,
						                        profile.getExternalLoadURLSubdomain().indexOf("/"))
						                + "/rest/packages/" + pkg + "/assets/" + iconName;
								
								String packageAssetsURL = ExternalInfo.getExternalProtocol(profile)
					                    + "://"
					                    + ExternalInfo.getExternalHost(profile)
					                    + "/"
					                    + profile.getExternalLoadURLSubdomain().substring(0,
					                            profile.getExternalLoadURLSubdomain().indexOf("/"))
					                    + "/rest/packages/" + pkg + "/assets/";
								
								
								// check if the wid already exists
								URL checkWidURL = new URL(widURL);
								HttpURLConnection checkWidConnection = (HttpURLConnection) checkWidURL
								        .openConnection();
								applyAuth(profile, checkWidConnection);
								checkWidConnection.setRequestMethod("GET");
								checkWidConnection
								        .setRequestProperty("Accept", "application/atom+xml");
								checkWidConnection.connect();
								_logger.info("check wid connection response code: " + checkWidConnection.getResponseCode());
								if (checkWidConnection.getResponseCode() == 200) {
									URL deleteAssetURL = new URL(widURL);
								    HttpURLConnection deleteConnection = (HttpURLConnection) deleteAssetURL
								            .openConnection();
								    applyAuth(profile, deleteConnection);
								    deleteConnection.setRequestMethod("DELETE");
								    deleteConnection.connect();
								    _logger.info("delete wid response code: " + deleteConnection.getResponseCode());
								}
								
								// check if icon already exists
								URL checkIconURL = new URL(iconURL);
								HttpURLConnection checkIconConnection = (HttpURLConnection) checkIconURL
								        .openConnection();
								applyAuth(profile, checkIconConnection);
								checkIconConnection.setRequestMethod("GET");
								checkIconConnection
								        .setRequestProperty("Accept", "application/atom+xml");
								checkIconConnection.connect();
								_logger.info("check icon connection response code: " + checkIconConnection.getResponseCode());
								if (checkIconConnection.getResponseCode() == 200) {
								    URL deleteAssetURL = new URL(iconURL);
								    HttpURLConnection deleteConnection = (HttpURLConnection) deleteAssetURL
								            .openConnection();
								    applyAuth(profile, deleteConnection);
								    deleteConnection.setRequestMethod("DELETE");
								    deleteConnection.connect();
								    _logger.info("delete icon response code: " + deleteConnection.getResponseCode());
								}
								
								// replace the icon value of the workitem config to include the guvnor rest url 
								workItemDefinitionContent = workItemDefinitionContent.replaceAll( "(\"icon\"\\s*\\:\\s*\")(.*?)(\")", "$1"+ ( packageAssetsURL + iconName.substring(0, iconName.indexOf("."))  +"/binary" ) + "$3" );
								// write to guvnor
								URL createWidURL = new URL(packageAssetsURL);
					            HttpURLConnection createWidConnection = (HttpURLConnection) createWidURL
					                    .openConnection();
					            applyAuth(profile, createWidConnection);
					            createWidConnection.setRequestMethod("POST");
					            createWidConnection.setRequestProperty("Content-Type",
					                    "application/octet-stream");
					            createWidConnection.setRequestProperty("Accept",
					                    "application/atom+xml");
					            createWidConnection.setRequestProperty("Slug", widName + ".wid");
					            createWidConnection.setDoOutput(true);
					            createWidConnection.getOutputStream().write(workItemDefinitionContent.getBytes("UTF-8"));
					            createWidConnection.connect();
					            System.out.println("created wid configuration:" + createWidConnection.getResponseCode());

					            URL createIconURL = new URL(packageAssetsURL);
					            HttpURLConnection createIconConnection = (HttpURLConnection) createIconURL
					                    .openConnection();
					            applyAuth(profile, createIconConnection);
					            createIconConnection.setRequestMethod("POST");
					            createIconConnection.setRequestProperty("Content-Type",
					                    "application/octet-stream");
					            createIconConnection.setRequestProperty("Accept",
					                    "application/atom+xml");
					            createIconConnection.setRequestProperty("Slug", iconName);
					            createIconConnection.setDoOutput(true);
					            createIconConnection.getOutputStream().write(iconContent);
					            createIconConnection.connect();
					            _logger.info("icon creation response code: " + createIconConnection.getResponseCode());
					            System.out.println("created icon:" + createIconConnection.getResponseCode());
								
								break;
							} else {
								_logger.error("Could not find the package for uuid: " + uuid);
								resp.getWriter().write("ERROR: Could not find the package associated with asset uuid.");
							}
						}
					}
				}
			} else {
				_logger.error("Invalid or empty service repository.");
				resp.getWriter().write("ERROR: Invalid or empty service repository.");
			}
		} 
	}


	private IDiagramProfile getProfile(HttpServletRequest req,
			String profileName) {
		IDiagramProfile profile = null;

		IDiagramProfileService service = new ProfileServiceImpl();
		service.init(getServletContext());
		profile = service.findProfile(req, profileName);
		if (profile == null) {
			throw new IllegalArgumentException(
					"Cannot determine the profile to use for interpreting models");
		}
		return profile;
	}

	private byte[] getImageBytes(InputStream is) throws Exception {
		try {
			return IOUtils.toByteArray(is);
		}
		catch (IOException e) {
			throw new Exception("Error creating image byte array.");
		}
		finally {
			if (is != null) { is.close(); }
		}
	}

	private List<String> findPackages(String uuid, IDiagramProfile profile) {
		List<String> packages = new ArrayList<String>();
		String packagesURL = ExternalInfo.getExternalProtocol(profile) + "://" + ExternalInfo.getExternalHost(profile) +
				"/" + profile.getExternalLoadURLSubdomain().substring(0, profile.getExternalLoadURLSubdomain().indexOf("/")) +
				"/rest/packages/";
		try {
			XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLStreamReader reader = factory.createXMLStreamReader(getInputStreamForURL(packagesURL, profile));
			while (reader.hasNext()) {
				if (reader.next() == XMLStreamReader.START_ELEMENT) {
					if ("title".equals(reader.getLocalName())) {
						packages.add(reader.getElementText());
					}
				}
			}
		} catch (Exception e) {
			// we dont want to barf..just log that error happened
			_logger.error(e.getMessage());
		} 
		return packages;
	}

	private void applyAuth(IDiagramProfile profile, HttpURLConnection connection) {
		if (profile.getUsr() != null && profile.getUsr().trim().length() > 0
				&& profile.getPwd() != null
				&& profile.getPwd().trim().length() > 0) {
			BASE64Encoder enc = new sun.misc.BASE64Encoder();
			String userpassword = profile.getUsr() + ":" + profile.getPwd();
			String encodedAuthorization = enc.encode(userpassword.getBytes());
			connection.setRequestProperty("Authorization", "Basic "
					+ encodedAuthorization);
		}
	}

	private InputStream getInputStreamForURL(String urlLocation, IDiagramProfile profile) throws Exception{
		// pretend we are mozilla
		URL url = new URL(urlLocation);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		connection.setRequestMethod("GET");
		connection
		.setRequestProperty(
				"User-Agent",
				"Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.6; en-US; rv:1.9.2.16) Gecko/20110319 Firefox/3.6.16");
		connection
		.setRequestProperty("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		connection.setRequestProperty("Accept-Language", "en-us,en;q=0.5");
		connection.setRequestProperty("Accept-Encoding", "gzip,deflate");
		connection.setRequestProperty("charset", "UTF-8");
		connection.setReadTimeout(5 * 1000);

		if(profile.getUsr() != null && profile.getUsr().trim().length() > 0 && profile.getPwd() != null && profile.getPwd().trim().length() > 0) {
			BASE64Encoder enc = new sun.misc.BASE64Encoder();
			String userpassword = profile.getUsr() + ":" + profile.getPwd();
			String encodedAuthorization = enc.encode( userpassword.getBytes() );
			connection.setRequestProperty("Authorization", "Basic "+ encodedAuthorization);
		}

		connection.connect();

		BufferedReader sreader = new BufferedReader(new InputStreamReader(
				connection.getInputStream(), "UTF-8"));
		StringBuilder stringBuilder = new StringBuilder();

		String line = null;
		while ((line = sreader.readLine()) != null) {
			stringBuilder.append(line + "\n");
		}

		return new ByteArrayInputStream(stringBuilder.toString()
				.getBytes("UTF-8"));
	}

}
