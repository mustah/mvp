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
import {Column} from '../../../components/layouts/column/Column';
import {TableInfoText} from '../../../components/table/TableInfoText';
import {toggle} from '../../../helpers/collections';
import {timestamp} from '../../../helpers/dateHelpers';
import {Maybe} from '../../../helpers/Maybe';
import {RootState} from '../../../reducers/rootReducer';
import {firstUpperTranslated} from '../../../services/translationService';
import {Medium, Quantity} from '../../../state/ui/graph/measurement/measurementModels';
import {selectQuantities} from '../../../state/ui/indicator/indicatorActions';
import {isSideMenuOpen} from '../../../state/ui/uiSelectors';
import {getSelectedPeriod} from '../../../state/user-selection/userSelectionSelectors';
import {Children, Dictionary, OnClick, uuid} from '../../../types/Types';
import {ActiveDot, ActiveDotReChartProps} from '../components/graph/ActiveDot';
import {CustomizedTooltip} from '../components/graph/CustomizedTooltip';
import {Dot, DotReChartProps} from '../components/graph/Dot';
import {QuantityDropdown} from '../components/QuantityDropdown';
import {ActiveDataPoint, GraphContents, LineProps, ProprietaryLegendProps} from '../reportModels';
import './GraphContainer.scss';

interface StateToProps {
  customDateRange: Maybe<DateRange>;
  isSideMenuOpen: boolean;
  period: Period;
  selectedIndicators: Medium[];
  selectedListItems: uuid[];
  selectedQuantities: Quantity[];
}

interface OwnProps {
  outerHiddenKeys: string[];
  graphContents: GraphContents;
}

interface GraphComponentState {
  hiddenKeys: string[];
  resized: boolean;
}

interface DispatchToProps {
  selectQuantities: (quantities: Quantity[]) => void;
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
  legend: ProprietaryLegendProps[];
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
    components.push(<YAxis key="leftYAxis" label={left} yAxisId="left"/>);
  }

  if (right) {
    components.push((
      <YAxis key="rightYAxis" label={right} yAxisId="right" orientation="right"/>
    ));
  }

  return components;
};

const lineMargins: React.CSSProperties = {top: 40, right: 0, bottom: 0, left: 0};

const GraphContent =
  ({content, data, key, legendClick, lines, legend, setTooltipPayload}: GraphContentProps) => (
    <Column className="GraphContainer" key={key}>
      <ResponsiveContainer aspect={2.5} width="99%" height="99%">
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
            tickFormatter={timestamp}
            type="number"
          />
          <CartesianGrid strokeDasharray="3 3"/>
          <Tooltip content={content}/>
          <Legend onClick={legendClick} payload={legend}/>
          {lines}
        </LineChart>
      </ResponsiveContainer>
      <TableInfoText/>
    </Column>
  );

type Props = OwnProps & StateToProps & DispatchToProps;

type GraphContentWrapperProps = GraphContentProps & WithEmptyContentProps;

const GraphContentWrapper = withEmptyContent<GraphContentWrapperProps>(GraphContent);

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
      selectedListItems,
      selectedQuantities,
      selectQuantities,
      selectedIndicators,
    } = this.props;

    const {hiddenKeys} = this.state;

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
      key: `graph-update-${isSideMenuOpen}-${this.state.resized}`,
      legendClick: this.legendClick,
      setTooltipPayload: this.setTooltipPayload,
      hasContent: selectedListItems.length > 0,
      noContentText: firstUpperTranslated('select meters to include in graph'),
    };

    return (
      <React.Fragment>
        <QuantityDropdown
          selectedIndicators={selectedIndicators}
          selectedQuantities={selectedQuantities}
          selectQuantities={selectQuantities}
        />
        <GraphContentWrapper {...wrapperProps}/>
      </React.Fragment>
    );
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

const mapStateToProps =
  ({
    report: {selectedListItems},
    userSelection: {userSelection},
    ui,
  }: RootState): StateToProps => {
    const {indicator: {selectedIndicators: {report}, selectedQuantities}} = ui;
    return ({
      ...getSelectedPeriod(userSelection),
      isSideMenuOpen: isSideMenuOpen(ui),
      selectedListItems,
      selectedQuantities,
      selectedIndicators: report,
    });
  };

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  selectQuantities,
}, dispatch);

export const GraphContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(GraphComponent);
