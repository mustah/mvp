import {darkBlack, fullBlack} from 'material-ui/styles/colors';
import getMuiTheme from 'material-ui/styles/getMuiTheme';
import * as React from 'react';

interface Styles {
  [key: string]: React.CSSProperties;
}

export const fontSizeNormal = 14;

export const colors = {
  darkBlue: '#006da3',
  blue: '#00b6f7',
  lightGrey: '#f9f9f9',
  lightBlack: '#7b7b7b',
  borderColor: '#cccccc',
  dividerColor: '#eaeaea',
  iconHover: '#0f2228',
};

export const mvpTheme = getMuiTheme({
  appBar: {
    height: 60,
    color: colors.darkBlue,
  },
  fontFamily: 'PT Sans, sans-serif',
  palette: {
    primary1Color: fullBlack,
    textColor: darkBlack,
  },
});

export const drawerWidth = 64;

export const iconStyle: React.CSSProperties = {
  padding: 0,
  width: 30,
  height: 30,
};

export const iconSizeLarge: React.CSSProperties = {
  width: 28,
  height: 28,
};

export const sideBarStyles: Styles = {
  fontSize: {fontSize: fontSizeNormal},
  padding: {padding: '5px 0'},
  selected: {color: colors.blue},
  notSelectable: {color: colors.lightBlack},
  onHover: {color: colors.lightGrey},
};

export const sideBarHeaderStyle: React.CSSProperties = {
  fontWeight: 'bold',
  fontSize: fontSizeNormal,
  paddingTop: 0,
};

export const listStyle: React.CSSProperties = {
  padding: 0,
};

export const dividerStyle: React.CSSProperties = {
  backgroundColor: colors.dividerColor,
  marginTop: 10,
  marginBottom: 10,
};

export const listItemStyle: React.CSSProperties = {
  fontSize: fontSizeNormal,
};

export const nestedListItemStyle: React.CSSProperties = {
  paddingTop: 0,
  paddingBottom: 0,
};

export const dropDownStyle: Styles = {
  popoverStyle: {marginTop: 6, marginLeft: 2},
  listStyle: {outline: 'none', paddingLeft: 5, flex: 1},
  parentStyle: {fontSize: 12, color: colors.lightBlack},
};

export const underlineFocusStyle = {
  borderColor: colors.blue,
};

export const floatingLabelFocusStyle = {
  color: colors.blue,
};

export const paperStyle: React.CSSProperties = {
  paddingTop: 16,
  paddingBottom: 16,
  boxShadow: '0 1px 3px rgba(0, 0, 0, 0.16)',
};
