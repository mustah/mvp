import IconButton from 'material-ui/IconButton';
import ActionInfoOutline from 'material-ui/svg-icons/action/info-outline';
import * as React from 'react';
import {OnClick} from '../../types/Types';
import SvgIconProps = __MaterialUI.SvgIconProps;

interface Props {
  onClick?: OnClick;
}

export const InfoButton = ({color, style, hoverColor, onClick}: SvgIconProps & Props) => (
  <IconButton onClick={onClick} style={style}>
    <ActionInfoOutline style={style} color={color} hoverColor={hoverColor}/>
  </IconButton>
);
