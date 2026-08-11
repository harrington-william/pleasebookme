import { cn } from "@/lib/utils";

/*
 * The grid is drawn with CSS gradients, NOT an `<svg><pattern id="...">`
 * The grid is wired in root layout.tsx and used globally across the app
 */

type GridBackgroundProps = {
  // Grid cell in pixel. Default: 40px
  cellSize?: number;
  glow?: boolean;
  className?: string;
};

export function GridBackground({
  cellSize = 40,
  glow = true,
  className,
}: GridBackgroundProps) {
  const line = "rgba(255, 255, 255, 0.035)";

  const fade =
    "radial-gradient(ellipse 100% 80% at 50% 40%, #000 20%, transparent 78%)";

  return (
    <div
      aria-hidden="true"
      className={cn(
        "pointer-events-none fixed inset-0 -z-10 overflow-hidden",
        className
      )}
    >
      <div
        className="absolute inset-0 opacity-40"
        style={{
          backgroundImage: `linear-gradient(to right, ${line} 1px, transparent 1px), linear-gradient(to bottom, ${line} 1px, transparent 1px)`,
          backgroundSize: `${cellSize}px ${cellSize}px`,
          maskImage: fade,
          WebkitMaskImage: fade,
        }}
      />

      {glow && (
        <>
          {/* Top-left light effect */}
          <div className="absolute -top-[15%] -left-[10%] h-[45%] w-[45%] rounded-full bg-primary opacity-[0.06] blur-[130px]" />

          {/* Bottom-right ligh effect */}
          <div className="absolute -right-[10%] -bottom-[15%] h-[35%] w-[35%] rounded-full bg-success opacity-[0.04] blur-[110px]" />
        </>
      )}
    </div>
  );
}
