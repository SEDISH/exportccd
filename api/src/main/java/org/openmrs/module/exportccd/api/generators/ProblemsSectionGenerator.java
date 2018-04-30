package org.openmrs.module.exportccd.api.generators;

import org.openhealthtools.mdht.uml.cda.Act;
import org.openhealthtools.mdht.uml.cda.CDAFactory;
import org.openhealthtools.mdht.uml.cda.Entry;
import org.openhealthtools.mdht.uml.cda.EntryRelationship;
import org.openhealthtools.mdht.uml.cda.Observation;
import org.openhealthtools.mdht.uml.cda.StrucDocText;
import org.openhealthtools.mdht.uml.cda.ccd.CCDFactory;
import org.openhealthtools.mdht.uml.cda.ccd.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.ccd.ProblemSection;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.CS;
import org.openhealthtools.mdht.uml.hl7.datatypes.DatatypesFactory;
import org.openhealthtools.mdht.uml.hl7.vocab.ActClassObservation;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActClassDocumentEntryAct;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActMoodDocumentObservation;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActRelationshipEntry;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActRelationshipEntryRelationship;
import org.openhealthtools.mdht.uml.hl7.vocab.x_DocumentActMood;
import org.openmrs.Patient;
import org.openmrs.activelist.Problem;
import org.openmrs.activelist.ProblemModifier;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.exportccd.api.utils.ExportCcdUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Component
public class ProblemsSectionGenerator {
	
	@Autowired
	private ExportCcdUtils utils;
	
	public ContinuityOfCareDocument buildProblems(ContinuityOfCareDocument ccd, Patient patient) {
		ProblemSection problemSection = CCDFactory.eINSTANCE.createProblemSection();
		ccd.addSection(problemSection);
		problemSection.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.3.88.11.83.103", "", "HITSP/C83"));
		problemSection.getTemplateIds().add(utils.buildTemplateID("1.3.6.1.4.1.19376.1.5.3.1.3.6", "", "IHE PCC"));
		problemSection.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.10.20.1.11", "", "HL7 CCD"));
		problemSection.setCode(utils.buildCodeCE("11450-4", "2.16.840.1.113883.6.1", "Problem list", "LOINC"));
		problemSection.setTitle(utils.buildST("Problems"));
		CE problemCode = DatatypesFactory.eINSTANCE.createCE();
		problemCode.setNullFlavor(NullFlavor.NA);
		StringBuffer buffer = new StringBuffer();
		buffer.append("<table border=\"1\" width=\"100%\">");
		buffer.append("<thead>");
		buffer.append("<tr>");
		buffer.append("<th>Problem</th>");
		buffer.append("<th>Effective Date</th>");
		buffer.append("<th>Status</th>");
		buffer.append("</tr>");
		buffer.append("</thead>");
		buffer.append("<tbody>");
		PatientService patientService = Context.getPatientService();
		List<Problem> patientProblemList = patientService.getProblems(patient);
		List<Entry> problemEntryList = new ArrayList();
		Iterator i$ = patientProblemList.iterator();
		
		while (i$.hasNext()) {
			Problem patientProblem = (Problem) i$.next();
			buffer.append("<tr>");
			buffer.append("<td><content ID=\"" + patientProblem.getProblem() + "\">"
			        + patientProblem.getProblem().getDisplayString() + "</content></td>");
			Date date = patientProblem.getStartDate();
			buffer.append("<td>" + utils.format(date) + "</td>");
			buffer.append("<td>" + patientProblem.getModifier().getText() + "</td>");
			buffer.append("</tr>");
			Entry problemEntry = CDAFactory.eINSTANCE.createEntry();
			problemEntry.setTypeCode(x_ActRelationshipEntry.DRIV);
			Act problemAct = CDAFactory.eINSTANCE.createAct();
			problemAct.setClassCode(x_ActClassDocumentEntryAct.ACT);
			problemAct.setMoodCode(x_DocumentActMood.EVN);
			problemEntry.setAct(problemAct);
			problemAct.getTemplateIds().add(
			    utils.buildTemplateID("2.16.840.1.113883.3.88.11.83.7", (String) null, "HITSP C83"));
			problemAct.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.10.20.1.27", (String) null, "CCD"));
			problemAct.getTemplateIds().add(
			    utils.buildTemplateID("1.3.6.1.4.1.19376.1.5.3.1.4.5.2", (String) null, "IHE PCC"));
			problemAct.getTemplateIds().add(
			    utils.buildTemplateID("1.3.6.1.4.1.19376.1.5.3.1.4.5.1", (String) null, "IHE PCC"));
			problemAct.getIds().add(utils.buildID(patientProblem.getUuid(), ""));
			problemSection.getEntries().add(problemEntry);
			problemAct.setEffectiveTime(utils.buildEffectiveTimeinIVL(patientProblem.getStartDate(),
			    patientProblem.getEndDate()));
			CS statusCode = DatatypesFactory.eINSTANCE.createCS();
			statusCode.setCode("completed");
			problemAct.setStatusCode(statusCode);
			CD problemActCode = DatatypesFactory.eINSTANCE.createCD();
			problemActCode.setNullFlavor(NullFlavor.NA);
			problemAct.setCode(problemActCode);
			EntryRelationship problemEntryRelationship = CDAFactory.eINSTANCE.createEntryRelationship();
			problemAct.getEntryRelationships().add(problemEntryRelationship);
			problemEntryRelationship.setTypeCode(x_ActRelationshipEntryRelationship.SUBJ);
			Observation problemObservation = CDAFactory.eINSTANCE.createObservation();
			problemObservation.setClassCode(ActClassObservation.OBS);
			problemObservation.setMoodCode(x_ActMoodDocumentObservation.EVN);
			problemObservation.getTemplateIds().add(
			    utils.buildTemplateID("2.16.840.1.113883.10.20.1.28", (String) null, "CCD"));
			problemObservation.getTemplateIds().add(
			    utils.buildTemplateID("1.3.6.1.4.1.19376.1.5.3.1.4.5", (String) null, "IHE PCC"));
			problemObservation.getIds().add(utils.buildID(patientProblem.getUuid(), ""));
			problemObservation.setCode(utils.buildCode("64572001", "2.16.840.1.113883.6.96", "Condition", "SNOMED-CT"));
			problemObservation.setText(utils.buildEDText("#" + patientProblem.getProblem()));
			problemEntryRelationship.setObservation(problemObservation);
			CS statusCodeObservation = DatatypesFactory.eINSTANCE.createCS();
			statusCodeObservation.setCode("completed");
			problemObservation.setStatusCode(statusCodeObservation);
			problemObservation.setEffectiveTime(utils.buildEffectiveTimeinIVL(patientProblem.getStartDate(),
			    patientProblem.getEndDate()));
			CE code = utils.buildConceptCode(patientProblem.getProblem());
			problemObservation.getValues().add(
			    utils.buildCode(code.getCode(), code.getCodeSystem(), code.getDisplayName(), code.getCodeSystemName()));
			EntryRelationship problemObsEntryRelationship = CDAFactory.eINSTANCE.createEntryRelationship();
			problemObsEntryRelationship.setTypeCode(x_ActRelationshipEntryRelationship.REFR);
			Observation statusObservation = CDAFactory.eINSTANCE.createObservation();
			statusObservation.setClassCode(ActClassObservation.OBS);
			statusObservation.setMoodCode(x_ActMoodDocumentObservation.EVN);
			statusObservation.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.10.20.1.50", "", ""));
			statusObservation.setCode(utils.buildCode("33999-4", "2.16.840.1.113883.6.1", "Status", "LOINC"));
			problemObsEntryRelationship.setObservation(statusObservation);
			ProblemModifier var10000 = patientProblem.getModifier();
			patientProblem.getModifier();
			if (var10000.equals(ProblemModifier.HISTORY_OF)) {
				statusObservation.getValues().add(
				    utils.buildCode("90734009", "2.16.840.1.113883.6.96", "Chronic", "Snomed CT"));
			} else {
				statusObservation.getValues().add(
				    utils.buildCode("415684004", "2.16.840.1.113883.6.96", "Rule Out", "Snomed CT"));
			}
			
			CS obsStatusObsCode = DatatypesFactory.eINSTANCE.createCS();
			obsStatusObsCode.setCode("completed");
			statusObservation.setStatusCode(obsStatusObsCode);
			problemObservation.getEntryRelationships().add(problemObsEntryRelationship);
			problemEntryList.add(problemEntry);
		}
		
		buffer.append("</tbody>");
		buffer.append("</table>");
		StrucDocText problemDetails = CDAFactory.eINSTANCE.createStrucDocText();
		problemDetails.addText(buffer.toString());
		problemSection.setText(problemDetails);
		problemSection.getEntries().addAll(problemEntryList);
		return ccd;
	}
}
