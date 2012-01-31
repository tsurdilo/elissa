package org.elissa.web.server;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.elissa.web.profile.IDiagramProfile;
import org.elissa.web.profile.IDiagramProfileService;
import org.elissa.web.profile.impl.ExternalInfo;
import org.elissa.web.profile.impl.ProfileServiceImpl;
import org.json.JSONArray;
import org.json.JSONObject;

import sun.misc.BASE64Encoder;


/**
 * 
 * Queries Guvnor for process version info.
 * 
 * @author Tihomir Surdilovic
 */
public class ProcessDiffServiceServlet extends HttpServlet {
	private static final Logger _logger = Logger
			.getLogger(ProcessDiffServiceServlet.class);

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
		String versionNum = req.getParameter("version");

		IDiagramProfile profile = getProfile(req, profileName);
		String[] packageAssetInfo = findPackageAndAssetInfo(uuid, profile);
        String packageName = packageAssetInfo[0];
        String assetName = packageAssetInfo[1];
        if(action != null && action.equals("getversion") && versionNum != null) {
        	resp.setCharacterEncoding("UTF-8");
			resp.setContentType("text/xml");
			try {
				resp.getWriter().write(getAssetVerionSource(packageName, assetName, versionNum, profile));
			} catch (Throwable t) {
				resp.getWriter().write("");
			}
        } else {
	        List<String> versionList;
			try {
				versionList = getAssetVersions(packageName, assetName, uuid, profile);
			} catch (Throwable t) {
				versionList = new ArrayList<String>();
			}
	        JSONObject jsonObject = new JSONObject();
			if(versionList != null && versionList.size() > 0) {
				for(String version : versionList) {
					try {
						jsonObject.put(version, version);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			resp.setCharacterEncoding("UTF-8");
			resp.setContentType("application/json");
			resp.getWriter().write(jsonObject.toString());
        }
	}

	private String getAssetVerionSource(String packageName, String assetName, String versionNum, IDiagramProfile profile) {
		String versionURL = ExternalInfo.getExternalProtocol(profile)
				+ "://"
                + ExternalInfo.getExternalHost(profile)
                + "/"
                + profile.getExternalLoadURLSubdomain().substring(0,
                        profile.getExternalLoadURLSubdomain().indexOf("/"))
                + "/rest/packages/" + packageName + "/assets/" + assetName 
                + "/versions/" + versionNum + "/source/";
		try {
			return IOUtils.toString(getInputStreamForURL(versionURL, "GET", profile), "UTF-8");
		} catch (Exception e) {
			return "";
		}
	}
	
	private List<String> getAssetVersions(String packageName, String assetName, String uuid, IDiagramProfile profile) {
		String assetVersionURL = ExternalInfo.getExternalProtocol(profile)
				+ "://"
                + ExternalInfo.getExternalHost(profile)
                + "/"
                + profile.getExternalLoadURLSubdomain().substring(0,
                        profile.getExternalLoadURLSubdomain().indexOf("/"))
                + "/rest/packages/" + packageName + "/assets/" + assetName 
                + "/versions/";
		List<String> versionList = new ArrayList<String>();
		try {
			XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader reader = factory
                    .createXMLStreamReader(getInputStreamForURL(
                    		assetVersionURL, "GET", profile));
            boolean isFirstTitle = true;
            String title = "";
            while (reader.hasNext()) {
                int next = reader.next();
                if (next == XMLStreamReader.START_ELEMENT) {
                    if ("title".equals(reader.getLocalName())) {
                    	if(isFirstTitle) {
                    		isFirstTitle = false;
                    	} else {
                    		versionList.add(reader.getElementText());
                    	}
                    }
                }
            }
		} catch (Exception e) {
            _logger.error(e.getMessage());
        }
		return versionList;
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

	private InputStream getInputStreamForURL(String urlLocation,
			String requestMethod, IDiagramProfile profile) throws Exception {
		URL url = new URL(urlLocation);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		connection.setRequestMethod(requestMethod);
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
		connection.setReadTimeout(3 * 1000);
		connection.setConnectTimeout(3 * 1000);

		applyAuth(profile, connection);

		connection.connect();

		BufferedReader sreader = new BufferedReader(new InputStreamReader(
				connection.getInputStream(), "UTF-8"));
		StringBuilder stringBuilder = new StringBuilder();

		String line = null;
		while ((line = sreader.readLine()) != null) {
			stringBuilder.append(line + "\n");
		}

		return new ByteArrayInputStream(stringBuilder.toString().getBytes(
				"UTF-8"));
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

	private String[] findPackageAndAssetInfo(String uuid,
			IDiagramProfile profile) {
		List<String> packages = new ArrayList<String>();
		String packagesURL = ExternalInfo.getExternalProtocol(profile)
				+ "://"
				+ ExternalInfo.getExternalHost(profile)
				+ "/"
				+ profile.getExternalLoadURLSubdomain().substring(0,
						profile.getExternalLoadURLSubdomain().indexOf("/"))
				+ "/rest/packages/";
		try {
			XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLStreamReader reader = factory
					.createXMLStreamReader(getInputStreamForURL(packagesURL,
							"GET", profile));
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

		boolean gotPackage = false;
		String[] pkgassetinfo = new String[2];
		for (String nextPackage : packages) {
			String packageAssetURL = ExternalInfo.getExternalProtocol(profile)
					+ "://"
					+ ExternalInfo.getExternalHost(profile)
					+ "/"
					+ profile.getExternalLoadURLSubdomain().substring(0,
							profile.getExternalLoadURLSubdomain().indexOf("/"))
					+ "/rest/packages/" + nextPackage + "/assets/";
			try {
				XMLInputFactory factory = XMLInputFactory.newInstance();
				XMLStreamReader reader = factory
						.createXMLStreamReader(getInputStreamForURL(
								packageAssetURL, "GET", profile));
				String title = "";
				while (reader.hasNext()) {
					int next = reader.next();
					if (next == XMLStreamReader.START_ELEMENT) {
						if ("title".equals(reader.getLocalName())) {
							title = reader.getElementText();
						}
						if ("uuid".equals(reader.getLocalName())) {
							String eleText = reader.getElementText();
							if (uuid.equals(eleText)) {
								pkgassetinfo[0] = nextPackage;
								pkgassetinfo[1] = title;
								gotPackage = true;
							}
						}
					}
				}
			} catch (Exception e) {
				// we dont want to barf..just log that error happened
				_logger.error(e.getMessage());
			}
			if (gotPackage) {
				// noo need to loop through rest of packages
				break;
			}
		}
		return pkgassetinfo;
	}

}
