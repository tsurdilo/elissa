/**
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.elissa.web.runner;

import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.compiler.BPMN2ProcessFactory;
import org.drools.compiler.ProcessBuilderFactory;
import org.drools.marshalling.impl.ProcessMarshallerFactory;
import org.drools.persistence.jpa.JPAKnowledgeService;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessRuntimeFactory;
import org.jbpm.bpmn2.BPMN2ProcessProviderImpl;
import org.jbpm.marshalling.impl.ProcessMarshallerFactoryServiceImpl;
import org.jbpm.process.builder.ProcessBuilderFactoryServiceImpl;
import org.jbpm.process.instance.ProcessRuntimeFactoryServiceImpl;
import org.jbpm.process.workitem.wsht.SyncWSHumanTaskHandler;
import org.jbpm.task.service.TaskService;
import org.jbpm.task.service.local.LocalTaskService;

/**
 * jBPM Process execution servlet.
 * @author tsurdilo
 */
public class RunnerServlet extends HttpServlet {
	private static final Logger _logger = Logger.getLogger(RunnerServlet.class);
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
		
	}
	
	private StatefulKnowledgeSession createNewSession(String bpmn2Str) {
		KnowledgeBase kbase = null;
		ProcessBuilderFactory.setProcessBuilderFactoryService(new ProcessBuilderFactoryServiceImpl());
        ProcessMarshallerFactory.setProcessMarshallerFactoryService(new ProcessMarshallerFactoryServiceImpl());
        ProcessRuntimeFactory.setProcessRuntimeFactoryService(new ProcessRuntimeFactoryServiceImpl());
        BPMN2ProcessFactory.setBPMN2ProcessProvider(new BPMN2ProcessProviderImpl());
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        
        // add resources
        // kbuilder.add(ResourceFactory.newFileResource(subfile), ResourceType.BPMN2);

        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
        StatefulKnowledgeSession ksession = null;
        EntityManagerFactory emf = Persistence.createEntityManagerFactory(
                "org.jbpm.persistence.jpa");
        Environment env = KnowledgeBaseFactory.newEnvironment();
        env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);
        Properties sessionconfigproperties = new Properties();
        sessionconfigproperties.put("drools.processInstanceManagerFactory", "org.jbpm.persistence.processinstance.JPAProcessInstanceManagerFactory");
        sessionconfigproperties.put("drools.processSignalManagerFactory", "org.jbpm.persistence.processinstance.JPASignalManagerFactory");
        KnowledgeSessionConfiguration config = KnowledgeBaseFactory.newKnowledgeSessionConfiguration(sessionconfigproperties);
        
        ksession = JPAKnowledgeService.newStatefulKnowledgeSession(kbase, config, env);
        
        TaskService taskService = HumanTaskService.getService();
        SyncWSHumanTaskHandler handler = new SyncWSHumanTaskHandler(new LocalTaskService(taskService.createSession()), ksession);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", handler);
        
        // temp.
        return null;
	}
}
