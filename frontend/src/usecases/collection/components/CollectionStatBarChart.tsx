import {toArray} from 'lodash';
import {Paper} from 'material-ui';
import * as React from 'react';
import {AxisDomain, Bar, BarChart, CartesianGrid, Margin, ResponsiveContainer, Tooltip, XAxis, YAxis} from 'recharts';
import {paperStyle} from '../../../app/themes';
import {withEmptyContent, WithEmptyContentProps} from '../../../components/hoc/withEmptyContent';
import {Column, ColumnCenter} from '../../../components/layouts/column/Column';
import {Row} from '../../../components/layouts/row/Row';
import {Loader} from '../../../components/loading/Loader';
import {Bold, Normal} from '../../../components/texts/Texts';
import {TimestampInfoMessage} from '../../../components/timestamp-info-message/TimestampInfoMessage';
import {displayDateNoHours, shortDate} from '../../../helpers/dateHelpers';
import {formatPercentage} from '../../../helpers/formatters';
import {firstUpperTranslated} from '../../../services/translationService';
import {CollectionStat} from '../../../state/domain-models/collection-stat/collectionStatModels';
import {colorFor} from '../../report/helpers/lineChartHelper';
import {DispatchToProps, StateToProps} from '../containers/CollectionGraphContainer';

interface CollectionStatData {
  data: CollectionStat[];
}

const ticks: number[] = [0, 20, 40, 60, 80, 100];
const lineMargins: Partial<Margin> = {top: 40, right: 0, bottom: 0, left: 0};
const domains: [AxisDomain, AxisDomain] = ['dataMin - 100000', 'dataMax + 100000'];

const style: React.CSSProperties = {
  padding: 8,
};

const formatTime = (time: number) =>
  shortDate(time * 1000);

const CustomizedTooltip = (props) => {
  const {active, payload} = props;
  if (active && payload != null) {
    const {payload: {id, collectionPercentage}} = payload[0];

    return (
      <Paper style={{...paperStyle, ...style}}>
        <Column>
          <Row>
            <Normal style={{marginRight: 4}}>{displayDateNoHours(id * 1000)}:</Normal>
            <Bold>{formatPercentage(collectionPercentage)}</Bold>
          </Row>
        </Column>
      </Paper>
    );
  }
  return null;
};

const CollectionStatBarChartComponent = ({data}: CollectionStatData) =>
  (
    <ColumnCenter className="align-items">
      <ResponsiveContainer aspect={2.5} width="95%" height="99%">
        <BarChart
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
          <YAxis
            height={100}
            ticks={ticks}
            label={{value: '[%]', angle: -90, position: 'insideLeft'}}
          />
          <CartesianGrid strokeDasharray="3 3"/>
          <Tooltip content={<CustomizedTooltip/>}/>
          <Bar
            dataKey="collectionPercentage"
            minPointSize={3}
            fill={colorFor('count')}
            barSize={100}
          />
        </BarChart>
      </ResponsiveContainer>
      <TimestampInfoMessage/>
    </ColumnCenter>
  );

const WrappedCollectionStatBarChart =
  withEmptyContent<CollectionStatData & WithEmptyContentProps>(CollectionStatBarChartComponent);

type Props = StateToProps & DispatchToProps;

export const CollectionStatBarChart = ({
  collectionStats,
  fetchCollectionStats,
  isFetching,
  parameters,
}: Props) => {
  React.useEffect(() => {
    fetchCollectionStats(parameters);
  }, [parameters]);

  const data = toArray(collectionStats);

  return (
    <Loader isFetching={isFetching}>
      <WrappedCollectionStatBarChart
        data={data}
        hasContent={data.length > 0}
        noContentText={firstUpperTranslated('no meters')}
      />
    </Loader>
  );
};
