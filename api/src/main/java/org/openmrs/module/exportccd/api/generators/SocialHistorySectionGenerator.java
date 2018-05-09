package org.openmrs.module.exportccd.api.generators;

import org.openhealthtools.mdht.uml.cda.CDAFactory;
import org.openhealthtools.mdht.uml.cda.StrucDocText;
import org.openhealthtools.mdht.uml.cda.ccd.CCDFactory;
import org.openhealthtools.mdht.uml.cda.ccd.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.ccd.SocialHistorySection;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.exportccd.api.db.PatientSummaryExportDAO;
import org.openmrs.module.exportccd.api.utils.ExportCcdUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
public class SocialHistorySectionGenerator {
	
	@Autowired
	private ExportCcdUtils utils;
	
	@Autowired
	private PatientSummaryExportDAO dao;
	
	public ContinuityOfCareDocument buildSocialHistory(ContinuityOfCareDocument ccd, Patient patient) {
		SocialHistorySection section = CCDFactory.eINSTANCE.createSocialHistorySection();
		ccd.addSection(section);
		section.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.10.20.1.15", "", "HITSP/C83"));
		section.setCode(utils.buildCodeCE("29762-2", "2.16.840.1.113883.6.1", "Social History", "LOINC"));
		section.setTitle(utils.buildST("Social History"));
		StrucDocText details = CDAFactory.eINSTANCE.createStrucDocText();
		StringBuffer buffer = new StringBuffer();
		buffer.append("<table border=\"1\" width=\"100%\">");
		buffer.append("<thead>");
		buffer.append("<tr>");
		buffer.append("<th>History Element</th>");
		buffer.append("<th>Values</th>");
		buffer.append("<th>Date</th>");
		buffer.append("</tr>");
		buffer.append("</thead>");
		buffer.append("<tbody>");

		List<Concept> historyList = new ArrayList<Concept>();
		historyList.add(Context.getConceptService().getConcept(374)); //planning of a family
		historyList.add(Context.getConceptService().getConcept(1061)); //method of HIV exposure
		historyList.add(Context.getConceptService().getConcept(159936));
		historyList.add(Context.getConceptService().getConcept(1659));
		historyList.add(Context.getConceptService().getConcept(163340));
		List<Obs> listOfObservations = new ArrayList();

		Iterator i$ = historyList.iterator();
		
		Concept concept;
		while (i$.hasNext()) {
			concept = (Concept) i$.next();
			if (concept.isSet()) {
				List<Concept> conceptSet = concept.getSetMembers();
				System.out.println(conceptSet);
				
				for (Concept conceptSet2 : conceptSet) {
					listOfObservations.addAll(Context.getObsService()
					        .getObservationsByPersonAndConcept(patient, conceptSet2));
				}
			} else {
				listOfObservations.addAll(Context.getObsService().getObservationsByPersonAndConcept(patient, concept));
			}
		}
		
		for (Obs obs : listOfObservations) {
			String element = obs.getConcept().getDisplayString();
			String value = obs.getValueCoded().getDisplayString();
			
			buffer.append("<tr>");
			buffer.append("<td>" + element + "</td>");
			buffer.append("<td>" + utils.format(obs.getObsDatetime()) + "</td>");
			buffer.append("<td>" + value + "</td>");
			buffer.append("</tr>");
			
		}
		
		buffer.append("</tbody>");
		buffer.append("</table>");
		details.addText(buffer.toString());
		section.setText(details);
		return ccd;
	}
}
