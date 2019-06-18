import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {
  changeToolbarView,
  exportCollectionStatsToExcel as exportToExcel,
  setCollectionStatsTimePeriod
} from '../collectionActions';
import {CollectionToolbar, DispatchToProps, StateToProps} from '../components/CollectionToolbar';

const mapStateToProps = ({
  collection: {isExportingToExcel, timePeriod},
  domainModels: {collectionStats: {isFetching, result}},
  ui: {toolbar: {collection: {view}}}
}: RootState): StateToProps => ({
  canExportToExcel: true,
  hasCollectionStats: result.length > 0,
  isFetching,
  isExportingToExcel,
  view,
  timePeriod,
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  changeToolbarView,
  exportToExcel,
  setCollectionStatsTimePeriod,
}, dispatch);

export const CollectionToolbarContainer = connect(mapStateToProps, mapDispatchToProps)(CollectionToolbar);
