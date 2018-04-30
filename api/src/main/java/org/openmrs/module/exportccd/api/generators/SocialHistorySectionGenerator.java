package org.openmrs.module.exportccd.api.generators;

import org.openhealthtools.mdht.uml.cda.CDAFactory;
import org.openhealthtools.mdht.uml.cda.Entry;
import org.openhealthtools.mdht.uml.cda.EntryRelationship;
import org.openhealthtools.mdht.uml.cda.Observation;
import org.openhealthtools.mdht.uml.cda.StrucDocText;
import org.openhealthtools.mdht.uml.cda.ccd.CCDFactory;
import org.openhealthtools.mdht.uml.cda.ccd.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.ccd.SocialHistorySection;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CR;
import org.openhealthtools.mdht.uml.hl7.datatypes.CS;
import org.openhealthtools.mdht.uml.hl7.datatypes.CV;
import org.openhealthtools.mdht.uml.hl7.datatypes.DatatypesFactory;
import org.openhealthtools.mdht.uml.hl7.datatypes.ST;
import org.openhealthtools.mdht.uml.hl7.vocab.ActClassObservation;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActMoodDocumentObservation;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActRelationshipEntry;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActRelationshipEntryRelationship;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.exportccd.api.db.PatientSummaryExportDAO;
import org.openmrs.module.exportccd.api.utils.ExportCcdUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
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
		buffer.append("<th>Social History Element</th>");
		buffer.append("<th>Values</th>");
		buffer.append("<th>Date</th>");
		buffer.append("</tr>");
		buffer.append("</thead>");
		buffer.append("<tbody>");
		List<Concept> socialHistoryList = this.dao.getConceptByCategory("SocialHistory");
		List<Obs> obsList = new ArrayList();
		Iterator i$ = socialHistoryList.iterator();
		
		while (i$.hasNext()) {
			Concept concept = (Concept) i$.next();
			obsList.addAll(Context.getObsService().getObservationsByPersonAndConcept(patient, concept));
		}
		
		i$ = obsList.iterator();
		
		while (i$.hasNext()) {
			Obs obs = (Obs) i$.next();
			buffer.append("<tr>");
			buffer.append("<td> <content ID = \"" + obs.getConceptDescription().getDescription() + "\" >"
			        + obs.getConceptDescription().getDescription() + "</content>");
			buffer.append("</td>");
			buffer.append("<td>");
			int type = obs.getConcept().getDatatype().getId().intValue();
			String value = "";
			switch (type) {
				case 1:
					value = obs.getValueNumeric().toString();
					buffer.append(obs.getValueNumeric());
					break;
				case 2:
					value = obs.getValueCoded().getDisplayString();
					buffer.append(obs.getValueCoded().getDisplayString());
					break;
				case 3:
					value = obs.getValueText();
					buffer.append(obs.getValueText());
				case 4:
				case 5:
				case 9:
				case 11:
				case 12:
				default:
					break;
				case 6:
					value = obs.getValueDate().toString();
					buffer.append(obs.getValueDate());
					break;
				case 7:
					value = obs.getValueTime().toString();
					buffer.append(obs.getValueTime());
					break;
				case 8:
					value = obs.getValueDatetime().toString();
					buffer.append(obs.getValueDatetime());
					break;
				case 10:
					value = obs.getValueAsBoolean().toString();
					buffer.append(obs.getValueBoolean());
					break;
				case 13:
					value = obs.getValueComplex();
					buffer.append(obs.getValueComplex());
			}
			
			buffer.append("</td>");
			buffer.append("<td>" + utils.format(obs.getObsDatetime()) + "</td>");
			buffer.append("</tr>");
			Entry entry = CDAFactory.eINSTANCE.createEntry();
			entry.setTypeCode(x_ActRelationshipEntry.DRIV);
			Observation observation = CDAFactory.eINSTANCE.createObservation();
			observation.setClassCode(ActClassObservation.OBS);
			observation.setMoodCode(x_ActMoodDocumentObservation.EVN);
			observation.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.10.20.1.33", "", ""));
			observation.getIds().add(utils.buildID(obs.getUuid(), ""));
			observation.setCode(utils.buildConceptCode(obs.getConcept(), "SNOMED", "AMPATH", "LOINC"));
			CS statusCode = DatatypesFactory.eINSTANCE.createCS();
			statusCode.setCode("completed");
			observation.setStatusCode(statusCode);
			observation.setEffectiveTime(utils.buildEffectiveTimeinIVL(obs.getObsDatetime(), (Date) null));
			ST value1 = utils.buildST(value);
			observation.getValues().add(value1);
			EntryRelationship entryRelationship = CDAFactory.eINSTANCE.createEntryRelationship();
			entryRelationship.setTypeCode(x_ActRelationshipEntryRelationship.SUBJ);
			entryRelationship.setInversionInd(true);
			Observation eObservation = CDAFactory.eINSTANCE.createObservation();
			eObservation.setClassCode(ActClassObservation.OBS);
			eObservation.setMoodCode(x_ActMoodDocumentObservation.EVN);
			eObservation.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.10.20.1.41", "", ""));
			eObservation.setCode(utils.buildCode("ASSERTION", "2.16.840.1.113883.5.4", "", ""));
			CS statusCode1 = DatatypesFactory.eINSTANCE.createCS();
			statusCode1.setCode("completed");
			eObservation.setStatusCode(statusCode1);
			CD value2 = utils.buildCode("404684003", "2.16.840.1.113883.6.96", "Clinical Finding", "");
			CR qualifier = DatatypesFactory.eINSTANCE.createCR();
			CV episodicity = DatatypesFactory.eINSTANCE.createCV();
			qualifier.setName(episodicity);
			qualifier.setValue(utils.buildCode("288527008", "2.16.840.1.113883.6.96", "New episode", ""));
			value2.getQualifiers().add(qualifier);
			eObservation.getValues().add(value2);
			EntryRelationship e = CDAFactory.eINSTANCE.createEntryRelationship();
			e.setTypeCode(x_ActRelationshipEntryRelationship.SAS);
			Observation eObservation2 = CDAFactory.eINSTANCE.createObservation();
			eObservation2.setClassCode(ActClassObservation.OBS);
			eObservation2.setMoodCode(x_ActMoodDocumentObservation.EVN);
			eObservation2.getIds().add(utils.buildID(obs.getConcept().getUuid(), ""));
			eObservation2.setCode(utils.buildConceptCode(obs.getConcept(), "SNOMED"));
			e.setObservation(eObservation2);
			eObservation.getEntryRelationships().add(e);
			entryRelationship.setObservation(eObservation);
			observation.getEntryRelationships().add(entryRelationship);
			entry.setObservation(observation);
			section.getEntries().add(entry);
		}
		
		buffer.append("</tbody>");
		buffer.append("</table>");
		details.addText(buffer.toString());
		section.setText(details);
		return ccd;
	}
}
