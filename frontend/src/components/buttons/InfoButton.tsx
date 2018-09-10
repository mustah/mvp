import IconButton from 'material-ui/IconButton';
import ActionInfoOutline from 'material-ui/svg-icons/action/info-outline';
import * as React from 'react';
import {colors} from '../../app/themes';
import {Clickable} from '../../types/Types';

interface Props extends Clickable {
  labelStyle?: React.CSSProperties;
  iconStyle?: React.CSSProperties;
}

export const InfoButton = ({iconStyle, onClick}: Props) => (
  <IconButton onClick={onClick} className="InfoButton" style={iconStyle}>
    <ActionInfoOutline style={iconStyle} color={colors.lightBlack} hoverColor={colors.iconHover}/>
  </IconButton>
);
