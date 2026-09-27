"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";

import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  bookingDetailsSchema,
  type BookingDetails,
} from "@/features/public-booking/schemas/booking-details-schema";
import { cn } from "@/lib/utils";

function FieldError({ message }: { message?: string }) {
  return message ? (
    <p className="mt-base text-label-md text-destructive">{message}</p>
  ) : null;
}

export function BookingDetailsForm({
  initialValues,
  onBack,
  onSubmit,
}: {
  initialValues: BookingDetails | null;
  onBack: () => void;
  onSubmit: (details: BookingDetails) => void;
}) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<BookingDetails>({
    resolver: zodResolver(bookingDetailsSchema),
    defaultValues: initialValues ?? { name: "", phone: "", email: "", notes: "" },
  });

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-lg">
      <div>
        <Label
          htmlFor="name"
          className="mb-xs text-body-md">Full Name</Label>
        <Input
          id="name"
          placeholder="Jane Doe"
          aria-invalid={Boolean(errors.name)}
          className="h-12"
          {...register("name")}
        />
        <FieldError message={errors.name?.message} />
      </div>

      <div>
        <Label
          htmlFor="phone"
          className="mb-xs text-body-md">Phone Number</Label>
        <Input
          id="phone"
          type="tel"
          placeholder="0912345678"
          aria-invalid={Boolean(errors.phone)}
          className="h-12"
          {...register("phone")}
        />
        <FieldError message={errors.phone?.message} />
      </div>

      <div>
        <div className="mb-xs flex items-center justify-between">
          <Label htmlFor="email" className="text-body-md">Email Address</Label>
          <span className="text-label-md text-muted-foreground">Optional</span>
        </div>
        <Input
          id="email"
          type="email"
          placeholder="jane@example.com"
          aria-invalid={Boolean(errors.email)}
          className="h-12"
          {...register("email")}
        />
        <FieldError message={errors.email?.message} />
      </div>

      <div>
        <div className="mb-xs flex items-center justify-between">
          <Label htmlFor="notes" className="text-body-md">Notes</Label>
          <span className="text-label-md text-muted-foreground">Optional</span>
        </div>
        <textarea
          id="notes"
          rows={3}
          placeholder="Any special requests or details we should know?"
          aria-invalid={Boolean(errors.notes)}
          className={cn(
            "w-full resize-none rounded-lg border border-input bg-transparent px-sm py-xs text-body-md outline-none transition-colors placeholder:text-muted-foreground focus-visible:border-ring focus-visible:ring-2 focus-visible:ring-ring/50",
            errors.notes && "border-destructive"
          )}
          {...register("notes")}
        />
        <FieldError message={errors.notes?.message} />
      </div>

      <div className="mt-md flex gap-md border-t border-border pt-md">
        <button type="button" onClick={onBack} className="h-12 w-1/3 cursor-pointer rounded-lg border border-border text-body-md font-medium transition-colors hover:bg-surface-hover">
          Back
        </button>
        <button type="submit" className="h-12 w-2/3 cursor-pointer rounded-lg bg-primary text-body-md font-bold text-primary-foreground shadow-lg shadow-primary/20 transition-colors hover:bg-primary/80">
          Continue to Review
        </button>
      </div>
    </form>
  );
}
