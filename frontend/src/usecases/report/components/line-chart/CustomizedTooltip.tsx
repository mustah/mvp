import Paper from 'material-ui/Paper';
import * as React from 'react';
import {paperStyle} from '../../../../app/themes';
import {Column} from '../../../../components/layouts/column/Column';
import {Row} from '../../../../components/layouts/row/Row';
import {Bold, Normal} from '../../../../components/texts/Texts';
import {timestamp} from '../../../../helpers/dateHelpers';
import {roundMeasurement} from '../../../../helpers/formatters';
import {ActiveDataPoint} from '../../reportModels';

const style: React.CSSProperties = {
  padding: 8,
};

export const CustomizedTooltip = ({payload: {name}, dataKey, value, stroke}: ActiveDataPoint) => (
  <Paper style={{...paperStyle, ...style}}>
    <Column>
      <Bold style={{color: stroke, marginBottom: 8}}>{dataKey}</Bold>
      <Row>
        <Normal style={{marginRight: 4}}>{timestamp(name)}:</Normal>
        <Bold>{roundMeasurement(value)}</Bold>
      </Row>
    </Column>
  </Paper>
);
