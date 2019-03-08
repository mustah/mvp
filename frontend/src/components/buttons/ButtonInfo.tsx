import classNames from 'classnames';
import * as React from 'react';
import {Titled} from '../../types/Types';
import {Row, RowMiddle} from '../layouts/row/Row';
import {Normal} from '../texts/Texts';
import './ButtonInfo.scss';
import {InfoButton, InfoButtonProps} from './InfoButton';

interface Props extends InfoButtonProps, Titled {
  label: string | number;
  labelStyle?: React.CSSProperties;
}

export const ButtonInfo = ({color, iconStyle, label, labelStyle, title}: Props) => (
  <RowMiddle className={classNames('ButtonInfo', 'flex-nowrap')}>
    <InfoButton color={color} iconStyle={iconStyle}/>
    <Row className="ButtonInfo-Label" title={title}>
      <Normal style={labelStyle}>{label}</Normal>
    </Row>
  </RowMiddle>
);
