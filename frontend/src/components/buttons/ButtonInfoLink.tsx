import * as React from 'react';
import {RowMiddle} from '../layouts/row/Row';
import {Normal} from '../texts/Texts';
import {InfoButton, InfoButtonProps} from './InfoButton';

interface Props extends InfoButtonProps {
  label: string | number;
  labelStyle?: React.CSSProperties;
}

export const ButtonInfoLink = ({color, iconStyle, label, onClick, labelStyle}: Props) => (
  <RowMiddle>
    <InfoButton color={color} iconStyle={iconStyle} onClick={onClick}/>
    <Normal style={labelStyle}>{label}</Normal>
  </RowMiddle>
);
