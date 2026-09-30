/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2026 Riverside Software
 * contact AT riverside DASH software DOT fr
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package eu.rssw.pct.elements;

import java.util.Collections;
import java.util.List;

import org.prorefactor.core.Pair;

/**
 * Result of method resolution containing the resolved method (if any) and diagnostic information
 * about candidates and ambiguities.
 */
public class MethodResolutionResult {

  /**
   * Reason why a method was considered as a candidate match.
   */
  public enum MatchReason {
    /** Exact match on all parameters */
    EXACT,
    /** Unknown data type used in call (e.g., null/?) */
    UNKNOWN_DATATYPE,
    /** Parameter mode difference (INPUT vs OUTPUT vs INPUT-OUTPUT) */
    PARAMETER_MODE_DIFFERENCE,
    /** Parameter datatype difference but compatible (e.g., CHAR vs LONGCHAR) */
    PARAMETER_TYPE_DIFFERENCE
  }

  /**
   * A candidate method that matched during resolution.
   */
  public static class Candidate {
    private final ITypeInfo typeInfo;
    private final IMethodElement method;
    private final List<MatchReason> reasons;

    public Candidate(ITypeInfo typeInfo, IMethodElement method, List<MatchReason> reasons) {
      this.typeInfo = typeInfo;
      this.method = method;
      this.reasons = Collections.unmodifiableList(reasons);
    }

    public ITypeInfo getTypeInfo() {
      return typeInfo;
    }

    public IMethodElement getMethod() {
      return method;
    }

    public List<MatchReason> getReasons() {
      return reasons;
    }

    public boolean hasReason(MatchReason reason) {
      return reasons.contains(reason);
    }
  }

  private final Pair<ITypeInfo, IMethodElement> resolvedMethod;
  private final List<Candidate> candidates;
  private final boolean ambiguous;
  private final String ambiguityReason;

  private MethodResolutionResult(Pair<ITypeInfo, IMethodElement> resolvedMethod, List<Candidate> candidates,
      boolean ambiguous, String ambiguityReason) {
    this.resolvedMethod = resolvedMethod;
    this.candidates = candidates == null ? Collections.emptyList() : Collections.unmodifiableList(candidates);
    this.ambiguous = ambiguous;
    this.ambiguityReason = ambiguityReason;
  }

  /**
   * Creates a result for a successful exact match.
   */
  public static MethodResolutionResult exactMatch(ITypeInfo typeInfo, IMethodElement method) {
    return new MethodResolutionResult(Pair.of(typeInfo, method),
        List.of(new Candidate(typeInfo, method, List.of(MatchReason.EXACT))), false, null);
  }

  /**
   * Creates a result for a successful compatible match with a single candidate.
   */
  public static MethodResolutionResult singleMatch(ITypeInfo typeInfo, IMethodElement method,
      List<MatchReason> reasons) {
    return new MethodResolutionResult(Pair.of(typeInfo, method),
        List.of(new Candidate(typeInfo, method, reasons)), false, null);
  }

  /**
   * Creates a result for a compatible match where multiple candidates existed but one was selected.
   */
  public static MethodResolutionResult resolvedWithCandidates(ITypeInfo typeInfo, IMethodElement method,
      List<Candidate> candidates) {
    return new MethodResolutionResult(Pair.of(typeInfo, method), candidates, false, null);
  }

  /**
   * Creates a result for an ambiguous match where no method could be selected.
   */
  public static MethodResolutionResult ambiguous(List<Candidate> candidates, String reason) {
    return new MethodResolutionResult(null, candidates, true, reason);
  }

  /**
   * Creates a result for when no matching method was found.
   */
  public static MethodResolutionResult notFound() {
    return new MethodResolutionResult(null, Collections.emptyList(), false, null);
  }

  /**
   * @return The resolved method pair (typeInfo, methodElement), or null if not resolved
   */
  public Pair<ITypeInfo, IMethodElement> getResolvedMethod() {
    return resolvedMethod;
  }

  /**
   * @return True if a method was successfully resolved
   */
  public boolean isResolved() {
    return resolvedMethod != null;
  }

  /**
   * @return True if the resolution was ambiguous (multiple candidates, none could be selected)
   */
  public boolean isAmbiguous() {
    return ambiguous;
  }

  /**
   * @return Description of why the resolution was ambiguous, or null if not ambiguous
   */
  public String getAmbiguityReason() {
    return ambiguityReason;
  }

  /**
   * @return List of candidate methods that matched (may be empty if no match found)
   */
  public List<Candidate> getCandidates() {
    return candidates;
  }

  /**
   * @return True if there were multiple candidates during resolution
   */
  public boolean hadMultipleCandidates() {
    return candidates.size() > 1;
  }

  /**
   * @return True if any candidate had an unknown datatype parameter
   */
  public boolean hasUnknownDatatypeCandidate() {
    return candidates.stream().anyMatch(c -> c.hasReason(MatchReason.UNKNOWN_DATATYPE));
  }
}
