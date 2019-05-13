import * as React from 'react';
import {iconStyle} from '../../app/themes';
import {Titled} from '../../types/Types';
import {ThemeContext, withCssStyles} from '../hoc/withThemeProvider';
import {Row, RowMiddle} from '../layouts/row/Row';
import {Normal} from '../texts/Texts';
import {InfoButton} from './InfoButton';

interface Props extends Titled, ThemeContext {
  label: string | number;
}

const labelStyle: React.CSSProperties = {
  textOverflow: 'ellipsis',
  maxWidth: 102,
  whiteSpace: 'nowrap',
  overflow: 'hidden',
};

export const ButtonInfo = withCssStyles(({cssStyles: {primary}, label, title}: Props) => (
  <RowMiddle className="flex-nowrap clickable">
    <InfoButton color={primary.bg} hoverColor={primary.bg} style={{...iconStyle, height: 40, width: 40}}/>
    <Row className="clickable" title={title}>
      <Normal style={{...labelStyle, color: primary.bg}}>{label}</Normal>
    </Row>
  </RowMiddle>
));
