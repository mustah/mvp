import * as React from 'react';
import {OnClick} from '../../types/Types';
import {RowMiddle} from '../layouts/row/Row';
import {Normal} from '../texts/Texts';
import {InfoButton} from './InfoButton';

interface InfoLinkProps {
  onClick: OnClick;
  label: string | number;
  labelStyle?: React.CSSProperties;
  iconStyle?: React.CSSProperties;
}

export const ButtonInfoLink = ({iconStyle, label, onClick, labelStyle}: InfoLinkProps) => (
  <RowMiddle>
    <InfoButton onClick={onClick} iconStyle={iconStyle}/>
    <Normal style={labelStyle}>{label}</Normal>
  </RowMiddle>
);
