import * as React from 'react';
import {TooltipPayload, TooltipProps} from 'recharts';
import {withEmptyContent, WithEmptyContentProps} from '../../../../components/hoc/withEmptyContent';
import {toggle} from '../../../../helpers/collections';
import {Maybe} from '../../../../helpers/Maybe';
import {firstUpperTranslated} from '../../../../services/translationService';
import {GraphContents} from '../../../../state/report/reportModels';
import {ThresholdQuery} from '../../../../state/user-selection/userSelectionModels';
import {Dictionary, uuid} from '../../../../types/Types';
import {toReferenceLineProps} from '../../helpers/lineChartHelper';
import {VisibilitySummaryProps} from '../VisibilitySummary';
import {ActiveDot, ActiveDotReChartProps} from './ActiveDot';
import {CustomizedTooltip} from './CustomizedTooltip';
import {Dot, KeyedDotProps} from './Dot';
import {LineChart, LineChartProps} from './LineChart';

interface MouseOverProps {
  isTooltipActive: boolean;
  chartX: number;
  chartY: number;
  activeTooltipIndex: number;
  activePayload: TooltipPayload[];
}

type GraphContentWrapperProps = LineChartProps & WithEmptyContentProps;

const LineChartWrapper = withEmptyContent<GraphContentWrapperProps>(LineChart);

interface State {
  hiddenKeys: string[];
}

interface Props {
  outerHiddenKeys: uuid[];
  graphContents: GraphContents;
  isSideMenuOpen: boolean;
  hasMeters: boolean;
  hasContent: boolean;
  threshold?: ThresholdQuery;
  visibilitySummary?: VisibilitySummaryProps;
}

export class LineChartComponent extends React.Component<Props, State> {

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
      hasMeters,
      hasContent,
      isSideMenuOpen,
      outerHiddenKeys,
      threshold,
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
      referenceLineProps: toReferenceLineProps(axes, threshold),
      setTooltipPayload: this.setTooltipPayload,
    };

    return <LineChartWrapper {...wrapperProps}/>;
  }

  legendClick = ({value}) => this.setState({hiddenKeys: toggle(value, this.state.hiddenKeys)});

  renderActiveDot = (props: ActiveDotReChartProps) =>
    <ActiveDot {...props} activeDataKey={this.activeDataKey}/>

  renderTooltipContent = (_: TooltipProps) =>
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
