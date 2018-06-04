import 'GraphContainer.scss';
import * as React from 'react';
import {connect} from 'react-redux';
import {
  CartesianGrid,
  ContentRenderer,
  Legend,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  TooltipProps,
  XAxis,
  YAxis,
} from 'recharts';
import {bindActionCreators} from 'redux';
import {DateRange, Period} from '../../../components/dates/dateModels';
import {withEmptyContent, WithEmptyContentProps} from '../../../components/hoc/withEmptyContent';
import {Medium} from '../../../components/indicators/indicatorWidgetModels';
import {Row} from '../../../components/layouts/row/Row';
import {Loader} from '../../../components/loading/Loader';
import {toggle} from '../../../helpers/collections';
import {timestamp} from '../../../helpers/dateHelpers';
import {Maybe} from '../../../helpers/Maybe';
import {RootState} from '../../../reducers/rootReducer';
import {firstUpperTranslated} from '../../../services/translationService';
import {
  fetchMeasurements,
  selectQuantities,
} from '../../../state/ui/graph/measurement/measurementActions';
import {initialState, Quantity} from '../../../state/ui/graph/measurement/measurementModels';
import {getSelectedPeriod} from '../../../state/user-selection/userSelectionSelectors';
import {Children, Dictionary, ErrorResponse, OnClick, uuid} from '../../../types/Types';
import {logout} from '../../auth/authActions';
import {OnLogout} from '../../auth/authModels';
import {ActiveDot, ActiveDotReChartProps} from '../components/ActiveDot';
import {CustomizedTooltip} from '../components/CustomizedTooltip';
import {Dot, DotReChartProps} from '../components/Dot';
import {QuantityDropdown} from '../components/QuantityDropdown';
import {ActiveDataPoint, GraphContents, LineProps, ProprietaryLegendProps} from '../reportModels';

interface StateToProps {
  customDateRange: Maybe<DateRange>;
  period: Period;
  selectedIndicators: Medium[];
  selectedListItems: uuid[];
  selectedQuantities: Quantity[];
}

export interface GraphContainerState {
  hiddenKeys: string[];
  graphContents: GraphContents;
  isFetching: boolean;
  error: Maybe<ErrorResponse>;
}

interface DispatchToProps {
  selectQuantities: (quantities: Quantity[]) => void;
  logout: OnLogout;
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
  lines: Children[];
  legend: ProprietaryLegendProps[];
  legendClick: OnClick;
  setTooltipPayload: OnClick;
}

export type OnUpdateGraph = (state: GraphContainerState) => void;

type Props = StateToProps & DispatchToProps;

type GraphContentWrapperProps = GraphContentProps & WithEmptyContentProps;

const margin: React.CSSProperties = {top: 40, right: 0, bottom: 0, left: 0};

const renderGraphContents = (
  {lines, axes}: GraphContents,
  hiddenKeys,
  renderDot: (props) => React.ReactNode,
  renderActiveDot: (props) => React.ReactNode,
): Children[] => {

  const components: Children[] = lines
    .filter((line) => hiddenKeys.findIndex((hiddenKey) => line.dataKey.startsWith(hiddenKey)) === -1)
    .map((props: LineProps, index: number) => {
      const newDot = (apiDotProps) => renderDot({...apiDotProps, dataKey: props.dataKey});
      return (
        <Line
          key={index}
          type="monotone"
          connectNulls={true}
          {...props}
          activeDot={renderActiveDot}
          dot={newDot}
        />
      );
    });

  if (axes.left) {
    components.push(<YAxis key="leftYAxis" label={axes.left} yAxisId="left"/>);
  }

  if (axes.right) {
    components.push((
      <YAxis key="rightYAxis" label={axes.right} yAxisId="right" orientation="right"/>
    ));
  }

  return components;
};

const GraphContent =
  ({data, setTooltipPayload, legendClick, content, lines, legend}: GraphContentProps) => (
    <Row className="GraphContainer">
      <Row className="Graph">
        <ResponsiveContainer aspect={2.5}>
          <LineChart
            width={10}
            height={50}
            data={data}
            margin={margin}
            onMouseMove={setTooltipPayload}
          >
            <XAxis
              dataKey="name"
              domain={['dataMin', 'dataMax']}
              scale="time"
              tickFormatter={timestamp}
              type="number"
            />
            <CartesianGrid strokeDasharray="3 3"/>
            <Tooltip content={content}/>
            <Legend onClick={legendClick} payload={legend}/>
            {lines}
          </LineChart>
        </ResponsiveContainer>
      </Row>
    </Row>
  );

const GraphContentWrapper = withEmptyContent<GraphContentWrapperProps>(GraphContent);

class GraphComponent extends React.Component<Props, GraphContainerState> {

  state: GraphContainerState = {...initialState};

  private dots: Dictionary<Dictionary<{dataKey: uuid; cy: number}>> = {};

  private tooltipPayload: ActiveDataPoint;

  private activeDataKey: uuid;

  async componentDidMount() {
    const {selectedListItems, period, customDateRange, selectedQuantities, logout, selectedIndicators} = this.props;
    this.setState({isFetching: true});
    await fetchMeasurements(
      selectedIndicators,
      selectedQuantities,
      selectedListItems,
      period,
      customDateRange,
      this.updateState,
      logout,
    );
  }

  async componentWillReceiveProps({
    selectedListItems, period, customDateRange, selectedQuantities, logout, selectedIndicators,
  }: Props) {
    const somethingChanged = true || period !== this.props.period; // TODO: Should not always
                                                                   // return "true"
    if (somethingChanged) {
      this.setState({isFetching: true});
      this.resetDots();
      await fetchMeasurements(
        selectedIndicators,
        selectedQuantities,
        selectedListItems,
        period,
        customDateRange,
        this.updateState,
        logout,
      );
    }
  }

  render() {
    const {selectedListItems, selectedQuantities, selectQuantities, selectedIndicators} = this.props;
    const {graphContents, hiddenKeys, isFetching, error} = this.state;
    const lines: Children[] = renderGraphContents(
      graphContents,
      hiddenKeys,
      this.renderAndStoreDot,
      this.renderActiveDot,
    );
    const {data, legend} = graphContents;

    const legendClick = ({value}: any) => this.setState({
      hiddenKeys: toggle(
        value,
        this.state.hiddenKeys,
      ),
    });

    const wrapperProps: GraphContentWrapperProps = {
      lines,
      data,
      legend,
      content: this.renderToolTip,
      legendClick,
      setTooltipPayload: this.setTooltipPayload,
      hasContent: selectedListItems.length > 0,
      noContentText: firstUpperTranslated('select meters to include in graph'),
    };

    return (
      <div>
        <QuantityDropdown
          selectedIndicators={selectedIndicators}
          selectedQuantities={selectedQuantities}
          selectQuantities={selectQuantities}
        />
        <Loader isFetching={isFetching} error={error} clearError={this.clearError}>
          <GraphContentWrapper {...wrapperProps}/>
        </Loader>
      </div>
    );
  }

  updateState = (state: GraphContainerState) => this.setState({...state});

  clearError = async () => {
    const {selectedIndicators, selectedListItems, period, customDateRange, selectedQuantities, logout} = this.props;
    this.setState({error: Maybe.nothing(), isFetching: true});
    this.resetDots();
    await fetchMeasurements(
      selectedIndicators,
      selectedQuantities,
      selectedListItems,
      period,
      customDateRange,
      this.updateState,
      logout,
    );
  }

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

  resetDots = () => this.dots = {};

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

const mapStateToProps =
  ({
    report: {selectedListItems},
    userSelection: {userSelection},
    ui: {measurements: {selectedQuantities}, indicator: {selectedIndicators: {report}}},
  }: RootState): StateToProps =>
    ({
      ...getSelectedPeriod(userSelection),
      selectedListItems,
      selectedQuantities,
      selectedIndicators: report,
    });

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  selectQuantities,
  logout,
}, dispatch);

export const GraphContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(GraphComponent);
