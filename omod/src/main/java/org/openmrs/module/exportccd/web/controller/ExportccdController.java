/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.exportccd.web.controller;

import org.openhealthtools.mdht.uml.cda.ccd.ContinuityOfCareDocument;
import org.openmrs.module.exportccd.api.PatientSummaryExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller("exportccd.ExportccdController")
@RequestMapping(value = "/rest/exportccd/", produces = MediaType.APPLICATION_JSON_VALUE)
public class ExportccdController {
	
	private final String NEW_LINE = "</br>";
	
	private final String H1_START = "<h1 style=\"text-align: center;\">";
	
	private final String H1_END = "</h1>";
	
	private final String H2_START = "<h2 style=\"text-align: center;\">";
	
	private final String H2_END = "</h2>";
	
	@Autowired
	PatientSummaryExportService patientSummaryExportService;
	
	@ResponseBody
	@RequestMapping(value = "/ccd/{patientECID}", method = RequestMethod.GET)
	public ResponseEntity<String> getCCD(@PathVariable String patientECID) {
		ContinuityOfCareDocument ccd = patientSummaryExportService.produceCCD(patientECID);

		String response = wrapSectionsToString(ccd);

		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-Type", "text/html; charset=utf-8");

		return new ResponseEntity<>(response, responseHeaders, HttpStatus.OK);
	}
	
	private String wrapSectionsToString(ContinuityOfCareDocument ccd) {
		
		String response = "<html><body style=\"font-family: monospace\">";
		
		response += ccd.getFamilyHistorySection() != null ? H1_START + ccd.getFamilyHistorySection().getTitle().getText()
		        + H1_END : "";
		response += ccd.getFamilyHistorySection() != null ? ccd.getFamilyHistorySection().getText().getText() + NEW_LINE
		        + NEW_LINE : "";
		
		response += ccd.getEncountersSection() != null ? H1_START + ccd.getEncountersSection().getTitle().getText() + H1_END
		        + H2_START + "(dernier 6 mois et première visite)" + H2_END : "";
		response += ccd.getEncountersSection() != null ? ccd.getEncountersSection().getText().getText() + NEW_LINE
		        + NEW_LINE : "";
		
		response += ccd.getProblemSection() != null ? H1_START + ccd.getProblemSection().getTitle().getText() + H1_END
		        + H2_START + "Diagnostics médicaux" + H2_END : "";
		response += ccd.getProblemSection() != null ? ccd.getProblemSection().getText().getText() + NEW_LINE + NEW_LINE : "";
		
		response += ccd.getSocialHistorySection() != null ? H1_START + ccd.getSocialHistorySection().getTitle().getText()
		        + H1_END : "";
		response += ccd.getSocialHistorySection() != null ? ccd.getSocialHistorySection().getText().getText() + NEW_LINE
		        + NEW_LINE : "";
		
		//		response += ccd.getAlertsSection() != null ? H1_START + ccd.getAlertsSection().getTitle().getText() + H1_END : "";
		//		response += ccd.getAlertsSection() != null ? ccd.getAlertsSection().getText().getText() + NEW_LINE + NEW_LINE : "";
		
		response += ccd.getMedicationsSection() != null ? H1_START + ccd.getMedicationsSection().getTitle().getText()
		        + H1_END : "";
		response += ccd.getMedicationsSection() != null ? ccd.getMedicationsSection().getText().getText() + NEW_LINE
		        + NEW_LINE : "";
		
		//
		//		response += ccd.getProceduresSection() != null ? H1_START + ccd.getProceduresSection().getTitle().getText() + H1_END
		//		        : "";
		//		response += ccd.getProceduresSection() != null ? ccd.getProceduresSection().getText().getText() + NEW_LINE + NEW_LINE : "";
		//
		//		response += ccd.getPlanOfCareSection() != null ? H1_START + ccd.getPlanOfCareSection().getTitle().getText() + H1_END
		//		        : "";
		//		response += ccd.getPlanOfCareSection() != null ? ccd.getPlanOfCareSection().getText().getText() + NEW_LINE + NEW_LINE : "";
		//
		//		response += ccd.getImmunizationsSection() != null ? H1_START + ccd.getImmunizationsSection().getTitle().getText()
		//		        + H1_END : "";
		//		response += ccd.getImmunizationsSection() != null ? ccd.getImmunizationsSection().getText().getText() + NEW_LINE + NEW_LINE
		//		        : "";
		
		response += ccd.getVitalSignsSection() != null ? H1_START + ccd.getVitalSignsSection().getTitle().getText() + H1_END
		        : "";
		response += ccd.getVitalSignsSection() != null ? ccd.getVitalSignsSection().getText().getText() + NEW_LINE
		        + NEW_LINE : "";
		
		//		response += ccd.getMedicalEquipmentSection() != null ? H1_START
		//		        + ccd.getMedicalEquipmentSection().getTitle().getText() + H1_END : "";
		//		response += ccd.getMedicalEquipmentSection() != null ? ccd.getMedicalEquipmentSection().getText().getText()
		//		        + NEW_LINE + NEW_LINE : "";
		//
		//		response += ccd.getFunctionalStatusSection() != null ? H1_START
		//		        + ccd.getFunctionalStatusSection().getTitle().getText() + H1_END : "";
		//		response += ccd.getFunctionalStatusSection() != null ? ccd.getFunctionalStatusSection().getText().getText()
		//		        + NEW_LINE + NEW_LINE : "";
		//
		//		response += ccd.getAdvanceDirectivesSection() != null ? H1_START
		//		        + ccd.getAdvanceDirectivesSection().getTitle().getText() + H1_END : "";
		//		response += ccd.getAdvanceDirectivesSection() != null ? ccd.getAdvanceDirectivesSection().getText().getText()
		//		        + NEW_LINE + NEW_LINE : "";
		//
		//		response += ccd.getPayersSection() != null ? H1_START + ccd.getPayersSection().getTitle().getText() + H1_END : "";
		//		response += ccd.getPayersSection() != null ? ccd.getPayersSection().getText().getText() + NEW_LINE + NEW_LINE : "";
		//
		//		response += ccd.getPurposeSection() != null ? H1_START + ccd.getPurposeSection().getTitle().getText() + H1_END : "";
		//		response += ccd.getPurposeSection() != null ? ccd.getPurposeSection().getText().getText() + NEW_LINE + NEW_LINE : "";
		
		response += ccd.getResultsSection() != null ? H1_START + ccd.getResultsSection().getTitle().getText() + H1_END : "";
		response += ccd.getResultsSection() != null ? ccd.getResultsSection().getText().getText() + NEW_LINE + NEW_LINE : "";
		
		return response + "</body></html>";
	}
}
