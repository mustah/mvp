import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {OnChangeToolbarView, ToolbarView} from '../../../state/ui/toolbar/toolbarModels';
import {SelectionInterval} from '../../../state/user-selection/userSelectionModels';
import {Callback, CallbackWith, Sectors} from '../../../types/Types';
import {changeToolbarView, exportToExcel, setCollectionTimePeriod} from '../collectionActions';
import {CollectionToolbar} from '../components/CollectionToolbar';

export interface StateToProps {
  hasCollectionStats: boolean;
  view: ToolbarView;
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
  collection: {isExportingToExcel, timePeriod},
  domainModels: {collectionStats: {isFetching, result}},
  ui: {toolbar: {collection: {view}}}
}: RootState): StateToProps =>
  ({
    hasCollectionStats: result.length > 0,
    isFetching,
    isExportingToExcel,
    view,
    timePeriod,
  });

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  changeToolbarView,
  exportToExcel,
  setCollectionTimePeriod: setCollectionTimePeriod(Sectors.collection),
}, dispatch);

export const CollectionToolbarContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(CollectionToolbar);
