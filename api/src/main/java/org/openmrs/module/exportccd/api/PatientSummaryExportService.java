//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.openmrs.module.exportccd.api;

import java.util.List;
import org.openhealthtools.mdht.uml.cda.ccd.ContinuityOfCareDocument;
import org.openmrs.Concept;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.api.db.DAOException;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface PatientSummaryExportService extends OpenmrsService {
	
	ContinuityOfCareDocument produceCCD(int var1);
	
	boolean saveConceptAsCCDSections(List<Integer> var1, String var2) throws DAOException, APIException;
	
	List<Concept> getConceptByCategory(String var1);
	
	boolean deleteConceptsByCategory(List<Concept> var1, String var2);
}
