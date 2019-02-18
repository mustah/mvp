import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {withEmptyContent, WithEmptyContentProps} from '../../../components/hoc/withEmptyContent';
import {RootState} from '../../../reducers/rootReducer';
import {isReportPage} from '../../../selectors/routerSelectors';
import {firstUpperTranslated} from '../../../services/translationService';
import {OnClick, OnClickWithId} from '../../../types/Types';
import {Legend} from '../components/Legend';
import {deleteItem, hideAllLines, removeSelectedListItems, toggleLine} from '../reportActions';
import {LegendItem, ReportState} from '../reportModels';
import {getLegendItems} from '../reportSelectors';

export interface StateToProps extends ReportState, WithEmptyContentProps {
  legendItems: LegendItem[];
  isReportPage: boolean;
}

export interface DispatchToProps {
  deleteItem: OnClickWithId;
  hideAllLines: OnClick;
  toggleLine: OnClickWithId;
  removeSelectedListItems: OnClick;
}

const LegendComponent = withEmptyContent<DispatchToProps & StateToProps>(Legend);

const mapStateToProps = ({report, routing}: RootState): StateToProps => {
  const {hiddenLines, resolution, savedReports} = report;
  const legendItems = getLegendItems(report);
  return ({
    legendItems,
    hiddenLines,
    isReportPage: isReportPage(routing),
    hasContent: legendItems.length > 0,
    noContentText: firstUpperTranslated('select meters'),
    resolution,
    savedReports,
  });
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  deleteItem,
  removeSelectedListItems,
  hideAllLines,
  toggleLine,
}, dispatch);

export const LegendContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(LegendComponent);
