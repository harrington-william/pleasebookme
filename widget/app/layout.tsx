import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
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
  title: "PleaseBookMe Widget Catalog",
  description: "Development harness for the portable PleaseBookMe widgets",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html
      lang="en"
      className={`${geistSans.variable} ${geistMono.variable} h-full antialiased`}
    >
      {/* The harness wears the widget's own theme class, so the page around
        * the widget is Obsidian Dark too. A client site that wants a dark
        * booking page does exactly this; one that does not simply leaves it
        * off, and only the widget's own subtree is themed. */}
      <body className="pbm-widget bg-background text-foreground min-h-full flex flex-col">
        {children}
      </body>
    </html>
  );
}
