import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {Meter} from '../../../state/domain-models-paginated/meter/meterModels';
import {
  getPageResult,
  getPaginatedEntities
} from '../../../state/domain-models-paginated/paginatedDomainModelsSelectors';
import {ObjectsById} from '../../../state/domain-models/domainModels';
import {addAllToSelectionReport} from '../../../state/report/reportActions';
import {LegendItem} from '../../../state/report/reportModels';
import {EntityTypes, Pagination} from '../../../state/ui/pagination/paginationModels';
import {getPagination} from '../../../state/ui/pagination/paginationSelectors';
import {ToolbarView} from '../../../state/ui/toolbar/toolbarModels';
import {CallbackWith, uuid} from '../../../types/Types';
import {SelectionReport} from '../components/SelectionReport';

interface StateToProps {
  view: ToolbarView;
  legendItems: LegendItem[];
  result: uuid[];
  entities: ObjectsById<Meter>;
}

interface DispatchToProps {
  addAllToSelectionReport: CallbackWith<LegendItem[]>;
}

export type Props = StateToProps & DispatchToProps;

const mapStateToProps = (rootState: RootState): StateToProps => {
  const {
    ui: {toolbar: {selectionReport: {view}}, pagination: paginationModel},
    paginatedDomainModels: {meters},
    selectionReport: {savedReports: {meterPage: {legendItems}}}
  } = rootState;

  const entityType: EntityTypes = 'meters';
  const pagination: Pagination = getPagination({entityType, pagination: paginationModel});
  const {page} = pagination;

  return ({
    view,
    legendItems,
    entities: getPaginatedEntities<Meter>(meters),
    result: getPageResult<Meter>(meters, page),
  });
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  addAllToSelectionReport
}, dispatch);

export const SelectionReportContentContainer = connect<StateToProps, DispatchToProps>(
  mapStateToProps,
  mapDispatchToProps
)(SelectionReport);
