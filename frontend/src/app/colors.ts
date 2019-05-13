import {ColorProperty} from 'csstype';
import {color, ColorHelper} from 'csx';

export interface Colors {
  bgHover: string;
  bgActive: string;
  bg: string;
  bgDark: string;
  bgDarkest: string;
  fg: string;
  fgActive: string;
  fgHover: string;
}

export interface CssStyles {
  primary: Colors;
  secondary: Colors;
}

interface ColorProperties {
  primary: ColorProperty;
  secondary: ColorProperty;
}

export const withStyles = ({primary, secondary}: ColorProperties): CssStyles => {
  const primaryHelper: ColorHelper = color(primary).toHSL();
  const secondaryHelper: ColorHelper = color(secondary).toHSL();
  return ({
    primary: {
      bgHover: primaryHelper.lighten(0.90, true).toString(),
      bgActive: primaryHelper.lighten(0.29).toHexString(),
      bg: primaryHelper.toHexString(),
      bgDark: primaryHelper.darken(0.17).toHexString(),
      bgDarkest: primaryHelper.darken(0.29).toHexString(),
      fg: '#7b7b7b',
      fgActive: '#044462',
      fgHover: '#0f2228',
    },
    secondary: {
      bgHover: '#edf8f2',
      bgActive: secondaryHelper.toHexString(),
      bg: '#f9f9f9',
      bgDark: secondaryHelper.toHexString(),
      bgDarkest: secondaryHelper.toHexString(),
      fg: '#3c3c3c',
      fgActive: '#245c40',
      fgHover: '#0f2228',
    },
  });
};

export const colors = {
  black: '#000000',
  white: '#ffffff',
  alternate: '#f9f9f9',
  ok: '#41c300',
  error: '#e84d6f',
  notification: '#e10050',
  borderColor: '#cccccc',
  dividerColor: '#eaeaea',
  disabledColor: '#eaeaea',
  thresholdStroke: '#90a4ae',
};
