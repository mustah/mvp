import * as React from 'react';
import {Children, OnClick, Styled, Titled} from '../../types/Types';
import {ButtonInfo} from '../buttons/ButtonInfo';
import {InfoButtonProps} from '../buttons/InfoButton';
import {Row} from '../layouts/row/Row';

interface Props extends InfoButtonProps, Styled, Titled {
  autoScrollBodyContent: boolean;
  children: Children;
  label: string | number;
  labelStyle?: React.CSSProperties;
  onLabelClick?: OnClick;
}

const infoLabelStyle: React.CSSProperties = {paddingLeft: 0};

export const OpenDialogInfoButton = ({color, iconStyle, label, labelStyle, style, title}: Props) => (
  <Row style={style}>
    <ButtonInfo
      label={label}
      color={color}
      iconStyle={iconStyle}
      labelStyle={labelStyle || infoLabelStyle}
      title={title}
    />
  </Row>
);
