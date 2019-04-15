import {darkBlack, fullBlack} from 'material-ui/styles/colors';
import getMuiTheme from 'material-ui/styles/getMuiTheme';
import * as React from 'react';
import SvgIconProps = __MaterialUI.SvgIconProps;

interface Styles {
  [key: string]: React.CSSProperties;
}

export const fontSize = {
  small: 12,
  normal: 14,
  medium: 16,
};

export const borderRadius = 4;

export const boxShadow =
  '0px 1px 3px 0px rgba(0, 0, 0, 0.2), 0px 1px 1px 0px rgba(0, 0, 0, 0.14), 0px 2px 1px -1px rgba(0, 0, 0, 0.12)';

export const popoverBoxShadow =
  '0px 5px 5px -3px rgba(0,0,0,0.2), 0px 8px 10px 1px rgba(0,0,0,0.14), 0px 3px 14px 2px rgba(0,0,0,0.12)';

// light-blue: http://zavoloklom.github.io/material-design-color-palette/colors.html#light-blue
const palette = {
  blue200: '#81d4fa',
  blue100: '#b3e5fc',
  blue50: '#e1f5fe',
  blueA100: '#80d8ff',
  blueA200: '#40c4ff',
  blueA400: '#00b0ff',
  blueA700: '#0091ea',
  blueA900: '#01579b',

  deepPurpleA200: '#7C4DFF',
};

export const colors = {
  ...palette,
  darkGreen: '#4caf50',
  orange: '#ff9800',
  red: '#e84d6f',
  secondaryBg: '#e10050',
  white: '#ffffff',
  lightGrey: '#f9f9f9',
  black: '#000000',
  lightBlack: '#7b7b7b',
  borderColor: '#cccccc',
  dividerColor: '#eaeaea',
  iconHover: '#0f2228',
  menuItemLeftIcon: '#757575',
  link: palette.blueA700,
};

export const bgHoverColor = colors.blue50;

export const secondaryBgActive = '#b6e2cc';
export const secondaryBgHover = '#edf8f2';
export const secondaryFgActive = '#245c40';

export const mvpTheme = getMuiTheme({
  appBar: {
    height: 60,
    padding: 16,
    color: colors.blueA900,
  },
  badge: {
    secondaryColor: colors.secondaryBg,
    secondaryTextColor: colors.white,
  },
  checkbox: {
    checkedColor: colors.blueA700,
    boxColor: colors.iconHover,
  },
  fontFamily: 'TTNorms, Arial, sans-serif',
  dialog: {
    bodyFontSize: fontSize.normal,
    bodyColor: darkBlack,
  },
  flatButton: {
    primaryTextColor: colors.blueA700,
  },
  listItem: {
    nestedLevelDepth: 14,
  },
  menuItem: {
    hoverColor: colors.blue50,
    selectedTextColor: colors.blueA700,
  },
  palette: {
    primary1Color: fullBlack,
    textColor: darkBlack,
  },
  raisedButton: {
    primaryColor: colors.blueA700,
  },
  toggle: {
    trackOnColor: colors.blueA100,
    thumbOnColor: colors.blueA700,
  }
});

export const appBarHeight: number = mvpTheme.appBar!.height!;

export const drawerContainerStyle: React.CSSProperties = {
  boxShadow,
  top: appBarHeight,
  paddingBottom: appBarHeight + 24,
};

export const sideMenuWidth = 300;

export const iconStyle: React.CSSProperties = {
  padding: 0,
  width: 30,
  height: 30,
};

export const iconSizeMedium: React.CSSProperties = {
  width: 26,
  height: 26,
};

export const actionMenuItemIconStyle: React.CSSProperties = {
  height: 18,
  width: 18,
  padding: 0,
  margin: '7px 0 0 4px',
};

export const topMenuItemIconStyle: React.CSSProperties = {
  ...actionMenuItemIconStyle,
  margin: '4px 0 0 4px',
  height: 22,
  width: 22,
};

export const dividerStyle: React.CSSProperties = {
  backgroundColor: colors.dividerColor
};

export const selectedStyle: React.CSSProperties = {
  color: colors.black,
  fontWeight: 'bold',
  backgroundColor: colors.blue100
};

export const menuItemStyle: React.CSSProperties = {
  fontSize: fontSize.normal,
  textStyle: {
    textOverflow: 'ellipsis',
    maxWidth: 150,
    whiteSpace: 'nowrap',
    overflow: 'hidden',
  },
};

export const listItemStyle: React.CSSProperties = {
  borderRadius: '0 50px 50px 0',
  ...menuItemStyle,
};

export const listItemStyleSelected: React.CSSProperties = {
  ...listItemStyle,
  ...selectedStyle,
  backgroundColor: secondaryBgActive,
  color: secondaryFgActive
};

export const listItemInnerDivStyle: React.CSSProperties = {
  padding: 0,
};

export const menuItemInnerDivStyle: React.CSSProperties = {
  ...menuItemStyle,
  lineHeight: '32px',
  minHeight: 32,
};

export const topMenuItemDivStyle: React.CSSProperties = {
  ...menuItemInnerDivStyle,
  fontSize: 16,
};

export const topMenuInnerDivStyle: React.CSSProperties = {
  padding: '0 0 0 38px',
};

export const popoverStyle: React.CSSProperties = {
  borderRadius,
  boxShadow: popoverBoxShadow,
};

export const dropdownStyle: Styles = {
  popoverStyle: {marginTop: 6, marginLeft: 2, ...popoverStyle},
  listStyle: {outline: 'none', paddingLeft: 5, flex: 1},
  parentStyle: {fontSize: 11, fontWeight: 'normal', color: colors.lightBlack},
};

export const underlineFocusStyle = {
  borderColor: colors.blueA700,
  borderWidth: 2,
};

export const floatingLabelFocusStyle = {
  color: colors.blueA700,
};

export const paperStyle: React.CSSProperties = {
  paddingTop: 16,
  paddingBottom: 16,
  boxShadow,
  borderRadius,
};

export const mainContentPaperStyle: React.CSSProperties = {
  ...paperStyle,
  paddingTop: 8,
  paddingBottom: 0,
};

export const cardStyle: React.CSSProperties = {
  borderRadius,
  boxShadow,
};

export const buttonStyle: React.CSSProperties = {
  backgroundColor: colors.blueA700,
  color: colors.white,
};

export const dropdownListStyle: React.CSSProperties = {
  width: 200,
  paddingTop: 8,
  paddingBottom: 8,
  ...popoverStyle
};

export const gridStyle: React.CSSProperties = {
  borderTopWidth: 0,
  borderBottomWidth: 0,
  marginBottom: borderRadius,
  borderBottomLeftRadius: borderRadius,
  borderBottomRightRadius: borderRadius,
};

export const dividerBorder = `1px solid ${colors.dividerColor}`;
export const border = `1px solid ${colors.borderColor}`;

export const svgIconProps: SvgIconProps = {
  color: colors.lightBlack,
  hoverColor: colors.iconHover
};
