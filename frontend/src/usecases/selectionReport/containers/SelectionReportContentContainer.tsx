import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {isNotNull} from '../../../helpers/commonHelpers';
import {RootState} from '../../../reducers/rootReducer';
import {
  getAllMeters,
  isMetersPageFetching
} from '../../../state/domain-models-paginated/paginatedDomainModelsSelectors';
import {addAllToSelectionReport} from '../../../state/report/reportActions';
import {LegendItem} from '../../../state/report/reportModels';
import {ToolbarViewSettingsProps} from '../../../state/ui/toolbar/toolbarModels';
import {CallbackWith, Fetching} from '../../../types/Types';
import {toLegendItem} from '../../report/helpers/legendHelper';
import {SelectionReport} from '../components/SelectionReport';

interface StateToProps extends ToolbarViewSettingsProps, Fetching {
  legendItems: LegendItem[];
  newLegendItems: LegendItem[];
}

interface DispatchToProps {
  addAllToSelectionReport: CallbackWith<LegendItem[]>;
}

export type Props = StateToProps & DispatchToProps;

const mapStateToProps = ({
  ui: {toolbar: {selectionReport: {view}}, pagination},
  paginatedDomainModels: {meters},
  selectionReport: {savedReports: {meterPage: {legendItems}}}
}: RootState): StateToProps =>
  ({
    isFetching: isMetersPageFetching(meters, pagination),
    legendItems,
    newLegendItems: getAllMeters(meters).filter(isNotNull).map(toLegendItem),
    view,
  });

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  addAllToSelectionReport
}, dispatch);

export const SelectionReportContentContainer = connect(mapStateToProps, mapDispatchToProps)(SelectionReport);
