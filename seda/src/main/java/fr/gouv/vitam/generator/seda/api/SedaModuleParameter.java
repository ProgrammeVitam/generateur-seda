package fr.gouv.vitam.generator.seda.api;

/**
 * List of parameters used by Seda Module 
 */

public enum SedaModuleParameter {
    // Question: name = lowercase ou UpperCamelCase comme dans le SEDA ?
    BINARYDATAOBJECT("binarydataobject");
    
    private String name;

    private SedaModuleParameter(String name) {
        this.name = name;
    }

    /**
     *
     * @return the Module Parameter name
     */
    public String getName() {
        return name;
    }
    
}
