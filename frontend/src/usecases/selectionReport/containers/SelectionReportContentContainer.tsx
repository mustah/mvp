import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {Meter} from '../../../state/domain-models-paginated/meter/meterModels';
import {getPaginatedResult} from '../../../state/domain-models-paginated/paginatedDomainModelsSelectors';
import {ObjectsById} from '../../../state/domain-models/domainModels';
import {addAllToSelectionReport} from '../../../state/report/reportActions';
import {LegendItem} from '../../../state/report/reportModels';
import {ToolbarViewSettingsProps} from '../../../state/ui/toolbar/toolbarModels';
import {CallbackWith, uuid} from '../../../types/Types';
import {SelectionReport} from '../components/SelectionReport';

interface StateToProps extends ToolbarViewSettingsProps {
  legendItems: LegendItem[];
  result: uuid[];
  entities: ObjectsById<Meter>;
}

interface DispatchToProps {
  addAllToSelectionReport: CallbackWith<LegendItem[]>;
}

export type Props = StateToProps & DispatchToProps;

const mapStateToProps = ({
  ui: {toolbar: {selectionReport: {view}}, pagination: paginationState},
  paginatedDomainModels: {meters},
  selectionReport: {savedReports: {meterPage: {legendItems}}}
}: RootState): StateToProps => {
  const {page} = paginationState.meters;

  return ({
    view,
    legendItems,
    entities: meters.entities,
    result: getPaginatedResult<Meter>(meters, page),
  });
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  addAllToSelectionReport
}, dispatch);

export const SelectionReportContentContainer = connect(mapStateToProps, mapDispatchToProps)(SelectionReport);
