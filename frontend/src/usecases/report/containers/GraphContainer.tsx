import MenuItem from 'material-ui/MenuItem';
import SelectField from 'material-ui/SelectField';
import * as React from 'react';
import {connect} from 'react-redux';
import {
  CartesianGrid, Legend, Line, LineChart, ResponsiveContainer, Tooltip, TooltipProps, XAxis,
  YAxis,
} from 'recharts';
import {bindActionCreators} from 'redux';
import {HasContent} from '../../../components/content/HasContent';
import {DateRange, Period} from '../../../components/dates/dateModels';
import {MissingDataTitle} from '../../../components/texts/Titles';
import {formatLabelTimeStamp} from '../../../helpers/dateHelpers';
import {Maybe} from '../../../helpers/Maybe';
import {RootState} from '../../../reducers/rootReducer';
import {firstUpperTranslated} from '../../../services/translationService';
import {
  fetchMeasurements,
  mapApiResponseToGraphData,
  selectQuantities,
} from '../../../state/ui/graph/measurement/measurementActions';
import {allQuantities, Quantity} from '../../../state/ui/graph/measurement/measurementModels';
import {getSelectedPeriod} from '../../../state/user-selection/userSelectionSelectors';
import {Children, Dictionary, uuid} from '../../../types/Types';
import {ActiveDot, ActiveDotReChartProps} from '../components/ActiveDot';
import {CustomizedTooltip} from '../components/CustomizedTooltip';
import {Dot, DotReChartProps} from '../components/Dot';
import {ActiveDataPoint, GraphContents, LineProps} from '../reportModels';

interface StateToProps {
  period: Period;
  customDateRange: Maybe<DateRange>;
  selectedListItems: uuid[];
  selectedQuantities: Quantity[];
}

interface State {
  graphContents: GraphContents;
  selectedQuantities: string[];
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

type Props = StateToProps & DispatchToProps;

const margin: React.CSSProperties = {top: 40, right: 0, bottom: 0, left: 0};

const renderGraphContents = (
  {lines, axes}: GraphContents,
  renderDot: (props) => React.ReactNode,
  renderActiveDot: (props) => React.ReactNode,
): Children[] => {

  const components: Children[] = lines.map((props: LineProps, index: number) => {
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
    components.push((
      <YAxis key="leftYAxis" label={axes.left} yAxisId="left"/>
    ));
  }

  if (axes.right) {
    components.push((
      <YAxis key="rightYAxis" label={axes.right} yAxisId="right" orientation="right"/>
    ));
  }

  return components;
};

const emptyGraphContents: GraphContents = {
  axes: {},
  data: [],
  legend: [],
  lines: [],
};

class GraphComponent extends React.Component<Props, State> {

  state: State = {
    graphContents: emptyGraphContents,
    selectedQuantities: [],
  };

  private dots: Dictionary<Dictionary<{dataKey: uuid; cy: number}>> = {};
  private tooltipPayload: ActiveDataPoint;
  private activeDataKey;

  changeQuantities = (event, index, values) => this.props.selectQuantities(values);

  async componentDidMount() {
    const {selectedListItems, period, customDateRange, selectedQuantities} = this.props;
    const graphData = await fetchMeasurements(selectedQuantities, selectedListItems, period, customDateRange);

    this.setState({
      ...this.state,
      graphContents: mapApiResponseToGraphData(graphData),
      selectedQuantities,
    });
  }

  async componentWillReceiveProps({selectedListItems, period, customDateRange, selectedQuantities}: Props) {
    const somethingChanged = true || period !== this.props.period; // TODO: Should not always return "true"
    if (somethingChanged) {
      const graphData = await fetchMeasurements(selectedQuantities, selectedListItems, period, customDateRange);
      this.setState({
        ...this.state,
        graphContents: mapApiResponseToGraphData(graphData),
        selectedQuantities,
      });
      this.resetDots();
    }
  }

  renderActiveDot = (props: ActiveDotReChartProps) => (<ActiveDot {...props} activeDataKey={this.activeDataKey}/>);
  renderToolTip = (props: TooltipProps) => (this.tooltipPayload) ? <CustomizedTooltip {...this.tooltipPayload}/> : null;
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
        this.activeDataKey = this.findClosestLine(activeTooltipIndex, chartY);
        this.tooltipPayload = activePayload.filter(({dataKey}) => this.activeDataKey === dataKey)[0];
      }
    }
  }

  findClosestLine = (index: number, mouseY: number): uuid | undefined => {
    const activeDots = this.dots[index];
    if (activeDots === undefined) {
      return undefined;
    }
    const sortedActiveDots = Object.keys(activeDots).map((id) => activeDots[id])
      .filter(({cy}) => cy || cy === 0)
      .map(({dataKey, cy}) => ({dataKey, yDistanceFromMouse: Math.abs(cy - mouseY)}))
      .sort(({yDistanceFromMouse: distA}, {yDistanceFromMouse: distB}) => distA - distB);
    return sortedActiveDots[0].dataKey;
  }

  render() {
    const {selectedQuantities} = this.props;
    const {graphContents} = this.state;
    const lines = renderGraphContents(graphContents, this.renderAndStoreDot, this.renderActiveDot);
    const {data, legend} = graphContents;

    const quantityMenuItem = (quantity: string) => (
      <MenuItem
        key={quantity}
        checked={selectedQuantities.includes(quantity)}
        value={quantity}
        primaryText={quantity}
      />
    );

    // TODO: [!Carl]
    // ResponsiveContainer is a bit weird, if we leave out the dimensions of the containing <div>,
    // it breaks. Setting width of ResponsiveContainer to 100% will cause the menu to overlap when
    // toggled

    const missingData = (
      <MissingDataTitle
        title={firstUpperTranslated('select meters to include in graph')}
      />
    );

    return (
      <div>
        <div style={{padding: '20px 20px 0px'}}>
          <SelectField
            multiple={true}
            hintText={firstUpperTranslated('select quantities')}
            value={selectedQuantities}
            onChange={this.changeQuantities}
          >
            {allQuantities.heat.map(quantityMenuItem)}
          </SelectField>
        </div>
        <HasContent
          hasContent={data.length > 0}
          fallbackContent={missingData}
        >
          <div>
            <ResponsiveContainer width="90%" aspect={2.5}>
              <LineChart
                width={10}
                height={50}
                data={data}
                margin={margin}
                onMouseMove={this.setTooltipPayload}
              >
                <XAxis
                  dataKey="name"
                  domain={['dataMin', 'dataMax']}
                  scale="time"
                  tickFormatter={formatLabelTimeStamp}
                  type="number"
                />
                <CartesianGrid strokeDasharray="3 3"/>
                <Tooltip content={this.renderToolTip}/>
                <Legend payload={legend}/>
                {lines}
              </LineChart>
            </ResponsiveContainer>
          </div>
        </HasContent>
      </div>
    );
  }
}

const mapStateToProps = (
  {
    report: {selectedListItems},
    userSelection: {userSelection},
    ui: {measurements: {selectedQuantities}},
  }: RootState): StateToProps =>
  ({
    ...getSelectedPeriod(userSelection),
    selectedListItems,
    selectedQuantities,
  });

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  selectQuantities,
}, dispatch);

export const GraphContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(GraphComponent);
