# Remove American Campsites & Focus on European / Global Experience

Streamline the CampHaven campsite directory by removing all United States campsites, American search presets, and US region menu options, while establishing European Metric units as the seamless default without interrupting the user on startup.

## User Review & Critical Decisions

> [!IMPORTANT]
> Based on your responses in Phase 1, the following choices are locked in for implementation:

- **Confirmed Decision 1 (Scope of Removal)**: Remove only United States campsites (`curated_1` Whispering Pines Ridge, CA and `curated_2` Silver River Haven, WA). Keep all European campsites (France, Italy, Spain, Portugal, Germany, Switzerland, Austria, Norway, Croatia, Slovenia, Greece, Netherlands) and non-US global destinations (UK, Iceland, Canada, Australia, New Zealand, Japan, South Africa, Chile/Argentina).
- **Confirmed Decision 2 (Search & Region Selectors)**: Remove USA / American destinations from both the "Change Region" dropdown menu and the Google Maps autocomplete search presets in `PlacesSearchService.kt`.
- **Confirmed Decision 3 (Unit System)**: Default directly to European Metric (kilometers, meters, kilograms, €) upon app launch, removing the startup metric prompt modal so users jump straight into exploring without interruption. Unit preferences remain customizable in the Profile screen.

---

## 1. Overview & Core Concept

- **What It Does**: Focuses the campsite catalog on Europe and international camping destinations by purging US-based pitches. Cleanses the region selector and search bar suggestions of American locations, and provides an immediate European-first experience with metric measurements by default.
- **Target Audience / Persona**: European road-trippers, campervan owners, and international campers looking for Park4night-verified pitches, aires de camping-car, and scenic campsites across Europe and global nature routes without US clutter.
- **Key Value**: Delivers a relevant, uncluttered catalog tailored to European camper needs, with zero American entries in listings, filters, or search suggestions.

---

## 2. User Experience & Visual Design

- **Key User Flows**:
  1. **Direct Launch to Explore**: The user opens the app and directly enters the Explore screen centered on European camping hubs (Central Alps / European spots) without an intrusive first-run unit prompt dialog.
  2. **Clean Region Selection**: Tapping **"Change Region"** displays a curated list of European regions (France, Italy, Germany, Switzerland, Spain, Austria, Norway, Portugal, Croatia, Slovenia, Greece, Netherlands, UK, Iceland) alongside international nature regions (Canada Rockies, Australia, New Zealand), with all USA entries removed.
  3. **Google Maps Search**: Typing in the search bar suggests relevant European and international outdoor destinations; American cities and parks (e.g. Yosemite, Olympic, Zion) no longer appear in pre-populated suggestions.
  4. **Consistent European Metrics**: Campsite elevation (meters), driving distance (km), and vehicle clearance restrictions are cleanly formatted in European metric standards.

- **Visual Identity & Atmosphere**:
  - *Theme*: Retains CampHaven's refined outdoor aesthetic with forest green primary accents (`#2D6A4F`, `#1B4332`), clean white/dark cards, and vibrant Park4Night badges (`#2E7D32`).
  - *No Modals on Startup*: Clean, unobstructed zero-state that immediately surfaces campsite cards with photography, tri-pillar amenities (Sleep, Water, Energy), and Park4night verification notes.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Removal Target (US Only vs Entire Americas)**
  - *Chosen Approach*: Specifically remove United States campsites (`stateOrCountry = "California, USA"` and `"Washington, USA"`), while retaining Canada (Banff/Jasper) and South America (Patagonia) as requested.
  - *Why*: Directly honors the user's specific response: "Remove only United States campsites".
- **Decision 2: Preset Search Suggestions Sanitation**
  - *Chosen Approach*: Remove `Yosemite & High Sierra`, `Olympic National Park`, and `Zion National Park` from `PlacesSearchService.POPULAR_CAMPING_DESTINATIONS`.
  - *Why*: Eliminates American entries from autocomplete cards while keeping all European national parks and international destinations intact.
- **Decision 3: Seamless European Metric Default**
  - *Chosen Approach*: Initialize `_unitSystem` to `UnitSystem.METRIC` and permanently set `_showUnitPrompt` to `false`.
  - *Why*: Eliminates friction on app launch while leaving the metric toggle readily accessible in the camper's Profile settings.

---

## 4. Technical Architecture & Data Strategy

### System Component Diagram

```
┌────────────────────────────────────────────────────────┐
│                      ExploreScreen                     │
│  - Top Search Bar (PlacesSearchService without US)     │
│  - Region Dropdown (Europe & Global minus USA)         │
│  - Campsite Cards (Filtered European/Global spots)     │
└──────────────────────────┬─────────────────────────────┘
                           │
                           ▼
┌────────────────────────────────────────────────────────┐
│                    CampsiteViewModel                   │
│  - _unitSystem = METRIC (Default)                      │
│  - _showUnitPrompt = false (Suppressed startup dialog) │
│  - filteredCampsites: Excludes US pitches              │
└──────────────────────────┬─────────────────────────────┘
                           │
                           ▼
┌────────────────────────────────────────────────────────┐
│                   CampsiteRepository                   │
│  - curatedCampsites (Removed curated_1 & curated_2)    │
│  - EuropeanPark4NightData (France, Italy, Spain, etc.) │
│  - WorldCampsitesData (UK, Iceland, Canada, NZ, etc.)  │
└────────────────────────────────────────────────────────┘
```

### Data Layer Adjustments
1. **`CampsiteRepository.kt`**:
   - Remove `curated_1` ("Whispering Pines Ridge", California, USA) and `curated_2` ("Silver River Hookup & Haven", Washington, USA) from `curatedCampsites`.
   - Ensure `seedInitialCampsites()` seeds only the updated curated set, `WorldCampsitesData`, and `EuropeanPark4NightData`.
2. **`PlacesSearchService.kt`**:
   - Remove US search presets (`Yosemite & High Sierra`, `Olympic National Park`, `Zion National Park`).
3. **`ExploreScreen.kt`**:
   - Remove `DropdownMenuItem` for `🇺🇸 USA (High Sierra & Pacific NW)` from the area selector menu.
   - Remove the `if (showUnitPrompt) { AlertDialog(...) }` first-launch prompt block.
4. **`CampsiteViewModel.kt`**:
   - Ensure default `_unitSystem` is `UnitSystem.METRIC` and `_showUnitPrompt` is initialized to `false`.
5. **`ExampleRobolectricTest.kt`**:
   - Add assertion verifying no United States campsites exist in `curatedCampsites`.
   - Verify distance and metric conversions run without errors.
