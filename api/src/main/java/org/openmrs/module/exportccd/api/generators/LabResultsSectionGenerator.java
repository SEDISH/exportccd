package org.openmrs.module.exportccd.api.generators;

import org.openhealthtools.mdht.uml.cda.CDAFactory;
import org.openhealthtools.mdht.uml.cda.Component4;
import org.openhealthtools.mdht.uml.cda.Entry;
import org.openhealthtools.mdht.uml.cda.Observation;
import org.openhealthtools.mdht.uml.cda.ObservationRange;
import org.openhealthtools.mdht.uml.cda.Organizer;
import org.openhealthtools.mdht.uml.cda.ReferenceRange;
import org.openhealthtools.mdht.uml.cda.StrucDocText;
import org.openhealthtools.mdht.uml.cda.ccd.CCDFactory;
import org.openhealthtools.mdht.uml.cda.ccd.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.ccd.ResultsSection;
import org.openhealthtools.mdht.uml.hl7.datatypes.CS;
import org.openhealthtools.mdht.uml.hl7.datatypes.DatatypesFactory;
import org.openhealthtools.mdht.uml.hl7.datatypes.PQ;
import org.openhealthtools.mdht.uml.hl7.vocab.ActClassObservation;
import org.openhealthtools.mdht.uml.hl7.vocab.ActMood;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActClassDocumentEntryOrganizer;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActMoodDocumentObservation;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActRelationshipEntry;
import org.openmrs.Concept;
import org.openmrs.ConceptNumeric;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.exportccd.api.db.PatientSummaryExportDAO;
import org.openmrs.module.exportccd.api.utils.ExportCcdUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class LabResultsSectionGenerator {
	
	@Autowired
	private ExportCcdUtils utils;
	
	@Autowired
	private PatientSummaryExportDAO dao;
	
	public ContinuityOfCareDocument buildLabResults(ContinuityOfCareDocument ccd, Patient patient) {
		ResultsSection section = CCDFactory.eINSTANCE.createResultsSection();
		ccd.addSection(section);
		section.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.3.88.11.83.122", "", "HITSP/C83"));
		section.getTemplateIds().add(utils.buildTemplateID("1.3.6.1.4.1.19376.1.5.3.1.3.28", "", "IHE PCC"));
		section.setCode(utils.buildCodeCE("30954-2", "2.16.840.1.113883.6.1", "Relevant diagnostic tests/laboratory data",
		    "LOINC"));
		section.setTitle(utils.buildST("Results"));
		StringBuffer buffer = new StringBuffer();
		buffer.append("<table border=\"1\" width=\"100%\">");
		buffer.append("<thead>");
		buffer.append("<tr>");
		buffer.append("<th>Date</th>");
		List<Concept> labResultsList = new ArrayList<Concept>();
		labResultsList.add(Context.getConceptService().getConcept(160593));
		List<Obs> listOfObservations = new ArrayList();
		Map<String, String> labResultData = new HashMap();
		List<Concept> observedConceptList = new ArrayList();
		Set<Date> dateSet = new HashSet();
		Iterator i$ = labResultsList.iterator();
		
		Concept concept;
		while (i$.hasNext()) {
			concept = (Concept) i$.next();
			if (concept.isSet()) {
				List<Concept> conceptSet = concept.getSetMembers();
				System.out.println(conceptSet);
				Iterator ii$ = conceptSet.iterator();
				
				while (ii$.hasNext()) {
					Concept conceptSet2 = (Concept) ii$.next();
					listOfObservations.addAll(Context.getObsService()
					        .getObservationsByPersonAndConcept(patient, conceptSet2));
					observedConceptList.add(conceptSet2);
				}
			} else {
				listOfObservations.addAll(Context.getObsService().getObservationsByPersonAndConcept(patient, concept));
				observedConceptList.add(concept);
			}
		}
		
		i$ = listOfObservations.iterator();
		
		while (i$.hasNext()) {
			Obs obs = (Obs) i$.next();
			Date dateCreated = obs.getObsDatetime();
			dateSet.add(dateCreated);
			if (labResultData.containsKey(dateCreated)) {
				String data = labResultData.get(dateCreated);
				data = data + "," + obs.getId();
				labResultData.put(dateCreated + obs.getConcept().getId().toString(), data);
			} else {
				labResultData.put(dateCreated + obs.getConcept().getId().toString(), obs.getId().toString());
			}
		}
		
		System.out.println(labResultData);
		i$ = dateSet.iterator();
		
		while (i$.hasNext()) {
			Date date = (Date) i$.next();
			buffer.append("<th>" + utils.format(date) + "</th>");
		}
		
		buffer.append("</tr>");
		buffer.append("</thead>");
		buffer.append("<tbody>");
		i$ = observedConceptList.iterator();
		
		while (i$.hasNext()) {
			concept = (Concept) i$.next();
			buffer.append("<tr>");
			buffer.append("<td><content Id= \"" + concept.getDisplayString() + "\">" + concept.getDisplayString()
			        + "</content></td>");
			Iterator ii$ = dateSet.iterator();
			
			while (ii$.hasNext()) {
				Date date = (Date) ii$.next();
				if (labResultData.containsKey(date + "" + concept)) {
					String obsId = labResultData.get(date + "" + concept);
					Obs obs = Context.getObsService().getObs(Integer.parseInt(obsId));
					int type = obs.getConcept().getDatatype().getId();
					ConceptNumeric c = Context.getConceptService().getConceptNumeric(concept.getId());
					String units = "";
					if (c != null && c.getUnits() != null) {
						units = c.getUnits();
					}
					switch (type) {
						case 1:
							buffer.append("<td>" + obs.getValueNumeric() + units + "</td>");
							break;
						case 2:
							buffer.append("<td>" + obs.getValueCoded().getDisplayString() + units + "</td>");
							break;
						case 3:
							buffer.append("<td>" + obs.getValueText() + units + "</td>");
						case 4:
						case 5:
						case 9:
						case 11:
						case 12:
						default:
							break;
						case 6:
							buffer.append("<td>" + utils.format(obs.getValueDate()) + "</td>");
							break;
						case 7:
							buffer.append("<td>" + obs.getValueTime() + units + "</td>");
							break;
						case 8:
							buffer.append("<td>" + utils.format(obs.getValueDatetime()) + "</td>");
							break;
						case 10:
							buffer.append("<td>" + obs.getValueBoolean() + units + "</td>");
							break;
						case 13:
							buffer.append("<td>" + obs.getValueComplex() + units + "</td>");
					}
					
					Entry labResultEntry = CDAFactory.eINSTANCE.createEntry();
					labResultEntry.setTypeCode(x_ActRelationshipEntry.DRIV);
					Organizer organizer = CDAFactory.eINSTANCE.createOrganizer();
					organizer.setClassCode(x_ActClassDocumentEntryOrganizer.CLUSTER);
					organizer.setMoodCode(ActMood.EVN);
					organizer.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.10.20.1.32", "", ""));
					organizer.getIds().add(utils.buildID(obs.getUuid(), ""));
					organizer.setCode(utils.buildCode("56850-1", "2.16.840.1.113883.6.1",
					    "Interpretation and review of laboratory results", "LOINC"));
					CS statusCode = DatatypesFactory.eINSTANCE.createCS();
					statusCode.setCode("completed");
					organizer.setStatusCode(statusCode);
					organizer.setEffectiveTime(utils.buildEffectiveTimeinIVL(date, null));
					Component4 component = CDAFactory.eINSTANCE.createComponent4();
					Observation observation = CDAFactory.eINSTANCE.createObservation();
					observation.setClassCode(ActClassObservation.OBS);
					observation.setMoodCode(x_ActMoodDocumentObservation.EVN);
					observation.getTemplateIds().add(
					    utils.buildTemplateID("2.16.840.1.113883.3.88.11.83.15", "", "HITSP C83"));
					observation.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.10.20.1.31", "", "CCD"));
					observation.getTemplateIds().add(utils.buildTemplateID("1.3.6.1.4.1.19376.1.5.3.1.4.13", "", "IHE PCC"));
					observation.getIds().add(utils.buildID(obs.getUuid(), ""));
					observation.setCode(utils.buildConceptCode(concept, "SNOMED", "LOINC"));
					observation.setText(utils.buildEDText("#" + concept.getDisplayString()));
					CS statusCode1 = DatatypesFactory.eINSTANCE.createCS();
					statusCode1.setCode("completed");
					observation.setStatusCode(statusCode1);
					observation.setEffectiveTime(utils.buildEffectiveTimeinIVL(obs.getObsDatetime(), null));
					PQ unit = DatatypesFactory.eINSTANCE.createPQ();
					unit.setUnit(units);
					unit.setValue(obs.getValueNumeric());
					observation.getValues().add(unit);
					ReferenceRange conceptRange = CDAFactory.eINSTANCE.createReferenceRange();
					ObservationRange observationRange = CDAFactory.eINSTANCE.createObservationRange();
					observationRange.setNullFlavor(NullFlavor.UNK);
					conceptRange.setObservationRange(observationRange);
					observation.getReferenceRanges().add(conceptRange);
					component.setObservation(observation);
					organizer.getComponents().add(component);
					labResultEntry.setOrganizer(organizer);
					section.getEntries().add(labResultEntry);
				} else {
					buffer.append("<td></td>");
				}
			}
			
			buffer.append("</tr>");
		}
		
		buffer.append("</tbody>");
		buffer.append("</table>");
		StrucDocText details = CDAFactory.eINSTANCE.createStrucDocText();
		String s = new String(buffer.toString().getBytes(), Charset.forName("UTF-8"));
		details.addText(s);
		section.setText(details);
		return ccd;
	}
	
}
