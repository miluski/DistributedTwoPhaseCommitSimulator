/** Phase header descriptor inserted before a phase-marking event in the timeline. */
export interface PhaseMarker {
  /** Circled numeral identifying the phase (e.g. ①). */
  readonly number: string;
  /** Descriptive Polish label for the phase. */
  readonly label: string;
}
