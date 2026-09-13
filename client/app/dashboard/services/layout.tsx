import type { ReactNode } from "react";

/**
 * A pass-through. The editor's tab rail is rendered by ServiceEditor itself, not
 * by a layout, because the tab is a query param rather than a route segment —
 * see AGENTS.md "The services editor". Kept as the segment's reserved slot for
 * anything genuinely shared by the catalog and the editor later on.
 */
export default function ServicesLayout({
  children,
}: Readonly<{ children: ReactNode }>) {
  return <>{children}</>;
}
