import classNames from "classnames"
import { twMerge } from "tailwind-merge"

/**
 * Standard classname composition helper. Joins conditional/array/object
 * class fragments via `classnames`, then resolves conflicting Tailwind
 * utilities (e.g. `p-2` + `p-4`) via `tailwind-merge`.
 *
 * Every dynamic className in this project should be built through this
 * function rather than hand-rolled with a ternary or template literal —
 * see AGENTS.md.
 */
export function cn(...inputs: classNames.ArgumentArray) {
  return twMerge(classNames(inputs))
}
