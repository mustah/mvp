import {toArray} from 'lodash';
import {Paper} from 'material-ui';
import * as React from 'react';
import {AxisDomain, CartesianGrid, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis} from 'recharts';
import {paperStyle} from '../../../app/themes';
import {Column, ColumnCenter} from '../../../components/layouts/column/Column';
import {Row} from '../../../components/layouts/row/Row';
import {Loader} from '../../../components/loading/Loader';
import {Bold, Normal} from '../../../components/texts/Texts';
import {TimestampInfoMessage} from '../../../components/timestamp-info-message/TimestampInfoMessage';
import {displayDate, shortTimestamp} from '../../../helpers/dateHelpers';
import {encodeRequestParameters, requestParametersFrom} from '../../../helpers/urlFactory';
import {colorFor} from '../../report/helpers/graphContentsMapper';
import {DispatchToProps, StateToProps} from '../containers/CollectionGraphContainer';

export type Props = StateToProps & DispatchToProps;

const lineMargins: React.CSSProperties = {top: 40, right: 0, bottom: 0, left: 0};
const domains: [AxisDomain, AxisDomain] = ['dataMin', 'dataMax'];

const style: React.CSSProperties = {
  padding: 8,
};

const formatTime = (time: number) =>
  shortTimestamp(time * 1000);

const CustomizedTooltip = (props) => {
  const {active, payload} = props;
  if (active) {
    const {payload: {id, collectionPercentage}} = payload[0];

    return (
      <Paper style={{...paperStyle, ...style}}>
        <Column>
          <Row>
            <Normal style={{marginRight: 4}}>{displayDate(id * 1000)}:</Normal>
            <Bold>{collectionPercentage}</Bold>
          </Row>
        </Column>
      </Paper>
    );
  }
  return null;
};

export const CollectionStatLineChart = (props: Props) => {
  const {
    isFetching,
    parameters,
    requestParameters,
    fetchCollectionStats,
    collectionStats,
  } = props;

  React.useEffect(() => {
    fetchCollectionStats(encodeRequestParameters(requestParametersFrom(requestParameters.selectionParameters)));
  }, [requestParameters, parameters]);

  const data = toArray(collectionStats);

  return (
    <Loader isFetching={isFetching}>
      <ColumnCenter className="align-items">
        <ResponsiveContainer aspect={2.5} width="95%" height="99%">
          <LineChart
            width={10}
            height={50}
            data={data}
            margin={lineMargins}
          >
            <XAxis
              dataKey="id"
              domain={domains}
              scale="time"
              tickFormatter={formatTime}
              type="number"
            />
            <YAxis/>
            <CartesianGrid strokeDasharray="3 3"/>
            <Tooltip content={<CustomizedTooltip/>}/>
            <Line
              dataKey="collectionPercentage"
              stroke={colorFor('collectionPercentage')}
              strokeWidth={2}
              type="monotone"
            />
          </LineChart>
        </ResponsiveContainer>
        <TimestampInfoMessage/>
      </ColumnCenter>
    </Loader>
  );
};
