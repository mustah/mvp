import Paper from 'material-ui/Paper';
import * as React from 'react';
import {TooltipPayload} from 'recharts';
import {paperStyle} from '../../../../app/themes';
import {Column} from '../../../../components/layouts/column/Column';
import {Row, RowSpaceBetween} from '../../../../components/layouts/row/Row';
import {Bold, Normal} from '../../../../components/texts/Texts';
import {displayDate} from '../../../../helpers/dateHelpers';
import {roundMeasurement} from '../../../../helpers/formatters';
import {TooltipMeta} from '../../../../state/ui/graph/measurement/measurementModels';
import {makeMetaKey, makeTimestampKey} from '../../helpers/lineChartHelper';

const style: React.CSSProperties = {
  padding: 8,
};

export const CustomizedTooltip = (props: TooltipPayload) => {
  const {dataKey, color, name, payload, unit, value} = props;
  const timestamp = payload[makeTimestampKey(dataKey as string)] || payload.name;
  const {quantity}: TooltipMeta = payload[makeMetaKey(dataKey as string)];
  return (
    <Paper style={{...paperStyle, ...style}}>
      <Column>
        <RowSpaceBetween style={{marginBottom: 8}}>
          <Bold style={{color}}>{quantity}</Bold>
          <Normal style={{marginLeft: 4}}>{displayDate(timestamp)}</Normal>
        </RowSpaceBetween>
        <Row style={{marginBottom: 8}}>
          <Normal>{name}</Normal>
        </Row>
        <Row>
          <Bold>{roundMeasurement(value as number | string)} {unit}</Bold>
        </Row>
      </Column>
    </Paper>
  );
};
