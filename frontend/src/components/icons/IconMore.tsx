import IconButton from 'material-ui/IconButton';
import NavigationMoreVert from 'material-ui/svg-icons/navigation/more-vert';
import * as React from 'react';
import {colors} from '../../app/colors';
import {Clickable} from '../../types/Types';
import {ThemeContext, withCssStyles} from '../hoc/withThemeProvider';

const style: React.CSSProperties = {
  padding: 2,
  width: 28,
  height: 28,
};

export const IconMore = withCssStyles(({cssStyles: {primary}, onClick}: Clickable & ThemeContext) => (
  <IconButton
    className="bordered clickable"
    hoveredStyle={{backgroundColor: primary.bgHover}}
    onClick={onClick}
    style={style}
  >
    <NavigationMoreVert color={primary.fg} hoverColor={colors.black}/>
  </IconButton>
));
