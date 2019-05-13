import {default as classNames} from 'classnames';
import {important} from 'csx';
import IconButton from 'material-ui/IconButton';
import * as React from 'react';
import {style as typestyle} from 'typestyle';
import {Selectable} from '../../types/Types';
import {ThemeContext, withCssStyles} from '../hoc/withThemeProvider';
import IconButtonProps = __MaterialUI.IconButtonProps;

const roundedIconStyle: React.CSSProperties = {
  padding: 0,
  marginLeft: 8,
  width: 44,
  height: 44,
  borderRadius: 44 / 2,
};

export const ToolbarIconButton = withCssStyles(({
  cssStyles: {primary},
  children,
  disabled,
  iconStyle,
  isSelected,
  onClick,
  style,
  tooltip,
}: IconButtonProps & Selectable & ThemeContext) => {
  const className = typestyle({
    $nest: {
      '&:hover svg': {fill: important(primary.bg)},
      '&.isSelected svg': {fill: important(primary.bg)},
      '&.isSelected:hover svg': {fill: important(primary.bg)},
    },
  });
  return (
    <IconButton
      className={classNames('ToolbarIconButton', {disabled}, {isSelected}, className)}
      disabled={disabled}
      hoveredStyle={{backgroundColor: primary.bgHover}}
      iconStyle={iconStyle}
      onClick={onClick}
      tooltip={tooltip}
      tooltipPosition="bottom-center"
      style={{...roundedIconStyle, ...style}}
    >
      {children}
    </IconButton>
  );
});
