package org.elissa.web.server;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.elissa.bpmn2.validation.BPMN2SyntaxChecker;
import org.elissa.web.profile.IDiagramProfile;
import org.elissa.web.profile.IDiagramProfileService;
import org.elissa.web.profile.impl.ProfileServiceImpl;
import org.json.JSONException;
import org.json.JSONObject;


/** 
 * 
 * Check syntax of a BPMN2 process.
 * 
 * @author Tihomir Surdilovic
 */
public class SyntaxCheckerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	@Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }
	
	@Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
		String json = req.getParameter("data");
        String profileName = req.getParameter("profile");
        String preprocessingData = req.getParameter("pp");
        String uuid = req.getParameter("uuid");
        IDiagramProfile profile = getProfile(req, profileName);

        BPMN2SyntaxChecker checker = new BPMN2SyntaxChecker(json, preprocessingData, profile, uuid);
		checker.checkSyntax();
		resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        
        if(checker.errorsFound()) {
			resp.getWriter().write(checker.getErrorsAsJson().toString());
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
}
