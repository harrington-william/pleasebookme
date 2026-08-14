export type MarketingLinkStatus = "ready" | "reserved";

export type MarketingLinkItem = {
  label: string;
  href?: string;
  status: MarketingLinkStatus;
  reservedReason?: string;
};

export const MARKETING_NAV_ITEMS: readonly MarketingLinkItem[] = [
  { label: "Features", href: "#features", status: "ready" },
  {
    label: "Pricing",
    status: "reserved",
    reservedReason:
      "The billing schema is deliberately excluded from Platform v1 (AGENTS.md).",
  },
];

export type MarketingFooterColumn = {
  heading: string;
  links: readonly MarketingLinkItem[];
};

export const MARKETING_FOOTER_COLUMNS: readonly MarketingFooterColumn[] = [
  {
    heading: "Product",
    links: [
      { label: "Features", href: "#features", status: "ready" },
      { label: "Integrations", href: "/dashboard/settings/integrations", status: "ready" },
      {
        label: "Pricing",
        status: "reserved",
        reservedReason:
          "The billing schema is deliberately excluded from Platform v1 (AGENTS.md).",
      },
      {
        label: "Changelog",
        status: "reserved",
        reservedReason: "No changelog surface has been built yet.",
      },
    ],
  },
  {
    heading: "Developers",
    links: [
      {
        label: "Documentation",
        status: "reserved",
        reservedReason: "No documentation site exists yet.",
      },
      {
        label: "API Reference",
        status: "reserved",
        reservedReason:
          "The public developer API is post-Platform-v1 work (AGENTS.md).",
      },
      {
        label: "Status",
        status: "reserved",
        reservedReason: "No status page has been built yet.",
      },
      {
        label: "GitHub",
        status: "reserved",
        reservedReason: "The repository is private during Platform v1.",
      },
    ],
  },
  {
    heading: "Company",
    links: [
      {
        label: "About",
        status: "reserved",
        reservedReason: "No company pages have been built yet.",
      },
      {
        label: "Blog",
        status: "reserved",
        reservedReason: "No blog has been built yet.",
      },
      {
        label: "Careers",
        status: "reserved",
        reservedReason: "No careers page has been built yet.",
      },
      {
        label: "Contact",
        status: "reserved",
        reservedReason: "No contact surface has been built yet.",
      },
    ],
  },
];

export const MARKETING_LEGAL_LINKS: readonly MarketingLinkItem[] = [
  {
    label: "Privacy",
    status: "reserved",
    reservedReason: "The privacy policy has not been written yet.",
  },
  {
    label: "Terms",
    status: "reserved",
    reservedReason: "The terms of service have not been written yet.",
  },
];
