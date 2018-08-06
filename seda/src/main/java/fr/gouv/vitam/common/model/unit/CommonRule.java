package fr.gouv.vitam.common.model.unit;

import fr.gouv.culture.archivesdefrance.seda.v2.RuleIdType;

import java.util.List;

/**
 * Common rule Interface
 */
public interface CommonRule extends CommonRuleBase {

    /**
     * Gets the value of the refNonRuleId property.
     * 
     * @return refNonRuleId
     */
    List<RuleIdType> getRefNonRuleId();
}
