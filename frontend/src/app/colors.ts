import {Color as ColorProp, ColorProperty} from 'csstype';

export type Color = ColorProp;

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

export interface ColorProperties {
  primary: ColorProperty;
  secondary: ColorProperty;
}

export const colors = {
  black: '#000000',
  white: '#ffffff',
  alternate: '#f9f9f9',
  ok: '#41c300',
  error: '#e84d6f',
  info: '#7b7b7b',
  notification: '#e10050',
  borderColor: '#cccccc',
  dividerColor: '#eaeaea',
  disabledColor: '#eaeaea',
  thresholdStroke: '#90a4ae',
};
