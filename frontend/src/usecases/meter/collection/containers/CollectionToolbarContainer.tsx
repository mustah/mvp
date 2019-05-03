import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../../reducers/rootReducer';
import {changeCollectionToolbarView} from '../../../../state/ui/toolbar/toolbarActions';
import {OnChangeToolbarView, ToolbarViewSettings} from '../../../../state/ui/toolbar/toolbarModels';
import {SelectionInterval} from '../../../../state/user-selection/userSelectionModels';
import {Callback, CallbackWith, Sectors} from '../../../../types/Types';
import {setCollectionTimePeriod} from '../../../collection/collectionActions';
import {CollectionToolbar} from '../../../collection/components/CollectionToolbar';
import {exportToExcel} from '../meterCollectionActions';

export interface StateToProps extends ToolbarViewSettings {
  hasCollectionStats: boolean;
  isFetching: boolean;
  isExportingToExcel: boolean;
  timePeriod: SelectionInterval;
}

export interface DispatchToProps {
  changeToolbarView: OnChangeToolbarView;
  exportToExcel: Callback;
  setCollectionTimePeriod: CallbackWith<SelectionInterval>;
}

const mapStateToProps = ({
  meterCollection: {isExportingToExcel, timePeriod},
  domainModels: {meterCollectionStats: {isFetching, result}},
  ui: {toolbar: {meterCollection: {view}}}
}: RootState): StateToProps =>
  ({
    hasCollectionStats: result.length > 0,
    isFetching,
    isExportingToExcel,
    view,
    timePeriod,
  });

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  changeToolbarView: changeCollectionToolbarView(Sectors.meterCollection),
  exportToExcel,
  setCollectionTimePeriod: setCollectionTimePeriod(Sectors.meterCollection),
}, dispatch);

export const CollectionToolbarContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(CollectionToolbar);
