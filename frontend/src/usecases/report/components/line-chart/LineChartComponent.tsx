import * as React from 'react';
import {TooltipPayload, TooltipProps} from 'recharts';
import {withEmptyContent, WithEmptyContentProps} from '../../../../components/hoc/withEmptyContent';
import {toggle} from '../../../../helpers/collections';
import {Maybe} from '../../../../helpers/Maybe';
import {firstUpperTranslated} from '../../../../services/translationService';
import {Dictionary, uuid} from '../../../../types/Types';
import {OwnProps, StateToProps} from '../../containers/MeasurementLineChartContainer';
import {ActiveDot, ActiveDotReChartProps} from './ActiveDot';
import {CustomizedTooltip} from './CustomizedTooltip';
import {Dot, KeyedDotProps} from './Dot';
import {GraphContentProps, LineChart} from './LineChart';

interface GraphComponentState {
  hiddenKeys: string[];
}

interface MouseOverProps {
  isTooltipActive: boolean;
  chartX: number;
  chartY: number;
  activeTooltipIndex: number;
  activePayload: TooltipPayload[];
}

type GraphContentWrapperProps = GraphContentProps & WithEmptyContentProps;

const LineChartWrapper = withEmptyContent<GraphContentWrapperProps>(LineChart);

type Props = StateToProps & OwnProps;

export class LineChartComponent extends React.Component<Props, GraphComponentState> {

  private dots: Dictionary<Dictionary<{dataKey: uuid; cy: number}>> = {};

  private tooltipPayload: TooltipPayload;

  private activeDataKey: uuid;

  constructor(props) {
    super(props);
    this.state = {hiddenKeys: []};
  }

  render() {
    const {
      graphContents: {axes, data, lines, legend},
      isSideMenuOpen,
      outerHiddenKeys,
      hasMeters,
      hasContent,
    } = this.props;

    const wrapperProps: GraphContentWrapperProps = {
      renderTooltipContent: this.renderTooltipContent,
      data,
      hasContent,
      key: `graph-update-${isSideMenuOpen}`,
      legend,
      legendClick: this.legendClick,
      linesProps: {
        axes,
        lines,
        outerHiddenKeys,
        hiddenKeys: this.state.hiddenKeys,
        renderDot: this.renderAndStoreDot,
        renderActiveDot: this.renderActiveDot,
      },
      noContentText: firstUpperTranslated(hasMeters ? 'no measurements' : 'no meters'),
      setTooltipPayload: this.setTooltipPayload,
    };

    return <LineChartWrapper {...wrapperProps}/>;
  }

  legendClick = ({value}) => this.setState({hiddenKeys: toggle(value, this.state.hiddenKeys)});

  renderActiveDot = (props: ActiveDotReChartProps) =>
    <ActiveDot {...props} activeDataKey={this.activeDataKey}/>

  renderTooltipContent = (props: TooltipProps) =>
    this.tooltipPayload ? <CustomizedTooltip {...this.tooltipPayload}/> : null

  renderAndStoreDot = ({dataKey, ...rest}: KeyedDotProps) => {
    const {index, cy} = rest;
    this.dots = {
      ...this.dots,
      [index]: {...this.dots[index], [dataKey]: {dataKey, cy}},
    };
    return (<Dot {...rest} />);
  }

  setTooltipPayload = ({isTooltipActive, chartY, activeTooltipIndex, activePayload}: MouseOverProps) => {
    if (isTooltipActive) {
      const closestLine = this.findClosestLineDataKey(activeTooltipIndex, chartY);
      if (closestLine) {
        this.activeDataKey = closestLine;
        this.tooltipPayload = activePayload.filter(({dataKey}) => this.activeDataKey === dataKey)[0];
      }
    }
  }

  findClosestLineDataKey = (index: number, mouseY: number): uuid | undefined =>
    Maybe.maybe(this.dots[index])
      .map(activeDots => {
        const sortedActiveDots = Object.keys(activeDots)
          .map((id) => activeDots[id])
          .filter(({cy}) => cy)
          .map(({dataKey, cy}) => ({dataKey, yDistanceFromMouse: Math.abs(cy - mouseY)}))
          .sort(({yDistanceFromMouse: distA}, {yDistanceFromMouse: distB}) => distA - distB);
        return sortedActiveDots.length ? sortedActiveDots[0].dataKey : undefined;
      })
      .getOrElseUndefined()

}
