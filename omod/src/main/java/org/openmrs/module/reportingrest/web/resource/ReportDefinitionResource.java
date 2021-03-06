/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.reportingrest.web.resource;

import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reportingrest.web.controller.ReportingRestController;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;


import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.definition.DefinitionContext;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.api.Searchable;
import org.openmrs.module.webservices.rest.web.resource.impl.MetadataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

/**
 * {@link Resource} for {@link ReportDefinition}s, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + ReportingRestController.REPORTING_REST_NAMESPACE + "/reportDefinition",
        supportedClass = ReportDefinition.class, supportedOpenmrsVersions = {"1.8.*", "1.9.*"})
public class ReportDefinitionResource extends MetadataDelegatingCrudResource<ReportDefinition> implements Searchable {
	
	/**
	 * @return the definition type that this resource wraps
	 */
	public Class<ReportDefinition> getDefinitionType() {
		return ReportDefinition.class;
	}
	
	/**
	 * @see BaseDelegatingResource#newDelegate()
	 */
	@Override
    public ReportDefinition newDelegate() {
		try {
			return getDefinitionType().newInstance();
		}
		catch (Exception e) {
			throw new RuntimeException("Unable to create new " + getDefinitionType());
		}
	}
	
	/**
	 * @see BaseDelegatingResource#getByUniqueId(String)
	 */
	@Override
	public ReportDefinition getByUniqueId(String uuid) {
		return DefinitionContext.getDefinitionByUuid(getDefinitionType(), uuid); 
	}
	
	/**
	 * @see BaseDelegatingResource#save(Object)
	 */
	@Override
	public ReportDefinition save(ReportDefinition definition) {
		return DefinitionContext.saveDefinition(definition);
	}

	/**
	 * @see BaseDelegatingResource#delete(Object, String, RequestContext)
	 */
	@Override
	public void delete(ReportDefinition definition, String reason, RequestContext context) throws ResponseException {
		definition.setRetireReason(reason);
		DefinitionContext.retireDefinition(definition);
	}
	
	/**
	 * @see BaseDelegatingResource#purge(Object, RequestContext)
	 */
	@Override
	public void purge(ReportDefinition definition, RequestContext context) throws ResponseException {
		DefinitionContext.purgeDefinition(getDefinitionType(), definition.getUuid());
	}

	/**
	 * @see BaseDelegatingResource#getRepresentationDescription(Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
        DelegatingResourceDescription description = null;		
		if (rep instanceof DefaultRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("name");
			description.addProperty("description");
			description.addProperty("parameters");
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
		} 
		else if (rep instanceof FullRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("name");
			description.addProperty("description");
			description.addProperty("parameters");
			description.addProperty("baseCohort");
			description.addProperty("dataSetDefintion");
			description.addProperty("types");
			description.addSelfLink();
		}
		return description;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doSearch(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected PageableResult doSearch(RequestContext context) {
        String query = context.getParameter("q");
		List<ReportDefinition> results = DefinitionContext.getDefinitionService(getDefinitionType()).getDefinitions(query, false);
		return new NeedsPaging<ReportDefinition>(results, context);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doGetAll(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	public PageableResult doGetAll(RequestContext context) throws ResponseException {
		return new NeedsPaging<ReportDefinition>(DefinitionContext.getDefinitionService(getDefinitionType()).getAllDefinitions(false), context);
	}
	
	
	@PropertyGetter("baseCohort")
	public String getCohort(ReportDefinition reportdef) {
		try{
			return reportdef.getBaseCohortDefinition().getParameterizable().getName();
		}catch(Exception e){
			return "All Patients";
		}
	}
	
	@PropertyGetter("dataSetDefintion")
	public Map<String,String> getDataSetDef(ReportDefinition reportdef) {
		try{
			Map<String, Mapped<? extends DataSetDefinition>> testt=reportdef.getDataSetDefinitions();
			Map<String,String> myMap=new HashMap<String,String>();
			for (Entry<String, Mapped<? extends DataSetDefinition>> e : testt.entrySet()){
				myMap.put(e.getKey(), e.getValue().getParameterizable().getName());
			}
			return myMap;
		}catch(Exception e){
			return null;
		}
	}
	
	@PropertyGetter("types")
	public Class<ReportDefinition> getTypes(ReportDefinition reportdef) {
		return DefinitionContext.getDefinitionService(getDefinitionType()).getDefinitionType();
	}
	
	/*@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();		
		description.addRequiredProperty("name");
		description.addProperty("description");
		description.addProperty("dataSetDefintion");		
		return description;
	}
	
	@PropertySetter("dataSetDefintion")
	public static void setDescriptions(ReportDefinition reportdef, List<LinkedHashMap> a) {
		String key=(String)a.get(0).get("key");
		String uuid=(String)a.get(0).get("uuid");
		
		DataSetDefinition datadef=DefinitionContext.getDefinitionByUuid(DataSetDefinition.class, uuid);
		Mapped<DataSetDefinition> mapped=new Mapped<DataSetDefinition>();
		mapped.setParameterizable(datadef);
		Map<String, Mapped<? extends DataSetDefinition>> m=reportdef.getDataSetDefinitions();
		m.put(key, mapped);
	}*/
	
	
	/**
	 * @param delegate
	 * @return the URI for the given delegate object
	 */
	@SuppressWarnings("unchecked")
	@Override
	public String getUri(Object delegate) {
		if (delegate == null)
			return "";
		
		Resource res = getClass().getAnnotation(Resource.class);
		if (res != null) {
			String url = RestConstants.URI_PREFIX + res.name() + "/" + getUniqueId((ReportDefinition) delegate);
			url = url.replace("/rest/", "/reporting/"); // hacky :-(
			return url;
		}
		throw new RuntimeException(getClass() + " needs a @Resource or @SubResource annotation");
		
	}
}
