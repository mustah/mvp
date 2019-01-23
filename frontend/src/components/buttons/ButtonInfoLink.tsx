import * as React from 'react';
import {OnClick, Titled} from '../../types/Types';
import {Row, RowMiddle} from '../layouts/row/Row';
import {Normal} from '../texts/Texts';
import './ButtonInfoLink.scss';
import {InfoButton, InfoButtonProps} from './InfoButton';

interface Props extends InfoButtonProps, Titled {
  label: string | number;
  labelStyle?: React.CSSProperties;
  onLabelClick?: OnClick;
}

export const ButtonInfoLink = ({color, iconStyle, label, onClick, onLabelClick, labelStyle, title}: Props) => (
  <RowMiddle className="flex-nowrap">
    <InfoButton color={color} iconStyle={iconStyle} onClick={onClick}/>
    <Row onClick={onLabelClick} className="ButtonInfoLink-Label" title={title}>
      <Normal style={labelStyle}>{label}</Normal>
    </Row>
  </RowMiddle>
);
