import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {exportToExcel} from '../../../state/ui/graph/measurement/measurementActions';
import {changeCollectionToolbarView} from '../../../state/ui/toolbar/toolbarActions';
import {OnChangeToolbarView, ToolbarView} from '../../../state/ui/toolbar/toolbarModels';
import {SelectionInterval} from '../../../state/user-selection/userSelectionModels';
import {Callback, CallbackWith} from '../../../types/Types';
import {setCollectionTimePeriod} from '../collectionActions';
import {CollectionToolbar} from '../components/CollectionToolbar';

interface StateToProps {
  hasCollectionStats: boolean;
  view: ToolbarView;
  isFetching: boolean;
  timePeriod: SelectionInterval;
}

interface DispatchToProps {
  changeToolbarView: OnChangeToolbarView;
  exportToExcel: Callback;
  setCollectionTimePeriod: CallbackWith<SelectionInterval>;
}

export type Props = StateToProps & DispatchToProps;

const mapStateToProps = ({
  collection: {timePeriod},
  domainModels: {collectionStats: {isFetching, result}}, // isExportingToExcel
  ui: {toolbar: {collection: {view}}}
}: RootState): StateToProps =>
  ({
    hasCollectionStats: result.length > 0,
    isFetching,
    view,
    timePeriod,
  });

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  changeToolbarView: changeCollectionToolbarView,
  exportToExcel,
  setCollectionTimePeriod,
}, dispatch);

export const CollectionToolbarContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(CollectionToolbar);
