# Egg Size Conversion Plan

## Summary

Recipes are authored with UK egg sizes. This feature converts UK egg size labels to regionally appropriate equivalents when rendering for non-UK audiences (starting with US).

## UK → US Mapping

| UK Size | UK Weight Range | US Equivalent | US Weight Range |
|---------|----------------|---------------|-----------------|
| Small   | under 53g      | Small         | 42.5–49.4g     |
| Medium  | 53–62.9g       | Large         | 56.7–63.7g     |
| Large   | 63–72.9g       | Extra large   | 63.8–70.8g     |

## Logic

1. **When to act**: The ingredient is egg-related (`"egg"`, `"egg white"`, `"egg yolk"`) AND a size word (`small`, `medium`, `large`) appears in the template text AND the target measuring system is US-based.
2. **What to do**: Replace the UK size word with the US equivalent from the mapping above.
3. **When to do nothing**: No size word present, target is Metric/Imperial (UK audience), or ingredient is not egg-related.

## Implementation Steps

### 1. Create `EggSizeConversion.kt`

**Location**: `library/src/commonMain/kotlin/com/gu/recipe/egg/EggSizeConversion.kt`

- Define an enum `UkEggSize` (SMALL, MEDIUM, LARGE) each with a midpoint weight in grams.
- Define an enum `EggRegion` (US — extensible to AU, CA, NZ later).
- Provide a `Map<EggRegion, Map<UkEggSize, String>>` holding the label mappings.
- Expose a function `convertEggSizeLabel(ukLabel: String, region: EggRegion): String?` that does a case-insensitive lookup and returns the target label, or null if no match (meaning no conversion needed or label unchanged).

### 2. Add post-processing in `ScaleRecipe.kt`

In `renderTemplate`, after joining all rendered parts:
- Check if any `QuantityPlaceholder` in the template has `ingredient` matching an egg-type.
- Determine the target egg region from the `MeasuringSystem` (US-based systems → `EggRegion.US`, otherwise null → skip).
- If both conditions met, regex-replace known UK size words (`\b(small|medium|large)\b` appearing before "egg") with the mapped US equivalent.

### 3. Map `MeasuringSystem` → `EggRegion`

A utility function:
- `USCustomary`, `USCustomaryWithMetric`, `USCustomaryWithImperial`, `USCombined` → `EggRegion.US`
- `Metric`, `Imperial` → null (no conversion, keep UK labels)

### 4. Write tests

**Location**: `library/src/commonTest/kotlin/com/gu/recipe/egg/EggSizeConversionTest.kt`

Test cases:
- UK "large" + US system → "extra large"
- UK "medium" + US system → "large"
- UK "small" + US system → "small" (no change)
- No size word ("1 egg") → unchanged
- Egg whites with size ("2 small egg whites") → correctly converted
- Suffix preserved ("4 medium eggs, lightly beaten" → "4 large eggs, lightly beaten")
- Metric system → no conversion applied

## Extensibility

To add a new region (e.g., Australia):
1. Add `AU` to `EggRegion` enum.
2. Add AU entries to the mapping: `{SMALL → n/a, MEDIUM → "medium", LARGE → "jumbo"}`.
3. Map the relevant `MeasuringSystem` to `EggRegion.AU` (requires adding an AU measuring system or a locale-based resolver).

No structural refactoring needed — only additive changes.

## Notes

- Phrasing: Use "extra large" (unhyphenated, lowercase) to match existing template style.
- Values/mappings live in Kotlin code (not JSON/CSV) for type safety and easy discoverability.
- Source reference: `docs/transformation/eggs-sizes.md`

