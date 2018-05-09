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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openhealthtools.mdht.uml.cda.ccd.ContinuityOfCareDocument;
import org.openmrs.module.exportccd.api.PatientSummaryExportService;
import org.springframework.beans.factory.annotation.Autowired;
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
	
	private final Log LOG = LogFactory.getLog(this.getClass());
	
	private final String NEW_LINE = "</br>";
	
	@Autowired
	PatientSummaryExportService patientSummaryExportService;
	
	@ResponseBody
	@RequestMapping(value = "/ccd/{patientECID}", method = RequestMethod.GET)
	public ResponseEntity<String> getCCD(@PathVariable String patientECID) {
		ContinuityOfCareDocument ccd = patientSummaryExportService.produceCCD(patientECID);

		String response = wrapSectionsToString(ccd);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	private String wrapSectionsToString(ContinuityOfCareDocument ccd) {
		
		String response = "";
		
		response += ccd.getProblemSection() != null ? ccd.getProblemSection().getTitle().getText() : "";
		response += ccd.getProblemSection() != null ? ccd.getProblemSection().getText().getText() + NEW_LINE : "";
		
		response += ccd.getFamilyHistorySection() != null ? ccd.getFamilyHistorySection().getTitle().getText() + NEW_LINE
		        : "";
		response += ccd.getFamilyHistorySection() != null ? ccd.getFamilyHistorySection().getText().getText() + NEW_LINE
		        : "";
		
		response += ccd.getSocialHistorySection() != null ? ccd.getSocialHistorySection().getTitle().getText() : "";
		response += ccd.getSocialHistorySection() != null ? ccd.getSocialHistorySection().getText().getText() + NEW_LINE
		        : "";
		
		response += ccd.getAlertsSection() != null ? ccd.getAlertsSection().getTitle().getText() : "";
		response += ccd.getAlertsSection() != null ? ccd.getAlertsSection().getText().getText() + NEW_LINE : "";
		
		response += ccd.getMedicationsSection() != null ? ccd.getMedicationsSection().getTitle().getText() : "";
		response += ccd.getMedicationsSection() != null ? ccd.getMedicationsSection().getText().getText() + NEW_LINE : "";
		
		response += ccd.getResultsSection() != null ? ccd.getResultsSection().getTitle().getText() : "";
		response += ccd.getResultsSection() != null ? ccd.getResultsSection().getText().getText() + NEW_LINE : "";
		
		response += ccd.getProceduresSection() != null ? ccd.getProceduresSection().getTitle().getText() : "";
		response += ccd.getProceduresSection() != null ? ccd.getProceduresSection().getText().getText() + NEW_LINE : "";
		
		response += ccd.getEncountersSection() != null ? ccd.getEncountersSection().getTitle().getText() : "";
		response += ccd.getEncountersSection() != null ? ccd.getEncountersSection().getText().getText() + NEW_LINE : "";
		
		response += ccd.getPlanOfCareSection() != null ? ccd.getPlanOfCareSection().getTitle().getText() : "";
		response += ccd.getPlanOfCareSection() != null ? ccd.getPlanOfCareSection().getText().getText() + NEW_LINE : "";
		
		response += ccd.getImmunizationsSection() != null ? ccd.getImmunizationsSection().getTitle().getText() : "";
		response += ccd.getImmunizationsSection() != null ? ccd.getImmunizationsSection().getText().getText() + NEW_LINE
		        : "";
		
		response += ccd.getVitalSignsSection() != null ? ccd.getVitalSignsSection().getTitle().getText() : "";
		response += ccd.getVitalSignsSection() != null ? ccd.getVitalSignsSection().getText().getText() + NEW_LINE : "";
		
		response += ccd.getMedicalEquipmentSection() != null ? ccd.getMedicalEquipmentSection().getTitle().getText() : "";
		response += ccd.getMedicalEquipmentSection() != null ? ccd.getMedicalEquipmentSection().getText().getText()
		        + NEW_LINE : "";
		
		response += ccd.getFunctionalStatusSection() != null ? ccd.getFunctionalStatusSection().getTitle().getText() : "";
		response += ccd.getFunctionalStatusSection() != null ? ccd.getFunctionalStatusSection().getText().getText()
		        + NEW_LINE : "";
		
		response += ccd.getAdvanceDirectivesSection() != null ? ccd.getAdvanceDirectivesSection().getTitle().getText() : "";
		response += ccd.getAdvanceDirectivesSection() != null ? ccd.getAdvanceDirectivesSection().getText().getText()
		        + NEW_LINE : "";
		
		response += ccd.getPayersSection() != null ? ccd.getPayersSection().getTitle().getText() : "";
		response += ccd.getPayersSection() != null ? ccd.getPayersSection().getText().getText() + NEW_LINE : "";
		
		response += ccd.getPurposeSection() != null ? ccd.getPurposeSection().getTitle().getText() : "";
		response += ccd.getPurposeSection() != null ? ccd.getPurposeSection().getText().getText() + NEW_LINE : "";
		
		return response;
	}
}
