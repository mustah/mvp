import * as React from 'react';
import {TooltipProps} from 'recharts';
import {withEmptyContent, WithEmptyContentProps} from '../../../components/hoc/withEmptyContent';
import {toggle} from '../../../helpers/collections';
import {firstUpperTranslated} from '../../../services/translationService';
import {Children, Dictionary, uuid} from '../../../types/Types';
import {OwnProps, StateToProps} from '../containers/MeasurementLineChartContainer';
import {ActiveDataPoint} from '../reportModels';
import {ActiveDot, ActiveDotReChartProps} from './line-chart/ActiveDot';
import {CustomizedTooltip} from './line-chart/CustomizedTooltip';
import {Dot, DotReChartProps} from './line-chart/Dot';
import {GraphContentProps, LineChart} from './line-chart/LineChart';
import {renderLines} from './line-chart/Lines';

interface GraphComponentState {
  hiddenKeys: string[];
}

interface MouseOverProps {
  isTooltipActive: boolean;
  chartX: number;
  chartY: number;
  activeTooltipIndex: number;
  activePayload: ActiveDataPoint[];
}

type GraphContentWrapperProps = GraphContentProps & WithEmptyContentProps;

const LineChartWrapper = withEmptyContent<GraphContentWrapperProps>(LineChart);

type Props = StateToProps & OwnProps;

export class GraphComponent extends React.Component<Props, GraphComponentState> {

  private dots: Dictionary<Dictionary<{dataKey: uuid; cy: number}>> = {};

  private tooltipPayload: ActiveDataPoint;

  private activeDataKey: uuid;

  constructor(props) {
    super(props);
    this.state = {hiddenKeys: []};
  }

  render() {
    const {
      graphContents,
      isSideMenuOpen,
      outerHiddenKeys,
      hasMeters,
      hasContent
    } = this.props;

    const {hiddenKeys} = this.state;

    const lines: Children[] = renderLines(
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
      key: `graph-update-${isSideMenuOpen}`,
      legendClick: this.legendClick,
      setTooltipPayload: this.setTooltipPayload,
      hasContent,
      noContentText: firstUpperTranslated(hasMeters ? 'no measurements' : 'no meters'),
    };

    return <LineChartWrapper {...wrapperProps}/>;
  }

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
