import {default as classNames} from 'classnames';
import IconButton from 'material-ui/IconButton';
import * as React from 'react';
import {Selectable} from '../../types/Types';
import IconButtonProps = __MaterialUI.IconButtonProps;

const roundedIconStyle: React.CSSProperties = {
  padding: 0,
  marginLeft: 8,
  width: 44,
  height: 44,
  borderRadius: 44 / 2,
};

export const ToolbarIconButton = ({
  children,
  disabled,
  iconStyle,
  isSelected,
  onClick,
  style,
  tooltip,
}: IconButtonProps & Selectable) => (
  <IconButton
    className={classNames('ToolbarIconButton', {disabled}, {isSelected})}
    disabled={disabled}
    iconStyle={iconStyle}
    onClick={onClick}
    tooltip={tooltip}
    tooltipPosition="bottom-center"
    style={{...roundedIconStyle, ...style}}
  >
    {children}
  </IconButton>
);
