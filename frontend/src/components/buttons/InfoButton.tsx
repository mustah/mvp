import IconButton from 'material-ui/IconButton';
import ActionInfoOutline from 'material-ui/svg-icons/action/info-outline';
import * as React from 'react';
import {colors} from '../../app/themes';
import {OnClick} from '../../types/Types';

export interface InfoButtonProps {
  color?: string;
  iconStyle?: React.CSSProperties;
  onClick?: OnClick;
}

export const InfoButton = ({color, iconStyle, onClick}: InfoButtonProps) => (
  <IconButton onClick={onClick} className="InfoButton" style={iconStyle}>
    <ActionInfoOutline style={iconStyle} color={color || colors.lightBlack} hoverColor={colors.iconHover}/>
  </IconButton>
);
