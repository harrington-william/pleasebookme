import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";

import { GridBackground } from "@/components/background/grid-background";
import { clientEnv } from "@/lib/env";
import { cn } from "@/lib/utils";

import "./globals.css";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: {
    default: `${clientEnv.appName} — Reservation Infrastructure`,
    template: `%s · ${clientEnv.appName}`,
  },
  description:
    "Booking infrastructure for service businesses. Availability, scheduling and reservation lifecycle through one centralized platform.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html
      lang="en"
      className={cn("dark", geistSans.variable, geistMono.variable, "h-full antialiased")}
    >
      <body className="flex min-h-full flex-col">
        {/* Grid backdrop for every route */}
        <GridBackground />
        {children}
      </body>
    </html>
  );
}
