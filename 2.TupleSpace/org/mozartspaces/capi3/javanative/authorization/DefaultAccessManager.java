/**
 * MozartSpaces - Java implementation of Extensible Virtual Shared Memory (XVSM)
 * Copyright 2009-2013 Space Based Computing Group, eva Kuehn, E185/1, TU Vienna
 * Visit http://www.mozartspaces.org for more information.
 *
 * MozartSpaces is free software: you can redistribute it and/or
 * modify it under the terms of version 3 of the GNU Affero General
 * Public License as published by the Free Software Foundation.
 *
 * MozartSpaces is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General
 * Public License along with MozartSpaces. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.mozartspaces.capi3.javanative.authorization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mozartspaces.capi3.AuthTargetCoordinator;
import org.mozartspaces.capi3.Capi3Exception;
import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.capi3.IsolationLevel;
import org.mozartspaces.capi3.LocalContainerReference;
import org.mozartspaces.capi3.OperationType;
import org.mozartspaces.capi3.Selector;
import org.mozartspaces.capi3.javanative.DefaultCapi3Native;
import org.mozartspaces.capi3.javanative.coordination.DefaultAuthTargetCoordinator;
import org.mozartspaces.capi3.javanative.coordination.NativeSelector;
import org.mozartspaces.capi3.javanative.isolation.NativeSubTransaction;
import org.mozartspaces.capi3.javanative.operation.NativeContainer;
import org.mozartspaces.capi3.javanative.operation.NativeEntry;
import org.mozartspaces.core.MzsConstants.Selecting;
import org.mozartspaces.core.RequestContext;
import org.mozartspaces.core.authorization.AuthorizationRule;
import org.mozartspaces.core.authorization.Condition;
import org.mozartspaces.core.authorization.ConditionQuery;
import org.mozartspaces.core.authorization.ContainerAction;
import org.mozartspaces.core.authorization.ContextAwareSelector;
import org.mozartspaces.core.authorization.Effect;
import org.mozartspaces.core.authorization.NamedValue;
import org.mozartspaces.core.authorization.RequestAuthTarget;
import org.mozartspaces.core.authorization.ScopeQuery;
import org.mozartspaces.core.authorization.Subject;
import org.mozartspaces.core.security.RequestContextUtils;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * Access Manager that checks permissions for container operations.
 *
 * @author Stefan Crass
 * @author Tobias Doenz
 */
public final class DefaultAccessManager implements NativeAccessManager {

    private static Logger log = LoggerFactory.get();

    private final DefaultCapi3Native capi3;

    private NativeContainer policyC;

    /**
     * @param capi3
     *            the DefaultCapi3Native instance used for accessing containers
     */
    public DefaultAccessManager(final DefaultCapi3Native capi3) {
        this.capi3 = capi3;
    }

    @Override
    public void setPolicyContainer(final NativeContainer policyC) {
        this.policyC = policyC;
    }

    @Override
    public AuthorizationResult checkPermissions(final NativeContainer container, final OperationType opType,
            final NativeSubTransaction stx, final RequestContext context) {

        if (!RequestContextUtils.isAuthorizationRequired(context)) {
            log.debug("Authorization not required, allowing access");
            return new AuthorizationResult(AuthorizationType.PERMITTED);
        }

        if (this.policyC == null) {
            throw new IllegalStateException("Policy Container must be set.");
        }

        Set<NamedValue> attributes = extractRequestContextAttributes(context);
        Subject subject = new Subject(attributes);

        ContainerAction action;
        switch (opType) {
        case WRITE:
            action = ContainerAction.WRITE;
            break;
        case READ:
            action = ContainerAction.READ;
            break;
        case TAKE:
            action = ContainerAction.TAKE;
            break;
        default:
            throw new IllegalArgumentException("Invalid operation type");
        }

        RequestAuthTarget target = new RequestAuthTarget(subject, action, container.getReference());

        // read rules from policy container
        List<AuthorizationRule> matchingRules;
        try {
            matchingRules = readRulesFromPolicyContainer(target, stx);
        } catch (Capi3Exception ex) {
            log.warn("Access control rules could not be read.", ex);
            return new AuthorizationResult(AuthorizationType.INDETERMINATE);
        }
        log.debug("{} matching rule(s)", matchingRules.size());

        AuthorizationResult result = this.combineRuleDecisions(matchingRules, container, opType, stx, context);
        log.debug("Authorization result: {}", result);

        return result;
    }

    @SuppressWarnings("unchecked")
    private Set<NamedValue> extractRequestContextAttributes(final RequestContext context) {
        Set<NamedValue> attributes = null;
        Object attributesProperty = context.getRequestProperty(RequestContextUtils.ATTRIBUTES_PROPERTY_KEY);
        if (attributesProperty != null && attributesProperty instanceof Set) {
            attributes = (Set<NamedValue>) attributesProperty;
        } else {
            log.debug("User attributes not found in context");
        }
        Object extraAttributesProperty = context
            .getRequestProperty(RequestContextUtils.EXTRA_ATTRIBUTES_PROPERTY_KEY);
        if (extraAttributesProperty != null && extraAttributesProperty instanceof Set) {
            log.debug("Using extra attributes from context");
            Set<NamedValue> origAttributes = attributes;
            attributes = new HashSet<NamedValue>();
            if (origAttributes != null) {
                attributes.addAll(origAttributes);
            }
            attributes.addAll((Set<NamedValue>) extraAttributesProperty);
        }
        return attributes;
    }

    private List<AuthorizationRule> readRulesFromPolicyContainer(final RequestAuthTarget target,
            final NativeSubTransaction stx) throws Capi3Exception {
        log.debug("Reading rules from policy container for request: {}", target);
        // TODO read policies from policy container (with referenced rules) instead of only rules
        List<NativeSelector<?>> selectors = new ArrayList<NativeSelector<?>>();
        selectors.add(DefaultAuthTargetCoordinator.newSelector(AuthTargetCoordinator.DEFAULT_NAME, Selecting.COUNT_ALL,
                target));
        List<NativeEntry> ruleEntries = this.policyC.selectEntries(selectors, IsolationLevel.READ_COMMITTED, stx, null);

        List<AuthorizationRule> matchingRules = new ArrayList<AuthorizationRule>();
        for (NativeEntry ruleEntry : ruleEntries) {
            if (ruleEntry.getData() instanceof AuthorizationRule) {
                AuthorizationRule rule = (AuthorizationRule) ruleEntry.getData();
                matchingRules.add(rule);
            } else {
                throw new IllegalStateException("Illegal policy container content.");
            }
        }
        return matchingRules;
    }

    private List<NativeSelector<?>> computeSelectors(final List<ContextAwareSelector<?>> dynamicSels,
            final RequestContext context) throws Capi3Exception {
        List<Selector> selectors = new ArrayList<Selector>();
        for (ContextAwareSelector<?> ds : dynamicSels) {
            Selector sel = ds.computeSelector(context);
            if (sel != null) {
                selectors.add(sel);
            } else {
                // context-aware selectors could not be computed (e.g. due to missing context parameters)
                throw new Capi3Exception("Context-aware selectors could not be computed");
            }
        }
        return this.capi3.translateCommonSelectors(selectors);
    }

    private AuthorizationResult combineRuleDecisions(final List<AuthorizationRule> matchingRules,
            final NativeContainer container, final OperationType opType, final NativeSubTransaction stx,
            final RequestContext context) {

        if (matchingRules == null || matchingRules.isEmpty()) {
            return new AuthorizationResult(AuthorizationType.NOT_APPLICABLE);
        }

        boolean matchingRule = false;
        Map<NativeEntry, AuthorizationType> decision = new HashMap<NativeEntry, AuthorizationType>();

        // TODO use combination algorithm according to policy instead of implicit strategy
        // TODO examine error semantics
        for (AuthorizationRule rule : matchingRules) {
            log.debug("Evaluating rule: {}", rule);
            try {
                boolean conditionFulfilled = evaluateCondition(rule.getCondition(), context, stx);
                // log.debug("Condition fulfilled? {}", conditionFulfilled);
                if (!conditionFulfilled) {
                    continue;
                }
                matchingRule = true;

                AuthorizationType authType;
                if (rule.getEffect() == Effect.PERMIT) {
                    authType = AuthorizationType.PERMITTED;
                } else {
                    authType = AuthorizationType.DENIED;
                }

                // general decisions (no or empty scope)
                if (rule.getScope() == null) {
                    return new AuthorizationResult(authType);
                }
                List<ScopeQuery> scopeQueries = rule.getScope().getScopeQueries();
                if (scopeQueries == null || scopeQueries.isEmpty()) {
                    return new AuthorizationResult(authType);
                }

                // compute entry decisions
                log.debug("Evaluating {} scope queries", scopeQueries.size());
                for (ScopeQuery query : scopeQueries) { // scopes are currently combined via OR
                    List<NativeSelector<?>> selectors = this.computeSelectors(query.getSelectors(), context);
                    List<NativeEntry> scopeEntries = container.selectEntries(selectors, IsolationLevel.READ_COMMITTED,
                            stx, context);
                    for (NativeEntry affectedEntry : scopeEntries) {
                        if (!decision.containsKey(affectedEntry)) {
                            // combination algorithm: first applicable
                            decision.put(affectedEntry, authType);
                        }
                    }
                }
                // TODO semantic not correct with DENY rules (combination of rules)
            } catch (Capi3Exception ex) {
                log.warn("Evaluation of policy rules failed: {}", ex.toString());
                return new AuthorizationResult(AuthorizationType.INDETERMINATE);
            }

        }

        if (matchingRule) {
            return new AuthorizationResult(decision);
        } else {
            return new AuthorizationResult(AuthorizationType.NOT_APPLICABLE);
        }
    }

    private boolean evaluateCondition(final Condition condition, final RequestContext context,
            final NativeSubTransaction stx) throws Capi3Exception {
        if (condition == null) {
            return true;
        }
        List<ConditionQuery> conditionQueries = condition.getConditionQueries();
        if (conditionQueries == null) {
            return true;
        }
        log.debug("Evaluating {} condition queries", conditionQueries.size());
        for (ConditionQuery query : conditionQueries) { // conditions are currently combined via AND
            LocalContainerReference conditionCref = query.getContainer();
            List<NativeSelector<?>> selectors = computeSelectors(query.getSelectors(), context);
            NativeContainer conditionC = this.capi3.getContainerManager().getContainer(conditionCref);
            try {
                List<NativeEntry> conditionResult = conditionC.selectEntries(selectors, IsolationLevel.READ_COMMITTED,
                        stx, context);
                log.debug("{} result entries for condition query {}", conditionResult.size(), selectors);
                if (conditionResult.size() == 0) {
                    // condition query does not return at least one result entry
                    return false;
                }
            } catch (CountNotMetException ex) {
                // condition query has count requirement that is not met
                log.debug("Condition query {} failed: {}", selectors, ex.toString());
                return false;
            }
        }
        return true;
    }

}
