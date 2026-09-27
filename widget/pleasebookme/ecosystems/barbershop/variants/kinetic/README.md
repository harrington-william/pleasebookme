# Barbershop · Kinetic

The first barbershop variant, and the reference for every variant after it.
Six steps in one card: **Service → Date → Time → Details → Review → Done**.

Suitable for any appointment-centric business, which is why `registry.ts`
maps both `BARBERSHOP` and `GENERAL` to it.

## Installing

Follow the seven steps in [`pleasebookme/README.md`](../../../../README.md).
The import line for this variant is:

```tsx
import { BarbershopBookingWidget } from "<path>/pleasebookme/ecosystems/barbershop/variants/kinetic/BarbershopBookingWidget";
```

`<BarbershopBookingWidget />` takes no props — everything it needs comes from
`PleaseBookMeProvider` through context. It renders its own loading and
"booking page isn't available" states, so the host page needs no fallback.

## What lives here, and what does not

This folder is **presentation only**. It holds no state beyond form inputs
and no fetching: every piece of behaviour — the step machine, the step-back
rule, the slot fetch, the zod schema, the error mapping — lives in
`../../logic/` and is shared by every future barbershop variant.

If you are changing *what the widget does*, you are in the wrong folder.
If you are changing *how it looks*, you are in the right one.

## Writing a second variant

1. `cp -r kinetic ../<name>` and rewrite the components.
2. Keep importing `useBookingFlow` from `../../logic/` — do not fork it.
3. Override `--pbm-*` in the new variant's `theme.css` rather than editing
   `core/styles/tokens.css`, which every variant shares.
4. Add it to `registry.ts` only if an ecosystem code should default to it.

`theme.css` here is intentionally empty: this variant uses the shared tokens
unchanged.

## Notes carried from the hosted page

- Stepping **back** keeps every choice up to the target step and clears the
  ones after it; moving **forward** never clears anything.
- A `409` on confirm means the slot went while the visitor was typing: the
  flow clears the slot, refetches, and returns to Time with a message.
- Every clickable control carries `cursor-pointer` — Tailwind v4 dropped the
  pointer cursor from `<button>`, and customers expect the same feel as other
  booking tools.
- The widget scrolls its own content on a step change, never the host page.
