import type { Currency } from "../../ecosystems/barbershop/logic/types";

export function formatDuration(minutes: number): string {
  if (minutes < 60) return `${minutes} min`;
  const hours = Math.floor(minutes / 60);
  const remainder = minutes % 60;
  return remainder ? `${hours} hr ${remainder} min` : `${hours} hr`;
}

function formatMoney(value: number, currency: Currency): string {
  return new Intl.NumberFormat(undefined, {
    style: "currency",
    currency,
    maximumFractionDigits: currency === "VND" ? 0 : 2,
  }).format(value);
}

export function formatPriceRange(
  min: number | null,
  max: number | null,
  currency: Currency
): string {
  if ((!min && !max) || (min === 0 && max === 0)) return "Free";
  if (min === null) return formatMoney(max ?? 0, currency);
  if (max === null || min === max) return formatMoney(min, currency);
  return `${formatMoney(min, currency)} – ${formatMoney(max, currency)}`;
}
