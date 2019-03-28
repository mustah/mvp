export const widgetMargins: [number, number] = [24, 24];

export const widgetHeightToPx = (height: number): number =>
  height * 170 + (24 * (height - 1)) - 52;

export const widgetWidthToPx = (width: number): number =>
  width * 172 + (24 * (width - 1));
