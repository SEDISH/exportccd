package org.openmrs.module.exportccd.api.utils;

import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.DatatypesFactory;
import org.openhealthtools.mdht.uml.hl7.datatypes.ED;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVXB_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.ST;
import org.openhealthtools.mdht.uml.hl7.datatypes.TS;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

@Component
public class ExportCcdUtils {
	
	SimpleDateFormat s = new SimpleDateFormat("yyyyMMdd");
	
	public String format(Date date) {
		return s.format(date);
	}
	
	public ST buildST(String title) {
		ST displayTitle = DatatypesFactory.eINSTANCE.createST();
		displayTitle.addText(title);
		return displayTitle;
	}
	
	public II buildID(String root, String extension) {
		II id = DatatypesFactory.eINSTANCE.createII();
		id.setRoot(root);
		id.setExtension(extension);
		return id;
	}
	
	public CE buildConceptCode(Concept c, String... source) {
		Collection<ConceptMap> conceptMap = c.getConceptMappings();
		CE codes = DatatypesFactory.eINSTANCE.createCE();
		Iterator i$ = conceptMap.iterator();
		
		while (i$.hasNext()) {
			ConceptMap n = (ConceptMap) i$.next();
			if (n.getSource().getName().contains("SNOMED")) {
				codes.setCodeSystem("2.16.840.1.113883.6.96");
			} else if (n.getSource().getName().contains("LOINC")) {
				codes.setCodeSystem("2.16.840.1.113883.6.1");
			} else if (n.getSource().getName().contains("RxNorm")) {
				codes.setCodeSystem("2.16.840.1.113883.6.88");
			} else if (!n.getSource().getName().contains("C4") && !n.getSource().getName().contains("CPT-4")) {
				if (!n.getSource().getName().contains("C5") && !n.getSource().getName().contains("CPT-5")) {
					if (!n.getSource().getName().contains("I9") && !n.getSource().getName().contains("ICD9")) {
						if (!n.getSource().getName().contains("I10") && !n.getSource().getName().contains("ICD10")) {
							if (!n.getSource().getName().contains("C2") && !n.getSource().getName().contains("CPT-2")) {
								if (n.getSource().getName().contains("FDDX")) {
									codes.setCodeSystem("2.16.840.1.113883.6.63");
								} else if (n.getSource().getName().contains("MEDCIN")) {
									codes.setCodeSystem("2.16.840.1.113883.6.26");
								}
							} else {
								codes.setCodeSystem("2.16.840.1.113883.6.13");
							}
						} else {
							codes.setCodeSystem("2.16.840.1.113883.6.3");
						}
					} else {
						codes.setCodeSystem("2.16.840.1.113883.6.42");
					}
				} else {
					codes.setCodeSystem("2.16.840.1.113883.6.82");
				}
			} else {
				codes.setCodeSystem("2.16.840.1.113883.6.12");
			}
			
			codes.setCode(n.getSourceCode());
			codes.setCodeSystemName(n.getSource().getName());
			codes.setDisplayName(n.getConcept().getDisplayString());
		}
		
		return codes;
	}
	
	public CD buildCode(String code, String codeSystem, String displayString, String codeSystemName) {
		CD e = DatatypesFactory.eINSTANCE.createCD();
		e.setCode(code);
		e.setCodeSystem(codeSystem);
		e.setDisplayName(displayString);
		e.setCodeSystemName(codeSystemName);
		return e;
	}
	
	public CE buildCodeCE(String code, String codeSystem, String displayString, String codeSystemName) {
		CE e = DatatypesFactory.eINSTANCE.createCE();
		e.setCode(code);
		e.setCodeSystem(codeSystem);
		e.setDisplayName(displayString);
		e.setCodeSystemName(codeSystemName);
		return e;
	}
	
	public ED buildEDText(String value) {
		ED text = DatatypesFactory.eINSTANCE.createED();
		text.addText("<reference value=\"" + value + "\"/>");
		return text;
	}
	
	public II buildTemplateID(String root, String extension, String assigningAuthorityName) {
		II templateID = DatatypesFactory.eINSTANCE.createII();
		templateID.setAssigningAuthorityName(assigningAuthorityName);
		templateID.setRoot(root);
		templateID.setExtension(extension);
		return templateID;
	}
	
	public TS buildEffectiveTime(Date d) {
		TS effectiveTime = DatatypesFactory.eINSTANCE.createTS();
		SimpleDateFormat s = new SimpleDateFormat("yyyyMMddhhmmss");
		String creationDate = s.format(d);
		String timeOffset = d.getTimezoneOffset() + "";
		timeOffset = timeOffset.replace("-", "-0");
		effectiveTime.setValue(creationDate + timeOffset);
		return effectiveTime;
	}
	
	public IVL_TS buildEffectiveTimeinIVL(Date d, Date d1) {
		IVL_TS effectiveTime = DatatypesFactory.eINSTANCE.createIVL_TS();
		String creationDate = this.s.format(d);
		IVXB_TS low = DatatypesFactory.eINSTANCE.createIVXB_TS();
		low.setValue(creationDate);
		effectiveTime.setLow(low);
		IVXB_TS high = DatatypesFactory.eINSTANCE.createIVXB_TS();
		if (d1 != null) {
			high.setValue(this.s.format(d1));
		}
		
		effectiveTime.setHigh(high);
		return effectiveTime;
	}

	public StringBuilder buildSectionHeader(String ... elements) {
		StringBuilder builder = new StringBuilder();

		builder.append("<table border=\"1\" width=\"100%\">");
		builder.append("<thead>");
		builder.append("<tr>");

		for (String element : elements) {
			builder.append("<th>").append(element).append("</th>");
		}

		builder.append("</tr>");
		builder.append("</thead>");
		builder.append("<tbody>");

		return builder;
	}

	public String buildSectionContent(String ... elements) {
		StringBuilder builder = new StringBuilder();

		builder.append("<tr>");

		for (String element : elements) {
			builder.append("<td>").append(element).append("</td>");
		}

		builder.append("</tr>");

		return builder.toString();
	}

	public String buildSectionFooter() {
		StringBuilder builder = new StringBuilder();

		builder.append("</tbody>");
		builder.append("</table>");

		return builder.toString();
	}
}
