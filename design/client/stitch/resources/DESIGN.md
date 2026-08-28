---
name: Obsidian Infrastructure
colors:
  surface: '#101419'
  surface-dim: '#101419'
  surface-bright: '#353940'
  surface-container-lowest: '#0a0e14'
  surface-container-low: '#181c22'
  surface-container: '#1c2026'
  surface-container-high: '#262a30'
  surface-container-highest: '#31353b'
  on-surface: '#dfe2eb'
  on-surface-variant: '#c2c6d6'
  inverse-surface: '#dfe2eb'
  inverse-on-surface: '#2d3137'
  outline: '#8c909f'
  outline-variant: '#424754'
  surface-tint: '#adc6ff'
  primary: '#adc6ff'
  on-primary: '#002e6a'
  primary-container: '#4d8eff'
  on-primary-container: '#00285d'
  inverse-primary: '#005ac2'
  secondary: '#4edea3'
  on-secondary: '#003824'
  secondary-container: '#00a572'
  on-secondary-container: '#00311f'
  tertiary: '#ffb786'
  on-tertiary: '#502400'
  tertiary-container: '#df7412'
  on-tertiary-container: '#461f00'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#d8e2ff'
  primary-fixed-dim: '#adc6ff'
  on-primary-fixed: '#001a42'
  on-primary-fixed-variant: '#004395'
  secondary-fixed: '#6ffbbe'
  secondary-fixed-dim: '#4edea3'
  on-secondary-fixed: '#002113'
  on-secondary-fixed-variant: '#005236'
  tertiary-fixed: '#ffdcc6'
  tertiary-fixed-dim: '#ffb786'
  on-tertiary-fixed: '#311400'
  on-tertiary-fixed-variant: '#723600'
  background: '#101419'
  on-background: '#dfe2eb'
  surface-variant: '#31353b'
typography:
  display:
    fontFamily: Geist
    fontSize: 48px
    fontWeight: '600'
    lineHeight: 56px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Geist
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
    letterSpacing: -0.02em
  headline-lg-mobile:
    fontFamily: Geist
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Geist
    fontSize: 20px
    fontWeight: '500'
    lineHeight: 28px
    letterSpacing: -0.01em
  body-lg:
    fontFamily: Geist
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
    letterSpacing: '0'
  body-md:
    fontFamily: Geist
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
    letterSpacing: '0'
  label-md:
    fontFamily: Geist
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.02em
  mono-label:
    fontFamily: Geist Mono
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
    letterSpacing: '0'
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 4px
  xs: 8px
  sm: 12px
  md: 16px
  lg: 24px
  xl: 32px
  2xl: 48px
  container-max: 1440px
  gutter: 24px
---

## Brand & Style

This design system is built for high-stakes enterprise reservation infrastructure. The visual narrative is rooted in **Modern Minimalism** and **Technical Precision**, drawing inspiration from developer-centric platforms like Linear and Vercel. 

The aesthetic is "Infrastructure as Interface"—calm, expensive, and rock-solid. It prioritizes information density and clarity over decorative elements. The user should feel they are interacting with a high-performance engine: silent, powerful, and impeccably organized. We achieve this through a "dark-first" architecture, strict monochromatic surfaces, and high-contrast typography. Decorative imagery is replaced by functional data visualization and purposeful spacing.

## Colors

The palette is anchored by a deep obsidian background to reduce eye strain during long-tail operational tasks. 

- **Foundation:** The `#0B0D10` background provides the lowest depth, while `#12151A` is used for elevated panels and interactive surfaces.
- **Accents:** A precision Blue (`#3B82F6`) is used sparingly for primary actions and focus states. 
- **Status:** We utilize standard semantic colors (Emerald, Amber, Red) but desaturate them slightly to maintain the premium, "quiet" atmosphere of the UI.
- **Borders:** Borders are the primary method of separation. Use `#1F2329` for subtle containment. For high-contrast separations, use a 10% opacity white overlay on the border color.

## Typography

The design system utilizes **Geist** for its technical, Swiss-inspired legibility and precise tracking. 

- **Hierarchy:** Use `Display` and `Headline-LG` only for top-level dashboard overviews or empty states. 
- **Density:** Most enterprise data should reside in `Body-MD` (14px). 
- **Functional:** For ID strings, API keys, or timestamps, utilize a monospaced variant of Geist to emphasize the infrastructure nature of the platform.
- **Contrast:** Maintain a strict distinction between primary text (`#F9FAFB`) and secondary metadata (`#9CA3AF`). Never use pure white (#FFFFFF) for body text to avoid "blooming" on dark backgrounds.

## Layout & Spacing

The layout philosophy follows a **strict 4px grid system** with generous external margins to maintain a premium feel. 

- **Grid:** Use a 12-column fluid grid for main content areas. On desktop, sidebars are fixed at 240px or 280px, while the main stage remains fluid until a max-width of 1440px.
- **Rhythm:** Use `24px` (lg) for internal panel padding and `16px` (md) for spacing between related elements. 
- **Information Density:** While the overall layout feels spacious, data tables and property lists should use compact vertical spacing (`12px` or `8px` gutters) to ensure high utility for power users.
- **Breakpoints:** 
  - Mobile: < 640px (Single column, 16px margins)
  - Tablet: 640px - 1024px (Reduced sidebars, 24px margins)
  - Desktop: > 1024px (Standard sidebars, 48px global margins)

## Elevation & Depth

Depth is communicated through **Tonal Layering** and **Subtle Outlines** rather than heavy shadows.

- **Level 0 (Background):** `#0B0D10` — The canvas.
- **Level 1 (Panels/Cards):** `#12151A` — Used for the primary container of content. These are outlined with a 1px solid border of `#1F2329`.
- **Level 2 (Popovers/Modals):** `#1C1F26` — Higher contrast surfaces. These may use a very subtle, large-radius ambient shadow: `0 20px 40px rgba(0,0,0,0.4)`.
- **Interactive States:** When hovering over a card or list item, transition the background slightly or brighten the border color to `#2D333B`. Do not use "lifts" or Y-axis shifts.

## Shapes

The shape language is controlled and geometric. 

- **Standard Radius:** 10px (`0.625rem`) is the universal radius for input fields, buttons, and small cards. 
- **Nested Radius:** When a component is nested inside another (e.g., a button inside a card), the inner radius should be slightly smaller (e.g., 6px) to maintain visual concentricity.
- **Utility:** Use 4px for very small elements like tags or status indicators. Avoid 0px (too aggressive) and pill-shapes (too consumer-focused/playful).

## Components

- **Buttons:** Primary buttons use the Accent color (`#3B82F6`) with white text. Secondary buttons use a ghost style: a border of `#1F2329` with a background that appears only on hover.
- **Input Fields:** 10px rounded corners. Background is `#0B0D10` (inset look) or a slightly lighter shade than the panel. Use a 2px focus ring of the primary accent color.
- **Cards:** Minimalist. No drop shadows. Defined solely by the `#1F2329` border. Titles should be `Headline-MD`.
- **Chips/Badges:** Small, 4px rounded corners. Use a "Subtle" style: 10% opacity background of the semantic color (e.g., 10% Emerald) with the solid color for text.
- **Data Tables:** No vertical lines. Use horizontal dividers in `#1F2329`. The header row should be `Label-MD` in `Text-Secondary` with all-caps styling.
- **Status Indicators:** Small 6px solid circles. Avoid pulsing animations unless the status is "Critical" or "Active Processing."