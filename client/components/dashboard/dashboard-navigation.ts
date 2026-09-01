import {
  Bell,
  Boxes,
  Braces,
  Calendar,
  CalendarCheck,
  ChartColumn,
  CircleUser,
  Clock,
  LayoutGrid,
  LayoutTemplate,
  LifeBuoy,
  Moon,
  Plug,
  Settings,
  Sparkles,
  Users,
  type LucideIcon,
} from "lucide-react";

export type DashboardNavStatus = "ready" | "reserved";

export type DashboardNavItem = {
  label: string;
  href: string;
  icon: LucideIcon;
  status: DashboardNavStatus;
  reservedReason?: string;
  match?: "exact" | "prefix";
};

export const DASHBOARD_NAV_ITEMS: readonly DashboardNavItem[] = [
  {
    label: "Dashboard",
    href: "/dashboard",
    icon: LayoutGrid,
    status: "ready",
    match: "exact",
  },
  {
    label: "Bookings",
    href: "/dashboard/bookings",
    icon: CalendarCheck,
    status: "reserved",
    reservedReason: "The bookings workspace has not been built yet.",
  },
  {
    label: "Availability",
    href: "/dashboard/availability",
    icon: Clock,
    status: "ready",
  },
  {
    label: "Services",
    href: "/dashboard/services",
    icon: Sparkles,
    status: "ready",
  },
  {
    label: "Resources",
    href: "/dashboard/resources",
    icon: Boxes,
    status: "ready",
    reservedReason: "The resource catalog has not been built yet.",
  },
  {
    label: "Customers",
    href: "/dashboard/customers",
    icon: Users,
    status: "reserved",
    reservedReason: "The customer directory has not been built yet.",
  },
  {
    label: "Calendar",
    href: "/dashboard/calendar",
    icon: Calendar,
    status: "reserved",
    reservedReason: "The calendar view has not been built yet.",
  },
  {
    label: "Widgets",
    href: "/dashboard/widgets",
    icon: LayoutTemplate,
    status: "reserved",
    reservedReason: "Widget management has not been built yet.",
  },
  {
    label: "Analytics",
    href: "/dashboard/analytics",
    icon: ChartColumn,
    status: "reserved",
    reservedReason:
      "The analytics schema is deliberately excluded from Platform v1 (AGENTS.md).",
  },
  {
    label: "Notifications",
    href: "/dashboard/notifications",
    icon: Bell,
    status: "reserved",
    reservedReason: "The notification inbox has not been built yet.",
  },
  {
    label: "Integrations",
    href: "/dashboard/settings/integrations",
    icon: Plug,
    status: "ready",
  },
  {
    label: "API",
    href: "/dashboard/api",
    icon: Braces,
    status: "reserved",
    reservedReason:
      "API keys exist as a table but have no functional usage in Platform v1 (AGENTS.md).",
  },
  {
    label: "Settings",
    href: "/dashboard/settings",
    icon: Settings,
    status: "reserved",
    reservedReason: "The settings index has not been built yet.",
  },
];

export const DASHBOARD_ACCOUNT_ITEMS: readonly DashboardNavItem[] = [
  {
    label: "Profile",
    href: "/dashboard/profile",
    icon: CircleUser,
    status: "reserved",
    reservedReason:
      "The platform exposes no /me endpoint, so there is no profile to render (AGENTS.md).",
  },
  {
    label: "Theme",
    href: "/dashboard/theme",
    icon: Moon,
    status: "reserved",
    reservedReason:
      "Obsidian Dark is the only theme; there is nothing to switch between (DESIGN.md).",
  },
  {
    label: "Support",
    href: "/dashboard/support",
    icon: LifeBuoy,
    status: "reserved",
    reservedReason: "No support surface has been built yet.",
  },
];

export function isDashboardNavItemActive(
  item: DashboardNavItem,
  pathname: string
): boolean {
  if (item.match === "exact") {
    return pathname === item.href;
  }

  return pathname === item.href || pathname.startsWith(`${item.href}/`);
}
