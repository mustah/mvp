import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {withEmptyContent, WithEmptyContentProps} from '../../../components/hoc/withEmptyContent';
import {RootState} from '../../../reducers/rootReducer';
import {isReportPage} from '../../../selectors/routerSelectors';
import {firstUpperTranslated} from '../../../services/translationService';
import {Normalized} from '../../../state/domain-models/domainModels';
import {OnClick, OnClickWithId} from '../../../types/Types';
import {Legend, LegendProps} from '../components/Legend';
import {clearSelectedListItems, hideAllLines, toggleLine, toggleSingleEntry} from '../reportActions';
import {LegendItem, ReportState} from '../reportModels';
import {getLegendItems} from '../reportSelectors';

interface StateToProps extends ReportState, WithEmptyContentProps {
  legendItems: Normalized<LegendItem>;
  isReportPage: boolean;
}

interface DispatchToProps {
  deleteItem: OnClickWithId;
  toggleLine: OnClickWithId;
  clearSelectedListItems: OnClick;
}

const LegendComponent = withEmptyContent<LegendProps & ReportState & WithEmptyContentProps>(Legend);

const mapStateToProps = ({
  report: {hiddenLines, resolution, selectedListItems},
  routing,
  selectionTree: {entities},
}: RootState): StateToProps =>
  ({
    legendItems: getLegendItems({selectedListItems, entities}),
    hiddenLines,
    isReportPage: isReportPage(routing),
    resolution,
    selectedListItems,
    hasContent: selectedListItems.length > 0,
    noContentText: firstUpperTranslated('select meters'),
  });

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  deleteItem: toggleSingleEntry,
  clearSelectedListItems,
  hideAllLines,
  toggleLine,
}, dispatch);

export const LegendContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(LegendComponent);
