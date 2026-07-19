---
name: Kinetic Flow
colors:
  surface: '#111317'
  surface-dim: '#111317'
  surface-bright: '#37393d'
  surface-container-lowest: '#0c0e11'
  surface-container-low: '#1a1c1f'
  surface-container: '#1e2023'
  surface-container-high: '#282a2d'
  surface-container-highest: '#333538'
  on-surface: '#e2e2e6'
  on-surface-variant: '#c2c6d6'
  inverse-surface: '#e2e2e6'
  inverse-on-surface: '#2f3034'
  outline: '#8c909f'
  outline-variant: '#424754'
  surface-tint: '#adc6ff'
  primary: '#adc6ff'
  on-primary: '#002e6a'
  primary-container: '#4d8eff'
  on-primary-container: '#00285d'
  inverse-primary: '#005ac2'
  secondary: '#c0c1ff'
  on-secondary: '#1000a9'
  secondary-container: '#3131c0'
  on-secondary-container: '#b0b2ff'
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
  secondary-fixed: '#e1e0ff'
  secondary-fixed-dim: '#c0c1ff'
  on-secondary-fixed: '#07006c'
  on-secondary-fixed-variant: '#2f2ebe'
  tertiary-fixed: '#ffdcc6'
  tertiary-fixed-dim: '#ffb786'
  on-tertiary-fixed: '#311400'
  on-tertiary-fixed-variant: '#723600'
  background: '#111317'
  on-background: '#e2e2e6'
  surface-variant: '#333538'
typography:
  display:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
    letterSpacing: -0.01em
  headline-sm:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 26px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
  label-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.05em
  display-mobile:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 4px
  xs: 4px
  sm: 8px
  md: 16px
  lg: 24px
  xl: 32px
  2xl: 48px
  container_padding: 20px
  stack_gap: 12px
---

## Brand & Style

The design system is engineered for high-conversion scheduling, blending the utility of a developer tool with the elegance of a premium lifestyle brand. The brand personality is professional, efficient, and frictionless, ensuring the user feels empowered rather than overwhelmed during the booking process.

The design style is **Corporate Modern with a Minimalist execution**. It leverages high-quality typography and generous whitespace to create a "one decision per screen" flow. The aesthetic takes cues from high-end fintech interfaces: crisp borders, subtle depth, and a relentless focus on accessibility. It is designed to be embedded into any website while maintaining a distinct, premium identity that suggests reliability and speed.

## Colors

The color palette is anchored by a vibrant **Primary Blue (#3B82F6)**, used strictly for action-oriented elements and progress indicators. 

In the default **Dark Theme**, the interface uses a deep obsidian background to reduce eye strain and emphasize the "glassy" nature of the interactive panels. The **Light Theme** shifts to a high-clarity white environment with soft grey surfaces to differentiate between background and interactive card elements. Semantic colors for success (Green), error (Red), and warning (Amber) should be used sparingly for inline validation and availability statuses.

## Typography

This design system utilizes **Inter** for all roles to ensure maximum legibility and a systematic, technical feel. 

Hierarchy is established through weight and scale. **Display** styles are reserved for the primary question or title of the booking step. **Labels** utilize a medium weight to provide clear affordance for input fields and buttons. For mobile views, typography scales down slightly to maximize screen real estate for interactive elements like calendars and time slots.

## Layout & Spacing

The system follows a **mobile-first, fluid layout** philosophy. When embedded as a widget, it occupies 100% of the parent container's width, with a maximum desktop width of 480px for the single-column booking flow.

Spacing is based on a **4px baseline grid**. 
- **Step Containers:** Use `lg` (24px) padding to create a breathable, premium feel.
- **Component Gaps:** Elements within a card (e.g., image to text) use `md` (16px) gaps.
- **Micro-spacing:** Labels and their respective inputs are separated by `sm` (8px).
- **Safe Areas:** On mobile, a minimum `container_padding` of 20px is maintained at the edges to prevent "cluttering" against the device bezel.

## Elevation & Depth

Depth is used to guide the user's focus through the multi-step flow. 
- **Level 0 (Background):** Flat, using the base theme background color.
- **Level 1 (Cards/Panels):** Raised via the `surface` color with a 1px solid `border`. This creates a structural, "tabbed" appearance.
- **Level 2 (Active States/Dropdowns):** Elevated using a soft, diffused ambient shadow: `0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05)`.

In the Dark Theme, shadows are extremely subtle; depth is instead primarily communicated through the transition from the `#0B0D10` background to the `#12151A` surface.

## Shapes

The shape language is friendly and modern, characterized by large, consistent corner radii. 
- **Service/Resource Cards:** Use a **12px (0.75rem)** radius to feel substantial and "squishy."
- **Inputs:** Use a **10px** radius to provide a soft landing for text, distinguishing them from the more rigid outer containers.
- **Buttons:** Use an **8px** radius to maintain a professional, slightly more precise appearance.
- **Avatars & Badges:** Utilize full rounding (pill-shaped) to provide visual contrast against the rectangular grid of the booking widget.

## Components

### Buttons & Time Slots
- **Primary Buttons:** Solid `#3B82F6` with white text. High-contrast, full-width on mobile.
- **Time Slots:** Secondary buttons that transform to Primary on selection. Large touch targets (min-height 48px).

### Service & Resource Cards
- **Service Cards:** Feature a top-aligned image with a 12px radius, title in `headline-sm`, and a status badge (e.g., "Popular") in the top right.
- **Resource Cards:** Horizontal layout featuring a circular avatar, rating stars, and a "Next Available" text label in `label-sm` (Primary color).

### Calendar
- The calendar uses a minimalist grid with no internal borders. 
- Current day is indicated by a subtle underline; selected day is a solid Primary circle. 
- Non-available days are reduced to 30% opacity.

### Inputs & Validation
- **Input Fields:** 10px radius, 1px border. Focus state uses a 2px Primary border glow.
- **Validation:** Error messages appear inline below the field in 12px text, shifting the border color to Red.

### Progress & Loading
- **Progress Bar:** A thin (4px) bar at the very top of the widget, transitioning smoothly between the 7 steps.
- **Skeletons:** Use a pulse animation with a slight shimmer effect, matching the card and input radii to maintain the layout's structural integrity during load.