import { MarketingLink } from "@/components/marketing/marketing-link";
import {
  MARKETING_FOOTER_COLUMNS,
  MARKETING_LEGAL_LINKS,
} from "@/components/marketing/marketing-navigation";
import { clientEnv } from "@/lib/env";

export function MarketingFooter() {
  return (
    <footer className="mt-2xl border-t border-border bg-surface px-lg pt-xl pb-lg lg:px-2xl">
      <div className="mx-auto max-w-[1440px]">
        <div className="mb-xl grid grid-cols-2 gap-xl md:grid-cols-4 lg:grid-cols-5">
          <div className="col-span-2">
            <span className="mb-sm block text-headline-md font-bold text-foreground">
              {clientEnv.appName}
            </span>
            <p className="max-w-[300px] text-body-md text-muted-foreground">
              Enterprise reservation infrastructure designed for the modern web.
            </p>
          </div>

          {MARKETING_FOOTER_COLUMNS.map((column) => (
            <div key={column.heading}>
              <h2 className="mb-sm text-label-md tracking-wider text-foreground uppercase">
                {column.heading}
              </h2>
              <ul className="flex flex-col gap-sm">
                {column.links.map((link) => (
                  <li key={link.label}>
                    <MarketingLink item={link} className="text-body-md" />
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>

        <div className="flex flex-col items-center justify-between border-t border-border pt-lg md:flex-row">
          <span className="text-body-md text-muted-foreground">
            © {new Date().getFullYear()} {clientEnv.appName} Inc. All rights
            reserved.
          </span>
          <div className="mt-md flex items-center gap-md md:mt-0">
            {MARKETING_LEGAL_LINKS.map((link) => (
              <MarketingLink
                key={link.label}
                item={link}
                className="text-body-md"
              />
            ))}
          </div>
        </div>
      </div>
    </footer>
  );
}
