import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../../reducers/rootReducer';
import {changeCollectionToolbarView} from '../../../../state/ui/toolbar/toolbarActions';
import {Sectors} from '../../../../types/Types';
import {
  exportMeterCollectionStatsToExcel as exportToExcel,
  setMeterCollectionStatsTimePeriod as setCollectionStatsTimePeriod,
} from '../../../collection/collectionActions';
import {CollectionToolbar, DispatchToProps, StateToProps} from '../../../collection/components/CollectionToolbar';

const mapStateToProps = ({
  meterCollection: {isExportingToExcel, timePeriod},
  domainModels: {meterCollectionStats: {isFetching, result}},
  ui: {toolbar: {meterCollection: {view}}}
}: RootState): StateToProps => ({
  hasCollectionStats: result.length > 0,
  isFetching,
  isExportingToExcel,
  view,
  timePeriod,
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  changeToolbarView: changeCollectionToolbarView(Sectors.meterCollection),
  exportToExcel,
  setCollectionStatsTimePeriod,
}, dispatch);

export const CollectionToolbarContainer = connect(mapStateToProps, mapDispatchToProps)(CollectionToolbar);
