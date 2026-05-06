# Egg Size Conversion Plan

## Summary

Recipes are authored with UK egg sizes. This feature converts UK egg size labels to regionally appropriate equivalents when rendering for US audiences.

## Mapping

| UK / EU Recipe Calls For | Use in United States |
|--------------------------|----------------------|
| Small (<53 g)            | Small                |
| Medium (53–63 g)         | Large                |
| Large (63–73 g)          | Extra Large          |
| XL (>73 g)               | Jumbo                |

## Logic

1. **When to act**: The ingredient is egg-related (`"egg"`, `"egg white"`, `"egg yolk"`) AND a size word appears in the template text AND the target measuring system is US-based.
2. **What to do**: Replace the UK size word with the US equivalent from the mapping above.
3. **When to do nothing**: No size word present, target is Metric/Imperial, or ingredient is not egg-related.

## Implementation

### 1. Create `EggSizeConversion.kt`

**Location**: `library/src/commonMain/kotlin/com/gu/recipe/egg/EggSizeConversion.kt`

- Define an enum `UkEggSize` (SMALL, MEDIUM, LARGE, EXTRA_LARGE) with label aliases for matching (e.g., EXTRA_LARGE matches "extra large" and "xl").
- Define an enum `EggRegion` (US — extensible later).
- Provide the mapping as `Map<EggRegion, Map<UkEggSize, String>>`.
- Expose `convertEggSizeLabel(ukLabel: String, region: EggRegion): String?`.

### 2. Integrate in `ScaleRecipe.kt`

In `renderTemplate`, after rendering all parts to a string:
- If any `QuantityPlaceholder` has an egg-related `ingredient` and the `MeasuringSystem` is US-based, post-process the output to replace UK size words before "egg" with the US equivalent.

### 3. Tests

**Location**: `library/src/commonTest/kotlin/com/gu/recipe/egg/EggSizeConversionTest.kt`

Key cases:
- UK "large" → US "extra large"
- UK "medium" → US "large"
- UK "small" → US "medium"
- UK "extra large" / "xl" → US "jumbo"
- No size word → unchanged
- Suffix preserved (e.g., ", lightly beaten")
- Metric system → no conversion

## Extensibility

To add a new region (e.g., Australia):
1. Add entry to `EggRegion` enum.
2. Add mapping entries.
3. Wire the relevant `MeasuringSystem` to the new region.

## Notes

- Use "extra large" (unhyphenated, lowercase) to match existing template style.
- All mappings live in Kotlin code for type safety.
- Source reference: `docs/transformation/eggs-sizes.md`

