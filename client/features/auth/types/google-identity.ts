/**
 * Minimal typings for Google Identity Services (GIS).
 *
 * GIS is loaded at runtime from https://accounts.google.com/gsi/client and
 * attaches itself to `window.google`. There is no official npm types package
 * that matches the loaded script, so only the surface this app actually uses is
 * declared here — deliberately narrow, so an unused API changing upstream
 * cannot silently mistype something we depend on.
 *
 * Reference: https://developers.google.com/identity/gsi/web/reference/js-reference
 */

/** Payload handed to the callback once the user picks a Google account. */
export interface GoogleCredentialResponse {
  /**
   * The ID token (a JWT). This is exactly what
   * POST /api/v1/auth/google expects as `idToken`.
   */
  credential: string;
  select_by?: string;
}

export interface GoogleIdConfiguration {
  client_id: string;
  callback: (response: GoogleCredentialResponse) => void;
  /** Disables Google's automatic One Tap sign-in on page load. */
  auto_select?: boolean;
  cancel_on_tap_outside?: boolean;
  /** Opt into the browser's FedCM API, which Chrome now requires for One Tap. */
  use_fedcm_for_prompt?: boolean;
  ux_mode?: "popup" | "redirect";
}

export interface GoogleButtonConfiguration {
  type?: "standard" | "icon";
  theme?: "outline" | "filled_blue" | "filled_black";
  size?: "small" | "medium" | "large";
  text?: "signin_with" | "signup_with" | "continue_with" | "signin";
  shape?: "rectangular" | "pill" | "circle" | "square";
  logo_alignment?: "left" | "center";
  /** Pixels. Google caps this at 400. */
  width?: number;
  locale?: string;
}

export interface GoogleAccountsId {
  initialize: (config: GoogleIdConfiguration) => void;
  renderButton: (
    parent: HTMLElement,
    options: GoogleButtonConfiguration
  ) => void;
  prompt: () => void;
  disableAutoSelect: () => void;
}

declare global {
  interface Window {
    google?: {
      accounts?: {
        id?: GoogleAccountsId;
      };
    };
  }
}

export {};
