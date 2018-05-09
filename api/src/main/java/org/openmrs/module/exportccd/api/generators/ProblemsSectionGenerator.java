package org.openmrs.module.exportccd.api.generators;

import org.openhealthtools.mdht.uml.cda.CDAFactory;
import org.openhealthtools.mdht.uml.cda.StrucDocText;
import org.openhealthtools.mdht.uml.cda.ccd.CCDFactory;
import org.openhealthtools.mdht.uml.cda.ccd.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.ccd.ProblemSection;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.DatatypesFactory;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.exportccd.api.utils.ExportCcdUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

		List<Obs> observations = Context.getObsService().getObservationsByPersonAndConcept(patient,
		    Context.getConceptService().getConcept(159947));
		for (Obs obs : observations) {
			List<Obs> group = Context.getObsService().findObsByGroupId(obs.getId());
			String problem = "";
			String status = "";
			for (Obs obs2 : group) {
				switch (obs2.getConcept().getId()) {
					case 1284:
						problem = obs2.getValueCoded().getDisplayString();
						break;
					case 159394:
						status = obs2.getValueCoded().getDisplayString();
						break;
				}
			}
			
			buffer.append("<tr>");
			buffer.append("<td>" + problem + "</td>");
			buffer.append("<td>" + utils.format(obs.getObsDatetime()) + "</td>");
			buffer.append("<td>" + status + "</td>");
			buffer.append("</tr>");
			
		}
		
		buffer.append("</tbody>");
		buffer.append("</table>");
		StrucDocText problemDetails = CDAFactory.eINSTANCE.createStrucDocText();
		problemDetails.addText(buffer.toString());
		problemSection.setText(problemDetails);
		return ccd;
	}
}
