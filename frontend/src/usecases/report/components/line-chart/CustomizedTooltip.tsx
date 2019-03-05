import Paper from 'material-ui/Paper';
import * as React from 'react';
import {TooltipPayload} from 'recharts';
import {paperStyle} from '../../../../app/themes';
import {Column} from '../../../../components/layouts/column/Column';
import {Row} from '../../../../components/layouts/row/Row';
import {Bold, Normal} from '../../../../components/texts/Texts';
import {displayDate} from '../../../../helpers/dateHelpers';
import {roundMeasurement} from '../../../../helpers/formatters';
import {makeTimestampKey} from '../../helpers/graphContentsMapper';

const style: React.CSSProperties = {
  padding: 8,
};

export const CustomizedTooltip = (props: TooltipPayload) => {
  const {dataKey, color, name, payload, value} = props;
  const timestamp = payload[makeTimestampKey(dataKey as string)] || payload.name;
  return (
    <Paper style={{...paperStyle, ...style}}>
      <Column>
        <Bold style={{color, marginBottom: 8}}>{name}</Bold>
        <Row>
          <Normal style={{marginRight: 4}}>{displayDate(timestamp)}:</Normal>
          <Bold>{roundMeasurement(value as number | string)}</Bold>
        </Row>
      </Column>
    </Paper>
  );
};
