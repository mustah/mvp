/**
 * Defaults to hex color '#0091ea'.
 *
 * @param lightnessPercentage number between 0 and 100.
 */
const primaryBlueColor = (lightnessPercentage: number): string => `hsla(203, 100%, ${lightnessPercentage}%, 1)`;

const primary = {
  primaryBgHover: primaryBlueColor(92),
  primaryBgLight: primaryBlueColor(75),
  primaryBg: primaryBlueColor(46),
  primaryBgDark: primaryBlueColor(29),

  primaryFg: '#7b7b7b',
  primaryFgActive: primaryBlueColor(46),
  primaryFgHover: '#0f2228',
};

const secondary = {
  secondaryBg: '#f9f9f9',
  secondaryBgActive: '#b6e2cc',
  secondaryBgHover: '#edf8f2',
  secondaryFgActive: '#245c40',
};

export const colors = {
  ...primary,
  ...secondary,

  black: '#000000',
  white: '#ffffff',
  ok: '#41c300',
  error: '#e84d6f',
  notification: '#e10050',
  borderColor: '#cccccc',
  dividerColor: '#eaeaea',
  thresholdStroke: '#90a4ae',
};
