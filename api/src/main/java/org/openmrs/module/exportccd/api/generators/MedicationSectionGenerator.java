package org.openmrs.module.exportccd.api.generators;

import org.openhealthtools.mdht.uml.cda.CDAFactory;
import org.openhealthtools.mdht.uml.cda.StrucDocText;
import org.openhealthtools.mdht.uml.cda.ccd.CCDFactory;
import org.openhealthtools.mdht.uml.cda.ccd.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.ccd.MedicationsSection;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.exportccd.api.utils.ExportCcdUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MedicationSectionGenerator {
	
	@Autowired
	private ExportCcdUtils utils;
	
	public ContinuityOfCareDocument buildMedication(ContinuityOfCareDocument ccd, Patient patient) {
		MedicationsSection medicationSection = CCDFactory.eINSTANCE.createMedicationsSection();
		ccd.addSection(medicationSection);
		medicationSection.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.3.88.11.83.112", "", "HITSP/C83"));
		medicationSection.getTemplateIds().add(utils.buildTemplateID("1.3.6.1.4.1.19376.1.5.3.1.3.19", "", "IHE PCC"));
		medicationSection.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.10.20.1.8", "", "HL7 CCD"));
		medicationSection.setCode(utils
		        .buildCodeCE("10160-0", "2.16.840.1.113883.6.1", "History of medication use", "LOINC"));
		medicationSection.setTitle(utils.buildST("Medication"));
		StringBuffer buffer = new StringBuffer();
		buffer.append("<table border=\"1\" width=\"100%\">");
		buffer.append("<thead>");
		buffer.append("<tr>");
		buffer.append("<th>Medication</th>");
		buffer.append("<th>Date</th>");
		buffer.append("<th>Dose</th>");
		buffer.append("<th>Days</th>");
		buffer.append("</tr>");
		buffer.append("</thead>");
		buffer.append("<tbody>");
		
		List<Obs> observations = Context.getObsService().getObservationsByPersonAndConcept(patient,
		    Context.getConceptService().getConcept(1442));
		for (Obs obs : observations) {
			List<Obs> group = Context.getObsService().findObsByGroupId(obs.getId());
			String dose = "";
			String drug = "";
			String duration = "";
			for (Obs obs2 : group) {
				switch (obs2.getConcept().getId()) {
					case 1444:
						dose = obs2.getValueText();
						break;
					case 1282:
						drug = obs2.getValueCoded().getDisplayString();
						break;
					case 159368:
						duration = String.valueOf(obs2.getValueNumeric());
				}
			}
			
			buffer.append("<tr>");
			buffer.append("<td>" + drug + "</td>");
			buffer.append("<td>" + utils.format(obs.getObsDatetime()) + "</td>");
			buffer.append("<td>" + dose + "</td>");
			buffer.append("<td>" + duration + "</td>");
			buffer.append("</tr>");
			
		}
		
		buffer.append("</tbody>");
		buffer.append("</table>");
		StrucDocText medicationDetails = CDAFactory.eINSTANCE.createStrucDocText();
		medicationDetails.addText(buffer.toString());
		medicationSection.setText(medicationDetails);
		
		return ccd;
	}
}
