import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {withContent} from '../../../components/hoc/withContent';
import {Period} from '../../../components/dates/dateModels';
import {RootState} from '../../../reducers/rootReducer';
import {isReportPage} from '../../../selectors/routerSelectors';
import {HasContent, OnClick, OnClickWithId, Visible} from '../../../types/Types';
import {Legend} from '../components/Legend';
import {deleteItem, hideAllLines, removeSelectedListItems, toggleLine} from '../reportActions';
import {LegendItem, ReportState} from '../reportModels';
import {getLegendItems} from '../reportSelectors';

export interface StateToProps extends ReportState, HasContent {
  legendItems: LegendItem[];
  isReportPage: boolean;
}

export interface DispatchToProps {
  deleteItem: OnClickWithId;
  hideAllLines: OnClick;
  toggleLine: OnClickWithId;
  removeSelectedListItems: OnClick;
}

export interface OwnProps extends Visible {
  showHideLegend: OnClick;
}

const LegendComponent = withContent<DispatchToProps & StateToProps>(Legend);

const mapStateToProps = ({report, routing}: RootState): StateToProps => {
  const {hiddenLines, resolution, savedReports} = report;
  const legendItems = getLegendItems(report);
  return ({
    legendItems,
    hiddenLines,
    isAllLinesHidden: report.isAllLinesHidden,
    isReportPage: isReportPage(routing),
    hasContent: legendItems.length > 0,
    resolution,
    savedReports,
    timePeriod: {period: Period.latest}, // TODO timePeriod is unused but I could not exclude it from ReportState
  });
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  deleteItem,
  removeSelectedListItems,
  hideAllLines,
  toggleLine,
}, dispatch);

export const LegendContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(LegendComponent);
