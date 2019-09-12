import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {addAllToSelectionReport, fetchLegendItems, limit} from '../../../state/report/reportActions';
import {LegendItem} from '../../../state/report/reportModels';
import {getLegendItemsWithLimit} from '../../../state/report/reportSelectors';
import {ToolbarViewSettingsProps} from '../../../state/ui/toolbar/toolbarModels';
import {getMeterParameters} from '../../../state/user-selection/userSelectionSelectors';
import {CallbackWith, EncodedUriParameters, Fetch} from '../../../types/Types';
import {SelectionReportContent} from '../components/SelectionReportContent';

interface StateToProps extends ToolbarViewSettingsProps {
  isSuccessfullyFetched: boolean;
  legendItems: LegendItem[];
  parameters: EncodedUriParameters;
  legendItemsParameters: EncodedUriParameters;
}

interface DispatchToProps {
  addAllToSelectionReport: CallbackWith<LegendItem[]>;
  fetchLegendItems: Fetch;
}

export type Props = StateToProps & DispatchToProps;

const mapStateToProps = ({
  domainModels: {legendItems},
  userSelection,
  ui: {toolbar: {selectionReport: {view}}}
}: RootState): StateToProps =>
  ({
    isSuccessfullyFetched: legendItems.isSuccessfullyFetched,
    legendItems: getLegendItemsWithLimit(legendItems),
    parameters: getMeterParameters(userSelection),
    legendItemsParameters: getMeterParameters({limit, ...userSelection}),
    view,
  });

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  addAllToSelectionReport,
  fetchLegendItems,
}, dispatch);

export const SelectionReportContentContainer = connect(mapStateToProps, mapDispatchToProps)(SelectionReportContent);
