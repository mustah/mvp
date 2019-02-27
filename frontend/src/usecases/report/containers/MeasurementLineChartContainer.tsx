import * as React from 'react';
import {connect} from 'react-redux';
import {
  CartesianGrid,
  ContentRenderer,
  LabelProps,
  Legend,
  LegendPayload,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  TooltipProps,
  XAxis,
  YAxis,
} from 'recharts';
import {DateRange, Period} from '../../../components/dates/dateModels';
import {withEmptyContent, WithEmptyContentProps} from '../../../components/hoc/withEmptyContent';
import {ColumnCenter} from '../../../components/layouts/column/Column';
import {DispatchToProps} from '../../../components/tabs/components/MainContentTabs';
import {TimestampInfoMessage} from '../../../components/timestamp-info-message/TimestampInfoMessage';
import {toggle} from '../../../helpers/collections';
import {shortTimestamp} from '../../../helpers/dateHelpers';
import {Maybe} from '../../../helpers/Maybe';
import {RootState} from '../../../reducers/rootReducer';
import {firstUpperTranslated} from '../../../services/translationService';
import {hasMeasurements} from '../../../state/ui/graph/measurement/measurementSelectors';
import {isSideMenuOpen} from '../../../state/ui/uiSelectors';
import {getSelectedPeriod} from '../../../state/user-selection/userSelectionSelectors';
import {Children, Dictionary, OnClick, uuid} from '../../../types/Types';
import {ActiveDot, ActiveDotReChartProps} from '../components/line-chart/ActiveDot';
import {CustomizedTooltip} from '../components/line-chart/CustomizedTooltip';
import {Dot, DotReChartProps} from '../components/line-chart/Dot';
import {ActiveDataPoint, GraphContents, LineProps} from '../reportModels';
import {hasLegendItems} from '../reportSelectors';

export interface GraphProps {
  outerHiddenKeys: uuid[];
  graphContents: GraphContents;
}

interface StateToProps {
  customDateRange: Maybe<DateRange>;
  isSideMenuOpen: boolean;
  period: Period;
  hasMeters: boolean;
  hasContent: boolean;
}

interface GraphComponentState {
  hiddenKeys: string[];
  resized: boolean;
}

interface MouseOverProps {
  isTooltipActive: boolean;
  chartX: number;
  chartY: number;
  activeTooltipIndex: number;
  activePayload: ActiveDataPoint[];
}

interface GraphContentProps {
  content?: React.ReactElement<any> | React.StatelessComponent<any> | ContentRenderer<TooltipProps>;
  data?: object[];
  key: string;
  lines: Children[];
  legend: LegendPayload[];
  legendClick: OnClick;
  setTooltipPayload: OnClick;
}

const renderGraphContents = (
  {lines, axes: {left, right}}: GraphContents,
  outerHiddenKeys,
  hiddenKeys,
  renderDot: (props) => React.ReactNode,
  renderActiveDot: (props) => React.ReactNode,
): Children[] => {

  const components: Children[] = lines
    .filter((line) => hiddenKeys.findIndex((hiddenKey) => line.dataKey.startsWith(hiddenKey)) === -1)
    .filter((line) => outerHiddenKeys.indexOf(line.id) === -1)
    .map((props: LineProps, index: number) => {
      const newDot = (apiDotProps) => renderDot({...apiDotProps, dataKey: props.dataKey});
      return (
        <Line
          animationDuration={600}
          key={index}
          type="monotone"
          connectNulls={true}
          {...props}
          activeDot={renderActiveDot}
          dot={newDot}
        />
      );
    });

  if (left) {
    const leftLabel: LabelProps = {value: left, position: 'insideLeft', angle: -90, dx: 10};
    components.push(<YAxis key="leftYAxis" label={leftLabel} yAxisId="left"/>);
  }

  if (right) {
    const rightLabel: LabelProps = {value: right, position: 'insideRight', angle: 90, dy: -10};
    components.push((
      <YAxis key="rightYAxis" label={rightLabel} yAxisId="right" orientation="right"/>
    ));
  }

  return components;
};

const lineMargins: React.CSSProperties = {top: 40, right: 0, bottom: 0, left: 0};

const LineChartComponent =
  ({content, data, key, legendClick, lines, legend, setTooltipPayload}: GraphContentProps) => (
    <ColumnCenter className="align-items" key={key}>
      <ResponsiveContainer aspect={2.5} width="95%" height="99%">
        <LineChart
          width={10}
          height={50}
          data={data}
          margin={lineMargins}
          onMouseMove={setTooltipPayload}
        >
          <XAxis
            dataKey="name"
            domain={['dataMin', 'dataMax']}
            scale="time"
            tickFormatter={shortTimestamp}
            type="number"
          />
          <CartesianGrid strokeDasharray="3 3"/>
          <Tooltip content={content}/>
          <Legend onClick={legendClick} payload={legend}/>
          {lines}
        </LineChart>
      </ResponsiveContainer>
      <TimestampInfoMessage/>
    </ColumnCenter>
  );

type Props = GraphProps & StateToProps;

type GraphContentWrapperProps = GraphContentProps & WithEmptyContentProps;

const LineChartWrapper = withEmptyContent<GraphContentWrapperProps>(LineChartComponent);

class GraphComponent extends React.Component<Props, GraphComponentState> {

  private dots: Dictionary<Dictionary<{dataKey: uuid; cy: number}>> = {};

  private tooltipPayload: ActiveDataPoint;

  private activeDataKey: uuid;

  constructor(props) {
    super(props);
    this.state = {hiddenKeys: [], resized: false};
  }

  componentDidMount() {
    window.addEventListener('resize', this.updateDimensions);
  }

  componentWillUnmount() {
    window.removeEventListener('resize', this.updateDimensions);
  }

  render() {
    const {
      graphContents,
      isSideMenuOpen,
      outerHiddenKeys,
      hasMeters,
      hasContent
    } = this.props;

    const {hiddenKeys, resized} = this.state;

    const lines: Children[] = renderGraphContents(
      graphContents,
      outerHiddenKeys,
      hiddenKeys,
      this.renderAndStoreDot,
      this.renderActiveDot,
    );
    const {data, legend} = graphContents;

    const wrapperProps: GraphContentWrapperProps = {
      lines,
      data,
      legend,
      content: this.renderToolTip,
      key: `graph-update-${isSideMenuOpen}-${resized}`,
      legendClick: this.legendClick,
      setTooltipPayload: this.setTooltipPayload,
      hasContent,
      noContentText: firstUpperTranslated(hasMeters ? 'no measurements' : 'no meters'),
    };

    return <LineChartWrapper {...wrapperProps}/>;
  }

  updateDimensions = () => this.setState(({resized}) => ({resized: !resized}));

  legendClick = ({value}) => this.setState({hiddenKeys: toggle(value, this.state.hiddenKeys)});

  renderActiveDot = (props: ActiveDotReChartProps) =>
    <ActiveDot {...props} activeDataKey={this.activeDataKey}/>

  renderToolTip = (props: TooltipProps) =>
    this.tooltipPayload ? <CustomizedTooltip {...this.tooltipPayload}/> : null

  renderAndStoreDot = ({dataKey, ...rest}: DotReChartProps & {dataKey: uuid}) => {
    const {index, cy} = rest;
    this.dots = {
      ...this.dots,
      [index]: {...this.dots[index], [dataKey]: {dataKey, cy}},
    };
    return (<Dot {...rest} />);
  }

  setTooltipPayload = ({isTooltipActive, chartY, activeTooltipIndex, activePayload}: MouseOverProps) => {
    if (isTooltipActive) {
      const closestLine = this.findClosestLine(activeTooltipIndex, chartY);
      if (closestLine !== undefined) {
        this.activeDataKey = closestLine;
        this.tooltipPayload = activePayload.filter(({dataKey}) => this.activeDataKey === dataKey)[0];
      }
    }
  }

  findClosestLine = (index: number, mouseY: number): uuid | undefined => {
    const activeDots = this.dots[index];
    if (activeDots === undefined) {
      return undefined;
    }
    const sortedActiveDots = Object.keys(activeDots)
      .map((id) => activeDots[id])
      .filter(({cy}) => cy || cy === 0)
      .map(({dataKey, cy}) => ({dataKey, yDistanceFromMouse: Math.abs(cy - mouseY)}))
      .sort(({yDistanceFromMouse: distA}, {yDistanceFromMouse: distB}) => distA - distB);
    return sortedActiveDots.length ? sortedActiveDots[0].dataKey : undefined;
  }

}

const mapStateToProps = ({report, measurement, userSelection: {userSelection}, ui}: RootState): StateToProps =>
  ({
    ...getSelectedPeriod(userSelection),
    isSideMenuOpen: isSideMenuOpen(ui),
    hasMeters: hasLegendItems(report),
    hasContent: hasMeasurements(measurement.measurementResponse)
  });

export const MeasurementLineChartContainer =
  connect<StateToProps, DispatchToProps, GraphProps>(mapStateToProps)(GraphComponent);
