package com.banfftech.reactodata.odata;

import org.apache.olingo.commons.api.edm.provider.*;

import java.util.*;

public class EdmService {
	private String namespace;
    private List<CsdlComplexType> complexTypes = new ArrayList<CsdlComplexType>();
    private List<CsdlEnumType> enumTypes = new ArrayList<CsdlEnumType>();
	private Map<String, CsdlComplexType> complexTypeMap = new HashMap<String, CsdlComplexType>(); // key是ComplexType的name
	private Map<String, CsdlEnumType> enumTypeMap = new HashMap<String, CsdlEnumType>(); // key是EnumType的name
    private Map<String, CsdlEntityType> entityTypeMap = new HashMap<String, CsdlEntityType>(); // key是EntityType的name
	private Map<String, CsdlTerm> termMap = new HashMap<String, CsdlTerm>(); // key是Term的name
	private Map<String, CsdlEntitySet> entitySetMap = new HashMap<String, CsdlEntitySet>(); // key是EntitySet的name
    private List<CsdlFunction> functions = new ArrayList<CsdlFunction>();
    private Map<String, CsdlFunction> functionMap = new HashMap<String, CsdlFunction>(); // key是Function的name
    private List<CsdlAction> actions = new ArrayList<CsdlAction>();
    private Map<String, CsdlAction> actionMap = new HashMap<String, CsdlAction>(); // key是Action的name
    private Map<String, CsdlFunctionImport> functionImportMap = new HashMap<String, CsdlFunctionImport>(); // key是FunctionImport的name
    private Map<String, CsdlActionImport> actionImportMap = new HashMap<String, CsdlActionImport>(); // key是ActionImport的name
    private Map<String, CsdlSingleton> singletonMap = new HashMap<String, CsdlSingleton>(); // key是ActionImport的name
    private List<CsdlAnnotations> annotationses = new ArrayList<CsdlAnnotations>();
    private Map<String, Map<String, CsdlAnnotations>> annotationsMap = new HashMap<String, Map<String, CsdlAnnotations>>(); // key是Annotations的Target, Qualifier
	private List<String> mainEntityTypes = new ArrayList<>();

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public List<String> getMainEntityTypes() {
		return mainEntityTypes;
	}

	public void setMainEntityTypes(List<String> mainEntityTypes) {
		this.mainEntityTypes = mainEntityTypes;
	}

	public List<CsdlAnnotations> getAnnotationses() {
		return annotationses;
	}
	public void addAnnotations(CsdlAnnotations annotations) {
    	if (targetExists(annotations)) {
    		return;
		}
		this.annotationses.add(annotations);
		Map<String, CsdlAnnotations> targetedAnnotations = annotationsMap.get(annotations.getTarget());
		if (targetedAnnotations == null) {
			targetedAnnotations = new HashMap<String, CsdlAnnotations>();
			annotationsMap.put(annotations.getTarget(), targetedAnnotations);
		}
		targetedAnnotations.put(annotations.getQualifier(), annotations);
	}
	public CsdlAnnotations getAnnotations(String target, String qualifier) {
		if (this.annotationsMap.get(target) != null) {
			return this.annotationsMap.get(target).get(qualifier);
		}
		return null;
	}
	public Map<String, CsdlComplexType> getComplexTypeMap() {
		return complexTypeMap;
    }
    public void addComplexType(CsdlComplexType complexType) {
		complexTypes.add(complexType);
		complexTypeMap.put(complexType.getName(), complexType);
    }
    public void addEnumType(CsdlEnumType enumType) {
    		enumTypes.add(enumType);
		enumTypeMap.put(enumType.getName(), enumType);
    }
    public Map<String, CsdlEnumType> getEnumTypeMap() {
    	return this.enumTypeMap;
    }
    public Map<String, CsdlEntityType> getEntityTypeMap() {
    		return entityTypeMap;
    }
    public Collection<CsdlTerm> getTerms() {
		return termMap.values();
	}
	public void addTerm(CsdlTerm term) {
//    	if (termMap.containsKey(term.getName())) {
//    		return;
//    	}
    	termMap.put(term.getName(), term);
	}
	public Map<String, CsdlTerm> getTermMap() {
		return termMap;
	}
	public void setTermMap(Map<String, CsdlTerm> termMap) {
		this.termMap = termMap;
	}
	public CsdlTerm getTerm(String name) {
		return this.termMap.get(name);
	}
	public void addEntityType(CsdlEntityType entityType) {
    	// 同一个对象entityType，同时放入List，快速给到schema用，放入Map，快速找到对应的 entity
//    	if (entityTypeMap.containsKey(entityType.getName())) {
//    		return;
//    	}
    	entityTypeMap.put(entityType.getName(), entityType);
    }
    
    public void addEntitySet(CsdlEntitySet entitySet) {
    		entitySetMap.put(entitySet.getName(), entitySet);
    }
    public void addFunction(CsdlFunction function) {
		this.functions.add(function);
		functionMap.put(function.getName(), function);
    }
    public void addFunctionImport(CsdlFunctionImport functionImport) {
    		functionImportMap.put(functionImport.getName(), functionImport);
    }
    public void addAction(CsdlAction action) {
		this.actions.add(action);
		actionMap.put(action.getName(), action);
    }
    public void addActionImport(CsdlActionImport actionImport) {
    		actionImportMap.put(actionImport.getName(), actionImport);
    }
    public void addSingleton(CsdlSingleton singleton) {
		singletonMap.put(singleton.getName(), singleton);
    }
    
    public List<CsdlComplexType> getComplexTypes() {
		return complexTypes;
    }
    
    public List<CsdlEnumType> getEnumTypes() {
		return enumTypes;
    }
    public Collection<CsdlEntityType> getEntityTypes() {
    		return entityTypeMap.values();
    }
    public Collection<CsdlEntitySet> getEntitySets() {
    	return entitySetMap.values();
    }
    public List<CsdlFunction> getFunctions() {
		return functions;
    }
    public List<CsdlAction> getActions() {
		return actions;
    }
    public Collection<CsdlFunctionImport> getFunctionImports() {
		return functionImportMap.values();
    }
    public Collection<CsdlActionImport> getActionImports() {
		return actionImportMap.values();
    }
    public Collection<CsdlSingleton> getSingletons() {
		return singletonMap.values();
    }
    
    public CsdlComplexType getComplexType(String complexTypeName) {
		if (complexTypeMap.get(complexTypeName) != null) {
			return complexTypeMap.get(complexTypeName);
		} else {
			return null;
		}
    }
    
    public CsdlEnumType getEnumType(String enumTypeName) {
		if (enumTypeMap.get(enumTypeName) != null) {
			return enumTypeMap.get(enumTypeName);
		} else {
			return null;
		}
    }
    public CsdlEntityType getEntityType(String entityTypeName) {
    		if (entityTypeMap.get(entityTypeName) != null) {
    			return entityTypeMap.get(entityTypeName);
    		} else {
    			return null;
    		}
    }
    
    public CsdlEntitySet getEntitySet(String entitySetName) {
		if (entitySetMap.get(entitySetName) != null) {
			return entitySetMap.get(entitySetName);
		} else {
			return null;
		}
    }

    public CsdlAction getAction(String actionName) {
    		if (actionMap.get(actionName) != null) {
    			return actionMap.get(actionName);
    		} else {
    			return null;
    		}
    }
    
	public CsdlFunction getFunction(String functionName)
	{
		return functionMap.get(functionName);
	}
    
    public CsdlActionImport getActionImport(String actionImportName) {
    		return actionImportMap.get(actionImportName);
    }
    
    public CsdlFunctionImport getFunctionImport(String functionImportName) {
		return functionImportMap.get(functionImportName);
    }
    
    public CsdlSingleton getSingleton(String singletonName) {
		if (singletonMap.get(singletonName) != null) {
			return singletonMap.get(singletonName);
		} else {
			return null;
		}
    }

	private boolean targetExists(CsdlAnnotations annotations) {
    	String annotationsTarget = annotations.getTarget();
		for (CsdlAnnotations csdlAnnotations:this.annotationses) {
			if (annotationsTarget.equals(csdlAnnotations.getTarget())) {
				return true;
			}
		}
		return false;
	}
}
