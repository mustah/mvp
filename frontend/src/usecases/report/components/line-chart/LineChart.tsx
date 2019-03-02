import * as React from 'react';
import {
  AxisDomain,
  CartesianGrid,
  ContentRenderer,
  Legend,
  LegendPayload,
  LineChart as ReChartLineChart,
  ResponsiveContainer,
  Tooltip,
  TooltipProps,
  XAxis
} from 'recharts';
import {ColumnCenter} from '../../../../components/layouts/column/Column';
import {TimestampInfoMessage} from '../../../../components/timestamp-info-message/TimestampInfoMessage';
import {shortTimestamp} from '../../../../helpers/dateHelpers';
import {useResizeWindow} from '../../../../hooks/resizeWindowHook';
import {Children, OnClick} from '../../../../types/Types';

const lineMargins: React.CSSProperties = {top: 40, right: 0, bottom: 0, left: 0};
const domains: [AxisDomain, AxisDomain] = ['dataMin', 'dataMax'];

export interface GraphContentProps {
  content?: React.ReactElement<any> | React.StatelessComponent<any> | ContentRenderer<TooltipProps>;
  data?: object[];
  key: string;
  lines: Children[];
  legend: LegendPayload[];
  legendClick: OnClick;
  setTooltipPayload: OnClick;
}

export const LineChart =
  ({content, data, key, legendClick, lines, legend, setTooltipPayload}: GraphContentProps) => {
    const {resized} = useResizeWindow();

    return (
      <ColumnCenter className="align-items" key={`${key}-${resized}`}>
        <ResponsiveContainer aspect={2.5} width="95%" height="99%">
          <ReChartLineChart
            width={10}
            height={50}
            data={data}
            margin={lineMargins}
            onMouseMove={setTooltipPayload}
          >
            <XAxis
              dataKey="name"
              domain={domains}
              scale="time"
              tickFormatter={shortTimestamp}
              type="number"
            />
            <CartesianGrid strokeDasharray="3 3"/>
            <Tooltip content={content}/>
            <Legend onClick={legendClick} payload={legend}/>
            {lines}
          </ReChartLineChart>
        </ResponsiveContainer>
        <TimestampInfoMessage/>
      </ColumnCenter>
    );
  };
