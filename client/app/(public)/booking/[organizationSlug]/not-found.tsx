import Link from "next/link";

export default function BookingNotFound() {
  return (
    <main className="flex flex-1 items-center justify-center px-md py-lg">
      <div className="max-w-[600px] rounded-xl border border-border bg-surface p-xl text-center shadow-sm">
        <h1 className="text-headline-lg-mobile md:text-headline-lg">This booking page isn&apos;t available.</h1>
        <p className="mt-xs text-body-lg text-muted-foreground">Check the link you were given, or contact the business directly.</p>
        <Link
          href="/"
          className="mt-lg inline-flex h-12 cursor-pointer items-center justify-center rounded-lg border border-border px-lg text-body-md font-medium transition-colors hover:bg-surface-hover"
        >
          Back to home
        </Link>
      </div>
    </main>
  );
}
