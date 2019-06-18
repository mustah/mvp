import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {encodeRequestParameters, requestParametersFrom} from '../../../../helpers/urlFactory';
import {RootState} from '../../../../reducers/rootReducer';
import {
  fetchMeterCollectionStats as fetchCollectionStats,
  meterCollectionStatClearError as clearError,
} from '../../../../state/domain-models/collection-stat/collectionStatActions';
import {getError} from '../../../../state/domain-models/domainModelsSelectors';
import {uuid} from '../../../../types/Types';
import {meterCollectionStatsExportToExcelSuccess as exportToExcelSuccess} from '../../../collection/collectionActions';
import {CollectionStatBarChart} from '../../../collection/components/CollectionStatBarChart';
import {DispatchToProps, StateToProps} from '../../../collection/containers/CollectionGraphContainer';

interface OwnProps {
  meterId: uuid;
}

const mapStateToProps = (
  {
    meterCollection: {timePeriod},
    domainModels: {meterCollectionStats},
  }: RootState,
  {meterId}: OwnProps
): StateToProps => {
  const parameters = encodeRequestParameters({
    ...requestParametersFrom({collectionDateRange: timePeriod}),
    logicalMeterId: meterId.toString()
  });

  return ({
    collectionStats: meterCollectionStats.entities,
    error: getError(meterCollectionStats),
    isFetching: meterCollectionStats.isFetching,
    parameters,
  });
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  clearError,
  exportToExcelSuccess,
  fetchCollectionStats,
}, dispatch);

export const CollectionGraphContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(CollectionStatBarChart);
