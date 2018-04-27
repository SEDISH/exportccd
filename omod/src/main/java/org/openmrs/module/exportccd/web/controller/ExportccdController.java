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
	
	@Autowired
	PatientSummaryExportService patientSummaryExportService;
	
	@ResponseBody
	@RequestMapping(value = "/ccd/{patientId}", method = RequestMethod.GET)
	public ResponseEntity<String> getCCD(@PathVariable int patientId) {
		ContinuityOfCareDocument ccd = patientSummaryExportService.produceCCD(patientId);

		String response = wrapSectionsToString(ccd);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	private String wrapSectionsToString(ContinuityOfCareDocument ccd) {
		
		String response = "";
		
		response += ccd.getProblemSection() != null ? ccd.getProblemSection().getText().getText() : "";
		response += ccd.getFamilyHistorySection() != null ? ccd.getFamilyHistorySection().getText().getText() : "";
		response += ccd.getSocialHistorySection() != null ? ccd.getSocialHistorySection().getText().getText() : "";
		response += ccd.getAlertsSection() != null ? ccd.getAlertsSection().getText().getText() : "";
		response += ccd.getMedicationsSection() != null ? ccd.getMedicationsSection().getText().getText() : "";
		response += ccd.getResultsSection() != null ? ccd.getResultsSection().getText().getText() : "";
		response += ccd.getProceduresSection() != null ? ccd.getProceduresSection().getText().getText() : "";
		response += ccd.getEncountersSection() != null ? ccd.getEncountersSection().getText().getText() : "";
		response += ccd.getPlanOfCareSection() != null ? ccd.getPlanOfCareSection().getText().getText() : "";
		response += ccd.getImmunizationsSection() != null ? ccd.getImmunizationsSection().getText().getText() : "";
		response += ccd.getVitalSignsSection() != null ? ccd.getVitalSignsSection().getText().getText() : "";
		response += ccd.getMedicalEquipmentSection() != null ? ccd.getMedicalEquipmentSection().getText().getText() : "";
		response += ccd.getFunctionalStatusSection() != null ? ccd.getFunctionalStatusSection().getText().getText() : "";
		response += ccd.getAdvanceDirectivesSection() != null ? ccd.getAdvanceDirectivesSection().getText().getText() : "";
		response += ccd.getPayersSection() != null ? ccd.getPayersSection().getText().getText() : "";
		response += ccd.getPurposeSection() != null ? ccd.getPurposeSection().getText().getText() : "";
		
		return response;
	}
}
