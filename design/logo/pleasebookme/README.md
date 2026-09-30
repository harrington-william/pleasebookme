# PleaseBookMe identity — Aperture

**Selected direction:** Aperture (concept 01). The open center represents available time. The red vertical interval marks one selected booking. A heavy, three-sided black structure gives the symbol the character of a precision instrument before its scheduling meaning is explained.

## Exploration and selection

[Ten distinct concepts](assets/exploration.svg) were drawn across time-block, clock, calendar, monogram, negative-space, scheduling-grid, and pure-geometry families. [Four shortlisted directions](assets/finalists.svg) were enlarged and checked in color and monochrome.

| Direction | Distinctiveness | Small-size clarity | Restraint | Hidden time/booking idea | Decision |
| --- | --- | --- | --- | --- | --- |
| Aperture | High | High | High | High | Selected; memorable open silhouette and a single precise red interval |
| P monogram | Medium | High | High | Medium | Reads too immediately as a generic initial |
| Keyhole | Medium | High | High | Medium | Strong square, but more app-icon-like |
| Quarter-turn | High | Medium | Medium | Medium | Energetic, but less calm at small sizes |

The remaining six directions were set aside because they read as conventional clock, bars, letter, or calendar motifs, or lost structure when reduced.

## Logo system

| Use | Asset |
| --- | --- |
| Primary horizontal logo, light background | [horizontal-color.svg](assets/horizontal-color.svg) |
| Primary horizontal logo, dark background | [horizontal-dark.svg](assets/horizontal-dark.svg) |
| One-color horizontal logo | [horizontal-black.svg](assets/horizontal-black.svg) |
| Compact navigation lockup | [compact-color.svg](assets/compact-color.svg) |
| Symbol, black and red | [symbol-color.svg](assets/symbol-color.svg) |
| Symbol, black | [symbol-black.svg](assets/symbol-black.svg) |
| Symbol, white | [symbol-white.svg](assets/symbol-white.svg) |
| Symbol on dark ground | [symbol-dark.svg](assets/symbol-dark.svg) |
| Wordmark, black | [wordmark-black.svg](assets/wordmark-black.svg) |
| Wordmark, white | [wordmark-white.svg](assets/wordmark-white.svg) |
| Light and dark applications | [identity-preview.svg](assets/identity-preview.svg) |
| Favicon PNGs | `assets/favicon-{16,24,32,64,128}.png` |

Matching PNG previews of the principal SVGs are in `assets/`. SVGs contain outlined wordmark paths and need no font installed to display.

## Geometry and meaning

- The symbol is built on a **100 × 100 unit field**. Its visible black form spans approximately x=16–81 and y=15–85.
- Three solid bars frame an open, rectangular interval. The open side implies availability rather than a literal calendar.
- The red rectangle spans x=66–81 and y=43–70. It closes one portion of the aperture: a time interval has been selected.
- The right-side gap above the red block keeps the structure open and gives the mark its recognizable silhouette.
- The symbol works in black or white alone; the red carries meaning but is not required for recognition.

## Specifications

| Element | Specification |
| --- | --- |
| Foundation black | `#111214` / RGB 17, 18, 20 |
| Booking red | `#B7192B` / RGB 183, 25, 43 |
| White | `#FFFFFF` |
| Presentation warm white | `#F6F5F2`; use only as a surface, not as part of the mark |
| Wordmark | **PleaseBookMe**, Manrope SemiBold (600), title case, custom optical tracking of 12 font units per glyph; supplied as outlines |
| Symbol clear space | At least **15 units** outside the visible mark on all sides, equal to the thickness of its top bar. Preserve the supplied SVG viewBox padding. |
| Lockup clear space | At least one symbol-bar thickness, measured at the lockup's displayed symbol size, around all visible artwork. |
| Symbol minimum | 16 × 16 px using the supplied pixel-snapped favicon; 24 px or larger for general UI. |
| Compact lockup minimum | 28 px high; use the symbol only if the wordmark has insufficient room. |
| Primary lockup minimum | 40 px high on screen; avoid reducing further in print. |

Keep the icon and wordmark at their supplied relative sizes and spacing. On dark surfaces, use the white and red version. For single-color production, use all black or all white. Do not add gradients, shadows, outlines, a background tile, or additional red elements.

## Build and verification

`build.py` reproduces the vector assets and PNG exports from the included OFL-licensed Manrope source. It requires `fonttools`, `uharfbuzz`, `cairosvg`, and `Pillow`. The font license is in `source/OFL.txt`.

Validation performed: SVGs parsed successfully, raster previews inspected, and symbol exports reviewed at 16, 24, 32, 64, and 128 px in both color and single-color form.
