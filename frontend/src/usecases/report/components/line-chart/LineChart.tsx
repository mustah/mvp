import * as React from 'react';
import {
  AxisDomain,
  CartesianGrid,
  ContentRenderer,
  DotProps,
  LabelProps,
  Legend,
  LegendPayload,
  Line,
  LineChart as ReChartLineChart,
  Margin,
  ReferenceLine,
  ReferenceLineProps,
  ResponsiveContainer,
  Tooltip,
  TooltipProps,
  XAxis,
  YAxis
} from 'recharts';
import {ResponsiveContentHeight} from '../../../../components/resize/ResponsiveContentHeight';
import {TimestampInfoMessage} from '../../../../components/timestamp-info-message/TimestampInfoMessage';
import {shortTimestamp} from '../../../../helpers/dateHelpers';
import {AxesProps, LineProps} from '../../../../state/report/reportModels';
import {Children, OnClickEventHandler, uuid} from '../../../../types/Types';
import {ActiveDotReChartProps} from './ActiveDot';

const lineMargins: Partial<Margin> = {top: 40, right: 0, bottom: 0, left: 0};
const domains: [AxisDomain, AxisDomain] = ['dataMin', 'dataMax'];

interface LinesProps {
  axes: AxesProps;
  lines: LineProps[];
  outerHiddenKeys: uuid[];
  hiddenKeys: string[];
  renderDot: (props) => React.ReactNode;
  renderActiveDot: (props: ActiveDotReChartProps) => React.ReactNode;
}

export interface LineChartProps {
  linesProps: LinesProps;
  renderTooltipContent: ContentRenderer<TooltipProps>;
  data?: object[];
  key: string;
  legend: LegendPayload[];
  legendClick: OnClickEventHandler;
  setTooltipPayload: OnClickEventHandler;
  referenceLineProps?: ReferenceLineProps;
}

const renderLines = ({lines, hiddenKeys, outerHiddenKeys, renderDot, renderActiveDot}: LinesProps): Children[] =>
  lines
    .filter(line => hiddenKeys.findIndex((hiddenKey) => line.dataKey.startsWith(hiddenKey)) === -1)
    .filter(line => outerHiddenKeys.indexOf(line.id) === -1)
    .map((props: LineProps, index: number) => {
      const newDot = (dotProps: DotProps) => renderDot({...dotProps, dataKey: props.dataKey});
      return (
        <Line
          activeDot={renderActiveDot}
          animationEasing="linear"
          animationDuration={600}
          connectNulls={true}
          dot={newDot}
          key={index}
          type="monotone"
          {...props}
        />
      );
    });

export const LineChart =
  ({
    data,
    key,
    legendClick,
    legend,
    linesProps,
    referenceLineProps,
    renderTooltipContent,
    setTooltipPayload,
  }: LineChartProps) => {
    const {axes: {left, right}} = linesProps;
    const leftLabel: LabelProps = {value: left, position: 'insideLeft', angle: -90, dx: 10};
    const rightLabel: LabelProps = {value: right, position: 'insideRight', angle: 90, dy: -10};

    return (
      <ResponsiveContentHeight className="align-items" key={key}>
        <ResponsiveContainer width="95%" height="99%">
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
            <Tooltip content={renderTooltipContent}/>
            <Legend onClick={legendClick} payload={legend}/>
            {left && <YAxis key="leftYAxis" label={leftLabel} yAxisId="left"/>}
            {right && <YAxis key="rightYAxis" label={rightLabel} yAxisId="right" orientation="right"/>}
            {renderLines(linesProps)}
            {referenceLineProps && <ReferenceLine {...referenceLineProps}/>}
          </ReChartLineChart>
        </ResponsiveContainer>
        <TimestampInfoMessage/>
      </ResponsiveContentHeight>
    );
  };
